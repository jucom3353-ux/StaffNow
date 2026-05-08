const GRACE_PERIOD_MINS = 10  // 유예 기간 (분)

/**
 * 시간 문자열 "HH:MM" → 분(number)
 */
function toMins(timeStr) {
  const [h, m] = timeStr.split(':').map(Number)
  return h * 60 + m
}

/**
 * 분(number) → "HH:MM"
 */
function fromMins(mins) {
  const h = Math.floor(mins / 60) % 24
  const m = mins % 60
  return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}`
}

/**
 * 정산용 퇴근 시간(billable checkout) 계산
 *
 * 규칙:
 *  1. 연장 승인 완료(overtimeApproved=true) → 실제 퇴근 시간 그대로 인정
 *  2. 실제 퇴근이 (예정 종료 + 유예기간) 이내 → 예정 종료 시간으로 고정 (정시 퇴근 처리)
 *  3. 실제 퇴근이 (예정 종료 + 유예기간) 초과 + 미승인 → 예정 종료 시간으로 Cap
 *
 * @param {string} actualCheckout   - 실제 퇴근 "HH:MM"
 * @param {string} scheduledEnd     - 예정 종료 "HH:MM"
 * @param {boolean} overtimeApproved - 연장 승인 여부
 * @param {number} graceMins        - 유예 기간(분), 기본 10분
 * @returns {{ billableCheckout: string, cappedMins: number, status: 'normal'|'grace'|'capped'|'approved' }}
 */
export function calcBillableCheckout(
  actualCheckout,
  scheduledEnd,
  overtimeApproved = false,
  graceMins = GRACE_PERIOD_MINS
) {
  const actualMins    = toMins(actualCheckout)
  const scheduledMins = toMins(scheduledEnd)
  const graceLimitMins = scheduledMins + graceMins

  // ── Case 1: 연장 승인 ─────────────────────────────────
  if (overtimeApproved) {
    return {
      billableCheckout: actualCheckout,
      cappedMins: 0,
      status: 'approved',
    }
  }

  // ── Case 2: 정시 또는 조기 퇴근 ──────────────────────
  if (actualMins <= scheduledMins) {
    return {
      billableCheckout: actualCheckout,
      cappedMins: 0,
      status: 'normal',
    }
  }

  // ── Case 3: 유예 기간 이내 초과 → 정시 퇴근으로 처리 ──
  if (actualMins <= graceLimitMins) {
    return {
      billableCheckout: scheduledEnd,
      cappedMins: actualMins - scheduledMins,  // 실제 초과분 (기록용)
      status: 'grace',
    }
  }

  // ── Case 4: 유예 기간 초과 + 미승인 → 예정 종료로 Cap ─
  return {
    billableCheckout: scheduledEnd,
    cappedMins: actualMins - scheduledMins,   // 미인정 분
    status: 'capped',
  }
}

/**
 * 정산 시간(시간 단위 float) 계산
 *
 * @param {string} checkIn
 * @param {string} actualCheckout
 * @param {string} scheduledEnd
 * @param {boolean} overtimeApproved
 * @returns {{ hours: number, billableCheckout: string, actualCheckout: string, cappedMins: number, overtimeStatus: string }}
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
 * 시간 float → "Xh Ym" 레이블
 */
export function hoursLabel(h) {
  if (h === 0) return '0h'
  const hh = Math.floor(h)
  const mm = Math.round((h - hh) * 60)
  return mm > 0 ? `${hh}h ${mm}m` : `${hh}h`
}
