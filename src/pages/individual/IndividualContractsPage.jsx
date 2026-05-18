import { useState, useEffect, useRef, useLayoutEffect } from 'react'
import SignatureCanvas from 'react-signature-canvas'
import {
  FileCheck, X, CheckCircle2, Building2, User,
  PenLine, AlertCircle, RotateCcw
} from 'lucide-react'
import Card from '../../components/ui/Card'
import EmptyState from '../../components/ui/EmptyState'
import { contractApi } from '../../services/api'

const WAGE_LABEL = { HOURLY: '시급', DAILY: '일급', MONTHLY: '월급', FIXED: '고정급' }

const STATUS_META = {
  PENDING:   { label: '서명 대기',  color: 'text-amber-600 bg-amber-50 border-amber-200' },
  SIGNED:    { label: '계약 완료',  color: 'text-green-600 bg-green-50 border-green-200' },
  CANCELLED: { label: '취소됨',     color: 'text-red-600 bg-red-50 border-red-200' },
}

function calcWorkHours(startTime, endTime, breakMinutes) {
  if (!startTime || !endTime) return null
  const [h1, m1] = startTime.split(':').map(Number)
  const [h2, m2] = endTime.split(':').map(Number)
  const mins = (h2 * 60 + m2) - (h1 * 60 + m1) - (breakMinutes || 0)
  if (mins <= 0) return null
  const hh = Math.floor(mins / 60)
  const mm = mins % 60
  return mm > 0 ? `${hh}시간 ${mm}분` : `${hh}시간`
}

function SigPad({ sigRef, onEnd, isEmpty }) {
  const containerRef = useRef(null)

  useLayoutEffect(() => {
    if (!containerRef.current || !sigRef.current) return
    const { width } = containerRef.current.getBoundingClientRect()
    const canvas = sigRef.current.getCanvas()
    const dpr = window.devicePixelRatio || 1
    canvas.width  = width * dpr
    canvas.height = 140   * dpr
    canvas.style.width  = `${width}px`
    canvas.style.height = '140px'
    canvas.getContext('2d').scale(dpr, dpr)
  }, [sigRef])

  return (
    <div
      ref={containerRef}
      className="relative border-2 border-dashed border-offwhite-300 rounded-xl overflow-hidden bg-offwhite-50"
      style={{ height: 140 }}
    >
      <SignatureCanvas
        ref={sigRef}
        onEnd={onEnd}
        clearOnResize={false}
        canvasProps={{ style: { position: 'absolute', top: 0, left: 0 } }}
      />
      {isEmpty && (
        <div className="absolute inset-0 flex items-center justify-center pointer-events-none">
          <span className="text-xs text-gray-300">여기에 서명해 주세요</span>
        </div>
      )}
    </div>
  )
}

