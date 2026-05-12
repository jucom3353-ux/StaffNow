const GRACE_PERIOD_MINS = 5   // 유예 기간 (분) — 백엔드 설계 기준 5분

function toMins(timeStr) {
  const [h, m] = timeStr.split(':').map(Number)
  return h * 60 + m
}

/**
 * 정산용 퇴근 시간(billable checkout) 계산
 *
 * 규칙:
 *  1. overtimeApproved=true → 실제 퇴근 시간 그대로 인정
 *  2. 실제 퇴근 < 예정 종료 → early_leave (기본급 비례, 보너스 없음)
 *  3. 실제 퇴근 = 예정 종료 → normal
 *  4. 실제 퇴근 ≤ (예정 종료 + 유예기간) → grace (정시 처리)
 *  5. 실제 퇴근 > (예정 종료 + 유예기간) + 미승인 → capped
 *
 * @returns {{ billableCheckout: string, cappedMins: number, status: string }}
 */
export function calcBillableCheckout(
  actualCheckout,
  scheduledEnd,
  overtimeApproved = false,
  graceMins = GRACE_PERIOD_MINS
) {
  const actualMins     = toMins(actualCheckout)
  const scheduledMins  = toMins(scheduledEnd)
  const graceLimitMins = scheduledMins + graceMins

  if (overtimeApproved) {
    return { billableCheckout: actualCheckout, cappedMins: 0, status: 'approved' }
  }
  if (actualMins < scheduledMins) {
    return { billableCheckout: actualCheckout, cappedMins: 0, status: 'early_leave' }
  }
  if (actualMins === scheduledMins) {
    return { billableCheckout: actualCheckout, cappedMins: 0, status: 'normal' }
  }
  if (actualMins <= graceLimitMins) {
    return {
      billableCheckout: scheduledEnd,
      cappedMins: actualMins - scheduledMins,
      status: 'grace',
    }
  }
  return {
    billableCheckout: scheduledEnd,
    cappedMins: actualMins - scheduledMins,
    status: 'capped',
  }
}

/**
 * 정산 시간(시간 단위 float) 계산
 *
 * @returns {{ hours, billableCheckout, actualCheckout, cappedMins, overtimeStatus }}
 */
export function calcBillableHours(checkIn, actualCheckout, scheduledEnd, overtimeApproved = false) {
  if (!checkIn || !actualCheckout) {
    return { hours: 0, billableCheckout: null, actualCheckout: null, cappedMins: 0, overtimeStatus: 'absent' }
  }

  const { billableCheckout, cappedMins, status } = calcBillableCheckout(
    actualCheckout, scheduledEnd, overtimeApproved
  )

  const checkInMins  = toMins(checkIn)
  const checkOutMins = toMins(billableCheckout)

  let totalMins = checkOutMins - checkInMins
  if (totalMins < 0) totalMins += 24 * 60  // 자정 넘김 처리

  return {
    hours: Math.max(0, totalMins / 60),
    billableCheckout,
    actualCheckout,
    cappedMins,
    overtimeStatus: status,
  }
}

/**
 * 인센티브 급여 산정 (기본급 + 근태 보너스)
 *
 * 보너스 지급 조건: normal|grace|capped|approved 상태 AND 지각 없음
 *
 * @param {string} checkIn              - 실제 출근 "HH:MM"
 * @param {string} actualCheckout       - 실제 퇴근 "HH:MM"
 * @param {string} scheduledStart       - 계약 시작 "HH:MM" (지각 판정용)
 * @param {string} scheduledEnd         - 계약 종료 "HH:MM"
 * @param {boolean} overtimeApproved    - 연장 승인 여부
 * @param {{ basePay: number, bonusPay: number }} payConfig - 급여 설정
 * @returns {{
 *   hours, billableCheckout, actualCheckout, cappedMins, overtimeStatus,
 *   basePay, bonusPay, totalPay, isLate, lateMins
 * }}
 */
export function calcPayoutWithBonus(
  checkIn,
  actualCheckout,
  scheduledStart,
  scheduledEnd,
  overtimeApproved = false,
  payConfig = null
) {
  const base = calcBillableHours(checkIn, actualCheckout, scheduledEnd, overtimeApproved)

  if (!payConfig) {
    return { ...base, basePay: 0, bonusPay: 0, totalPay: 0, isLate: false, lateMins: 0 }
  }

  const { basePay, bonusPay } = payConfig

  // 지각 판정: 실제 출근 > 계약 시작
  const lateMins = checkIn
    ? Math.max(0, toMins(checkIn) - toMins(scheduledStart))
    : 0
  const isLate = lateMins > 0

  // 노쇼 판정용: checkIn이 없고 scheduledStart + NOSHOW_CUTOFF_MINS 경과 여부는
  // 프론트에서 현재시각 기준으로 판단 (서버 없이 시뮬레이션)
  const isNoShow = !checkIn  // 데모에서는 checkIn 없으면 absent/no_show

  if (isNoShow) {
    return { ...base, basePay: 0, bonusPay: 0, totalPay: 0, isLate: false, lateMins: 0 }
  }

  // 기본급: 예정 총 시간 대비 실근무 시간 비례
  const scheduledTotalMins = toMins(scheduledEnd) - toMins(scheduledStart)
  const baseRatePerMin     = scheduledTotalMins > 0 ? basePay / scheduledTotalMins : 0
  const billableMins       = Math.round(base.hours * 60)
  const basePayEarned      = Math.min(Math.round(baseRatePerMin * billableMins), basePay)

  // 보너스: 정상 근무 완료 + 지각 없음
  const bonusEligible = new Set(['normal', 'grace', 'capped', 'approved'])
  const bonusPayEarned = (bonusEligible.has(base.overtimeStatus) && !isLate) ? bonusPay : 0

  return {
    ...base,
    basePay: basePayEarned,
    bonusPay: bonusPayEarned,
    totalPay: basePayEarned + bonusPayEarned,
    isLate,
    lateMins,
  }
}

/**
 * 시간 float → "Xh Ym" 레이블
 */
export function hoursLabel(h) {
  if (h === 0) return '0h'
  const hh = Math.floor(h)
  const mm = Math.round((h - hh) * 60)
  return mm > 0 ? `${hh}h ${mm}m` : `${hh}h`
}