function ContractSignModal({ contract, onClose, onSigned }) {
  const sigRef = useRef(null)
  const [signing,     setSigning]     = useState(false)
  const [localSigned, setLocalSigned] = useState(false)
  const [isEmpty,     setIsEmpty]     = useState(true)

  const isSigned  = localSigned || contract.status === 'SIGNED'
  const isPending = contract.status === 'PENDING'
  const wageLabel = WAGE_LABEL[contract.wageType] || '시급'
  const workHoursLabel = calcWorkHours(contract.startTime, contract.endTime, contract.breakTime)
  const signedDate = contract.workerSignedAt?.substring(0, 10)
    ?? (localSigned ? new Date().toISOString().substring(0, 10) : null)

  async function handleSign() {
    if (isEmpty) return
    setSigning(true)
    try {
      const res = await contractApi.sign(contract.id)
      if (res.ok) {
        setLocalSigned(true)
        onSigned(contract.id)
      }
    } catch {
      // no-op
    } finally {
      setSigning(false)
    }
  }

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4"
      onClick={onClose}
    >
      <div
        className="bg-white rounded-2xl shadow-2xl w-full max-w-xl max-h-[95vh] overflow-y-auto"
        onClick={e => e.stopPropagation()}
      >
        {/* 헤더 */}
        <div className="sticky top-0 bg-white flex items-center justify-between px-5 pt-5 pb-3 border-b border-offwhite-200 z-10">
          <div>
            <h2 className="text-base font-extrabold text-navy">아르바이트 근로계약서</h2>
            <p className="text-xs text-gray-400 mt-0.5">{contract.jobPostTitle}</p>
          </div>
          <button
            onClick={onClose}
            className="w-8 h-8 rounded-lg hover:bg-offwhite-100 flex items-center justify-center text-gray-400"
          >
            <X size={16} />
          </button>
        </div>

        <div className="px-5 py-4 space-y-4 text-sm">

          {/* 당사자 */}
          <div className="grid grid-cols-2 gap-3">
            <div className="bg-offwhite-100 rounded-xl p-3.5">
              <div className="flex items-center gap-1.5 mb-1.5">
                <Building2 size={12} className="text-navy" />
                <span className="text-xs font-bold text-navy uppercase tracking-wider">갑 (사업주)</span>
              </div>
              <p className="font-semibold text-navy text-sm">{contract.companyName || '—'}</p>
            </div>
            <div className="bg-offwhite-100 rounded-xl p-3.5">
              <div className="flex items-center gap-1.5 mb-1.5">
                <User size={12} className="text-navy" />
                <span className="text-xs font-bold text-navy uppercase tracking-wider">을 (근로자)</span>
              </div>
              <p className="font-semibold text-navy text-sm">{contract.workerName || '—'}</p>
            </div>
          </div>

          {/* 제1조 */}
          <div className="border border-offwhite-200 rounded-xl p-4">
            <p className="text-xs font-bold text-navy mb-1.5">제1조 — 근무장소</p>
            <p className="text-gray-700">{contract.workLocation || '—'}</p>
          </div>

          {/* 제2조 */}
          <div className="border border-offwhite-200 rounded-xl p-4">
            <p className="text-xs font-bold text-navy mb-1.5">제2조 — 직종 / 업무내용</p>
            <p className="text-gray-700">{contract.workType || '—'}</p>
          </div>

          {/* 제3조 */}
          <div className="border border-offwhite-200 rounded-xl p-4">
            <p className="text-xs font-bold text-navy mb-1.5">제3조 — 근로계약기간</p>
            <p className="text-gray-700">
              {contract.contractStartDate && contract.contractEndDate
                ? `${contract.contractStartDate} ~ ${contract.contractEndDate}`
                : '—'}
            </p>
            {contract.startTime && contract.endTime && (
              <p className="text-xs text-gray-400 mt-1.5">
                근무시간 {contract.startTime} ~ {contract.endTime}
                {workHoursLabel && ` · 실근무 ${workHoursLabel}`}
              </p>
            )}
          </div>

          {/* 제4조 */}
          <div className="border border-offwhite-200 rounded-xl p-4">
            <p className="text-xs font-bold text-navy mb-3">제4조 — 임금</p>
            <div className="flex justify-between py-2 border-b border-offwhite-100">
              <span className="text-gray-400 text-xs">{wageLabel}</span>
              <span className="font-semibold text-navy text-xs">
                {contract.wageAmount
                  ? `₩${Number(contract.wageAmount).toLocaleString('ko-KR')}`
                  : '—'}
              </span>
            </div>
            <div className="mt-2.5 text-xs text-gray-400 space-y-0.5">
              <p>• 임금에는 주휴수당이 포함될 수 있습니다.</p>
              <p>• 소득세(3.3%) 원천징수 후 지급됩니다.</p>
              <p>• 임금은 익월 말일 이내 지급됩니다.</p>
            </div>
          </div>

          {/* 고정 약관 */}
          <div className="bg-offwhite-100 rounded-xl px-4 py-3.5 text-xs text-gray-500 leading-relaxed space-y-2.5">
            <div>
              <p className="font-bold text-navy mb-1">제5조 — 근무 기본사항</p>
              <p>• 무단결근 시 해당 근무 임금은 지급되지 않습니다.</p>
              <p>• 지각·조퇴·결근은 해당 시간 임금에서 공제됩니다.</p>
              <p>• 부당 이직·이탈 시 사전 고지 없이 급여가 보류될 수 있습니다.</p>
            </div>
            <div>
              <p className="font-bold text-navy mb-1">제6조 — 비밀유지</p>
              <p>• 근로기간 중 알게 된 회사의 영업정보 및 개인정보는 외부에 유출할 수 없습니다.</p>
            </div>
            <div>
              <p className="font-bold text-navy mb-1">제7조 — 소송 관할</p>
              <p>• 분쟁 발생 시 관할 법원은 광주 지방법원으로 합니다.</p>
            </div>
            <p className="pt-1 border-t border-offwhite-200">
              본 계약은 「근로기준법」에 따른 단기 근로 계약이며, 양 당사자는 위 내용에 동의하여 서명합니다.
            </p>
          </div>

          {/* 서명란 */}
          <div className="border border-offwhite-200 rounded-xl p-4">
            <p className="text-xs font-bold text-navy mb-3">전자서명 (을 — 근로자)</p>
            {isSigned ? (
              <div className="flex items-center gap-2 py-2 px-3 bg-green-50 border border-green-200 rounded-xl">
                <CheckCircle2 size={15} className="text-green-500 shrink-0" />
                <span className="text-sm font-semibold text-green-700 flex-1">서명 완료</span>
                {signedDate && <span className="text-xs text-green-400">{signedDate}</span>}
              </div>
            ) : (
              <div className="space-y-2">
                <SigPad
                  sigRef={sigRef}
                  onEnd={() => setIsEmpty(sigRef.current?.isEmpty() ?? true)}
                  isEmpty={isEmpty}
                />
                <button
                  onClick={() => { sigRef.current?.clear(); setIsEmpty(true) }}
                  className="flex items-center gap-1.5 text-xs text-gray-400 hover:text-navy transition-colors"
                >
                  <RotateCcw size={11} />다시 쓰기
                </button>
              </div>
            )}
          </div>
        </div>

        {/* 하단 버튼 */}
        <div className="px-5 pb-5 flex gap-2">
          <button
            onClick={onClose}
            className="flex-1 py-2.5 text-sm font-semibold text-gray-500 bg-offwhite-100 hover:bg-offwhite-200 rounded-xl transition-colors"
          >
            닫기
          </button>
          {isPending && !isSigned && (
            <button
              onClick={handleSign}
              disabled={isEmpty || signing}
              className="flex-1 py-2.5 text-sm font-semibold text-white bg-navy hover:bg-navy-700 rounded-xl transition-colors disabled:opacity-50 flex items-center justify-center gap-2"
            >
              <PenLine size={14} />
              {signing ? '처리 중...' : '서명 완료'}
            </button>
          )}
        </div>
      </div>
    </div>
  )
}

function ContractCard({ contract, onClick }) {
  const meta = STATUS_META[contract.status] ?? STATUS_META.PENDING
  const isPending = contract.status === 'PENDING'
  return (
    <Card padding={false}>
      <div className="p-4">
        <div className="flex items-start justify-between gap-2 mb-1.5">
          <div className="min-w-0">
            <p className="font-semibold text-navy truncate">{contract.jobPostTitle}</p>
            <p className="text-xs text-gray-400 mt-0.5">{contract.companyName}</p>
          </div>
          <span className={`shrink-0 inline-flex text-xs font-semibold px-2.5 py-1 rounded-full border ${meta.color}`}>
            {meta.label}
          </span>
        </div>
        <p className="text-xs text-gray-500 mb-3">
          {contract.contractStartDate} ~ {contract.contractEndDate}
        </p>
        <div className="flex items-center justify-between pt-2.5 border-t border-offwhite-100">
          <p className="font-semibold text-navy">
            {contract.wageAmount
              ? `₩${Number(contract.wageAmount).toLocaleString('ko-KR')}`
              : '—'}
          </p>
          <button
            onClick={onClick}
            className={`text-xs font-semibold px-3 py-2 rounded-lg transition-colors flex items-center gap-1.5 ${
              isPending
                ? 'text-white bg-navy hover:bg-navy-700'
                : 'text-navy bg-offwhite-100 hover:bg-offwhite-200 border border-offwhite-200'
            }`}
          >
            {isPending
              ? <><PenLine size={11} />서명하기</>
              : '계약서 보기'
            }
          </button>
        </div>
      </div>
    </Card>
  )
}

export default function IndividualContractsPage() {
  const [contracts, setContracts] = useState([])
  const [loading,   setLoading]   = useState(true)
  const [selected,  setSelected]  = useState(null)

  useEffect(() => {
    contractApi.myList()
      .then(res => res.ok ? res.json() : [])
      .then(data => setContracts(Array.isArray(data) ? data : []))
      .catch(() => setContracts([]))
      .finally(() => setLoading(false))
  }, [])

  function handleSigned(contractId) {
    const now = new Date().toISOString()
    setContracts(prev =>
      prev.map(c => c.id === contractId ? { ...c, status: 'SIGNED', workerSignedAt: now } : c)
    )
    setSelected(prev =>
      prev?.id === contractId ? { ...prev, status: 'SIGNED', workerSignedAt: now } : prev
    )
  }

  const pending = contracts.filter(c => c.status === 'PENDING')
  const others  = contracts.filter(c => c.status !== 'PENDING')

  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-xl font-bold text-navy">근로계약서</h1>
        <p className="text-sm text-gray-500 mt-0.5">총 {contracts.length}건의 계약</p>
      </div>

      {loading ? (
        <Card>
          <div className="py-8 text-center text-sm text-gray-400">계약 정보를 불러오는 중...</div>
        </Card>
      ) : contracts.length === 0 ? (
        <Card>
          <EmptyState
            icon={FileCheck}
            title="계약 내역이 없습니다"
            description="합격 처리된 이후 계약서가 발송되면 여기에 표시됩니다"
          />
        </Card>
      ) : (
        <>
          {pending.length > 0 && (
            <div>
              <div className="flex items-center gap-2 mb-2">
                <AlertCircle size={14} className="text-amber-500" />
                <p className="text-sm font-semibold text-amber-700">
                  서명이 필요한 계약 ({pending.length}건)
                </p>
              </div>
              <div className="space-y-2">
                {pending.map(c => (
                  <ContractCard key={c.id} contract={c} onClick={() => setSelected(c)} />
                ))}
              </div>
            </div>
          )}

          {others.length > 0 && (
            <div>
              <p className="text-sm font-semibold text-gray-500 mb-2">지난 계약</p>
              <div className="space-y-2">
                {others.map(c => (
                  <ContractCard key={c.id} contract={c} onClick={() => setSelected(c)} />
                ))}
              </div>
            </div>
          )}
        </>
      )}

      {selected && (
        <ContractSignModal
          contract={selected}
          onClose={() => setSelected(null)}
          onSigned={handleSigned}
        />
      )}
    </div>
  )
}
