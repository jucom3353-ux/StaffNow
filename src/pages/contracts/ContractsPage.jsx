import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import {
  FileCheck, X, Download, CheckCircle2, Clock,
  Building2, User, ClipboardList, Plus, ChevronRight,
  Loader2
} from 'lucide-react'
import Card from '../../components/ui/Card'
import EmptyState from '../../components/ui/EmptyState'
import { contractApi, jobApi, applicationApi } from '../../services/api'

const WAGE_LABEL = { HOURLY: '시급', DAILY: '일급', MONTHLY: '월급', FIXED: '고정급' }

const STATUS_META = {
  PENDING:   { label: '서명 대기', color: 'text-amber-600 bg-amber-50 border-amber-200' },
  SIGNED:    { label: '계약 완료', color: 'text-green-600 bg-green-50 border-green-200' },
  CANCELLED: { label: '취소됨',   color: 'text-red-600 bg-red-50 border-red-200' },
}

function calcWorkHours(startTime, endTime, breakMinutes) {
  if (!startTime || !endTime) return null
  const [h1, m1] = startTime.split(':').map(Number)
  const [h2, m2] = endTime.split(':').map(Number)
  const mins = (h2 * 60 + m2) - (h1 * 60 + m1) - (breakMinutes || 0)
  if (mins <= 0) return null
  const hh = Math.floor(mins / 60)
  const mm = mins % 60
  return mm > 0 ? `${hh}h ${mm}m` : `${hh}h`
}

// ── 계약서 보기 모달 ────────────────────────────────────────
function ContractViewModal({ contract, onClose }) {
  const statusMeta = STATUS_META[contract.status] ?? STATUS_META.PENDING
  const workHoursLabel = calcWorkHours(contract.startTime, contract.endTime, contract.breakTime)
  const wageLabel = WAGE_LABEL[contract.wageType] || '시급'
  const isSigned = contract.status === 'SIGNED'

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40" onClick={onClose}>
      <div
        className="bg-white rounded-2xl shadow-xl w-full max-w-lg mx-4 max-h-[90vh] overflow-y-auto"
        onClick={e => e.stopPropagation()}
      >
        <div className="flex items-center justify-between px-6 pt-6 pb-4 border-b border-offwhite-200">
          <div className="flex items-center gap-2.5">
            <div className="w-9 h-9 rounded-xl bg-green-50 flex items-center justify-center">
              <FileCheck size={17} className="text-green-600" />
            </div>
            <div>
              <h2 className="text-base font-bold text-navy">근로 계약서</h2>
              <p className="text-xs text-gray-400">{contract.jobPostTitle}</p>
            </div>
          </div>
          <button onClick={onClose} className="w-8 h-8 rounded-lg hover:bg-offwhite-100 flex items-center justify-center text-gray-400">
            <X size={16} />
          </button>
        </div>

        <div className="px-6 py-5 space-y-5 text-sm">
          <div className="grid grid-cols-2 gap-3">
            <div className="bg-offwhite-100 rounded-xl p-4">
              <div className="flex items-center gap-2 mb-2">
                <Building2 size={13} className="text-navy" />
                <span className="text-xs font-bold text-navy uppercase tracking-wider">사업주</span>
              </div>
              <p className="font-semibold text-navy text-sm">{contract.companyName}</p>
              {contract.workLocation && (
                <p className="text-xs text-gray-400 mt-0.5">{contract.workLocation}</p>
              )}
            </div>
            <div className="bg-offwhite-100 rounded-xl p-4">
              <div className="flex items-center gap-2 mb-2">
                <User size={13} className="text-navy" />
                <span className="text-xs font-bold text-navy uppercase tracking-wider">근로자</span>
              </div>
              <p className="font-semibold text-navy text-sm">{contract.workerName}</p>
            </div>
          </div>

          <div>
            <p className="text-xs font-bold text-navy uppercase tracking-wider mb-3">계약 조건</p>
            <div className="space-y-2">
              {[
                { label: '계약 기간', value: contract.contractStartDate && contract.contractEndDate ? `${contract.contractStartDate} ~ ${contract.contractEndDate}` : '—' },
                { label: '근무 시간', value: contract.startTime && contract.endTime ? `${contract.startTime} ~ ${contract.endTime}` : '—' },
                { label: '실 근무', value: workHoursLabel ?? '—' },
                { label: wageLabel, value: contract.wageAmount ? `₩${Number(contract.wageAmount).toLocaleString('ko-KR')}` : '—' },
                { label: '근무지', value: contract.workLocation ?? '—' },
                { label: '계약 형태', value: contract.workType ?? '단기 근로' },
              ].map(row => (
                <div key={row.label} className="flex justify-between py-2 border-b border-offwhite-100 last:border-0">
                  <span className="text-gray-400 text-xs">{row.label}</span>
                  <span className="font-semibold text-navy text-xs">{row.value}</span>
                </div>
              ))}
            </div>
          </div>

          <div className="bg-offwhite-100 rounded-xl px-4 py-3 text-xs text-gray-500 leading-relaxed space-y-1">
            <p>• 본 계약은 「근로기준법」에 따른 단기 근로 계약입니다.</p>
            <p>• 임금은 근로일 기준 익월 10일 이내 지급됩니다.</p>
            <p>• 근무 중 발생한 사고는 산업재해보상보험법에 따릅니다.</p>
            <p>• 무단 결근 시 해당 근무 임금은 지급되지 않습니다.</p>
          </div>

          {isSigned ? (
            <div className="flex items-center justify-between py-3 px-4 rounded-xl border border-green-200 bg-green-50">
              <div className="flex items-center gap-2">
                <CheckCircle2 size={16} className="text-green-600" />
                <span className="text-sm font-semibold text-green-700">양측 서명 완료</span>
              </div>
              <span className="text-xs text-green-500">{contract.companySignedAt?.substring(0, 10) ?? ''}</span>
            </div>
          ) : (
            <div className="flex items-center gap-2 py-3 px-4 rounded-xl border border-amber-200 bg-amber-50">
              <Clock size={15} className="text-amber-500" />
              <span className="text-sm font-semibold text-amber-700">{statusMeta.label}</span>
            </div>
          )}
        </div>

        <div className="px-6 pb-6 flex gap-2">
          <button
            onClick={onClose}
            className="flex-1 py-2.5 text-sm font-semibold text-gray-500 bg-offwhite-100 hover:bg-offwhite-200 rounded-xl transition-colors"
          >
            닫기
          </button>
          <button
            onClick={() => alert('데모 버전에서는 다운로드가 지원되지 않습니다.')}
            className="flex-1 py-2.5 text-sm font-semibold text-white bg-navy hover:bg-navy-700 rounded-xl transition-colors flex items-center justify-center gap-2"
          >
            <Download size={14} />PDF 다운로드
          </button>
        </div>
      </div>
    </div>
  )
}

// ── 계약서 발송 모달 ────────────────────────────────────────
function ContractCreateModal({ onClose, onCreate }) {
  const [jobs,         setJobs]         = useState([])
  const [jobsLoading,  setJobsLoading]  = useState(true)
  const [selectedJob,  setSelectedJob]  = useState(null)

  const [applicants,       setApplicants]       = useState([])
  const [applicantsLoading, setApplicantsLoading] = useState(false)
  const [selectedApplicant, setSelectedApplicant] = useState(null)

  const [form, setForm] = useState({
    contractStartDate: '',
    contractEndDate:   '',
    startTime:         '09:00',
    endTime:           '18:00',
    breakTime:         60,
    wageType:          'HOURLY',
    wageAmount:        '',
    workLocation:      '',
    workType:          '',
  })
  const [submitting, setSubmitting] = useState(false)
  const [submitError, setSubmitError] = useState('')

  useEffect(() => {
    jobApi.myList()
      .then(res => res.ok ? res.json() : [])
      .then(data => setJobs(Array.isArray(data) ? data : []))
      .catch(() => setJobs([]))
      .finally(() => setJobsLoading(false))
  }, [])

  function handleSelectJob(job) {
    setSelectedJob(job)
    setSelectedApplicant(null)
    setApplicants([])
    setForm(prev => ({
      ...prev,
      workLocation: job.workLocation ?? job.location ?? '',
      wageType:     job.wageType ?? 'HOURLY',
      wageAmount:   job.wageAmount ? String(job.wageAmount) : '',
    }))

    setApplicantsLoading(true)
    applicationApi.jobApplicants(job.id)
      .then(res => res.ok ? res.json() : [])
      .then(data => {
        const list = Array.isArray(data) ? data : []
        setApplicants(list.filter(a => a.status === 'APPROVED'))
      })
      .catch(() => setApplicants([]))
      .finally(() => setApplicantsLoading(false))
  }

  function patch(key, value) {
    setForm(prev => ({ ...prev, [key]: value }))
  }

  async function handleSubmit() {
    if (!selectedApplicant) return
    setSubmitting(true)
    setSubmitError('')
    try {
      const res = await contractApi.create({
        jobPostId:         Number(selectedJob.id),
        workerId:          selectedApplicant.workerId,
        contractStartDate: form.contractStartDate,
        contractEndDate:   form.contractEndDate,
      })
      if (!res.ok) {
        setSubmitError('계약서 발송에 실패했습니다. 다시 시도해 주세요.')
        return
      }
      const created = await res.json()
      onCreate(created)
      onClose()
    } catch {
      setSubmitError('네트워크 오류가 발생했습니다.')
    } finally {
      setSubmitting(false)
    }
  }

  const canSubmit =
    selectedApplicant &&
    form.contractStartDate &&
    form.contractEndDate &&
    form.startTime &&
    form.endTime &&
    form.wageAmount &&
    form.workLocation &&
    !submitting

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4" onClick={onClose}>
      <div
        className="bg-white rounded-2xl shadow-xl w-full max-w-lg max-h-[95vh] overflow-y-auto"
        onClick={e => e.stopPropagation()}
      >
        {/* 헤더 */}
        <div className="flex items-center justify-between px-6 pt-6 pb-4 border-b border-offwhite-200">
          <div className="flex items-center gap-2.5">
            <div className="w-9 h-9 rounded-xl bg-navy/10 flex items-center justify-center">
              <Plus size={17} className="text-navy" />
            </div>
            <h2 className="text-base font-bold text-navy">계약서 발송</h2>
          </div>
          <button onClick={onClose} className="w-8 h-8 rounded-lg hover:bg-offwhite-100 flex items-center justify-center text-gray-400">
            <X size={16} />
          </button>
        </div>

        <div className="px-6 py-5 space-y-5">

          {/* ── STEP 1: 공고 선택 ── */}
          <div>
            <p className="text-xs font-bold text-navy uppercase tracking-wider mb-2">
              1 — 공고 선택
            </p>
            {jobsLoading ? (
              <div className="flex items-center gap-2 py-3 text-sm text-gray-400">
                <Loader2 size={14} className="animate-spin" />불러오는 중...
              </div>
            ) : jobs.length === 0 ? (
              <p className="text-sm text-gray-400 py-2">등록된 공고가 없습니다.</p>
            ) : (
              <div className="space-y-1.5">
                {jobs.map(job => (
                  <button
                    key={job.id}
                    onClick={() => handleSelectJob(job)}
                    className={`w-full flex items-center justify-between px-4 py-3 rounded-xl border text-left transition-colors ${
                      selectedJob?.id === job.id
                        ? 'border-navy bg-navy/5 text-navy'
                        : 'border-offwhite-200 hover:border-navy/30 hover:bg-offwhite-100'
                    }`}
                  >
                    <div>
                      <p className="text-sm font-semibold text-navy">{job.title}</p>
                      <p className="text-xs text-gray-400 mt-0.5">
                        {job.workLocation ?? job.location ?? '—'}
                      </p>
                    </div>
                    {selectedJob?.id === job.id && (
                      <CheckCircle2 size={16} className="text-navy shrink-0" />
                    )}
                  </button>
                ))}
              </div>
            )}
          </div>

          {/* ── STEP 2: 합격자 선택 ── */}
          {selectedJob && (
            <div>
              <p className="text-xs font-bold text-navy uppercase tracking-wider mb-2">
                2 — 합격자 선택
              </p>
              {applicantsLoading ? (
                <div className="flex items-center gap-2 py-3 text-sm text-gray-400">
                  <Loader2 size={14} className="animate-spin" />불러오는 중...
                </div>
              ) : applicants.length === 0 ? (
                <p className="text-sm text-gray-400 py-2">
                  합격 처리된 지원자가 없습니다.
                </p>
              ) : (
                <div className="space-y-1.5">
                  {applicants.map(app => (
                    <button
                      key={app.id}
                      onClick={() => setSelectedApplicant(app)}
                      className={`w-full flex items-center justify-between px-4 py-3 rounded-xl border text-left transition-colors ${
                        selectedApplicant?.id === app.id
                          ? 'border-navy bg-navy/5'
                          : 'border-offwhite-200 hover:border-navy/30 hover:bg-offwhite-100'
                      }`}
                    >
                      <div className="flex items-center gap-3">
                        <div className="w-8 h-8 rounded-full bg-orange/10 flex items-center justify-center shrink-0">
                          <span className="text-sm font-bold text-orange">
                            {(app.user?.name ?? '?')[0]}
                          </span>
                        </div>
                        <div>
                          <p className="text-sm font-semibold text-navy">
                            {app.user?.name ?? '이름 없음'}
                          </p>
                          <p className="text-xs text-gray-400">{app.user?.email ?? ''}</p>
                        </div>
                      </div>
                      {selectedApplicant?.id === app.id && (
                        <CheckCircle2 size={16} className="text-navy shrink-0" />
                      )}
                    </button>
                  ))}
                </div>
              )}
            </div>
          )}

          {/* ── STEP 3: 계약 조건 ── */}
          {selectedApplicant && (
            <div>
              <p className="text-xs font-bold text-navy uppercase tracking-wider mb-3">
                3 — 계약 조건
              </p>
              <div className="space-y-3">

                {/* 계약 기간 */}
                <div className="grid grid-cols-2 gap-2">
                  <div>
                    <label className="text-xs text-gray-500 mb-1 block">계약 시작일</label>
                    <input
                      type="date"
                      value={form.contractStartDate}
                      onChange={e => patch('contractStartDate', e.target.value)}
                      className="w-full border border-offwhite-200 rounded-xl px-3 py-2.5 text-sm text-navy focus:outline-none focus:border-navy"
                    />
                  </div>
                  <div>
                    <label className="text-xs text-gray-500 mb-1 block">계약 종료일</label>
                    <input
                      type="date"
                      value={form.contractEndDate}
                      onChange={e => patch('contractEndDate', e.target.value)}
                      className="w-full border border-offwhite-200 rounded-xl px-3 py-2.5 text-sm text-navy focus:outline-none focus:border-navy"
                    />
                  </div>
                </div>

                {/* 근무 시간 */}
                <div className="grid grid-cols-3 gap-2">
                  <div>
                    <label className="text-xs text-gray-500 mb-1 block">근무 시작</label>
                    <input
                      type="time"
                      value={form.startTime}
                      onChange={e => patch('startTime', e.target.value)}
                      className="w-full border border-offwhite-200 rounded-xl px-3 py-2.5 text-sm text-navy focus:outline-none focus:border-navy"
                    />
                  </div>
                  <div>
                    <label className="text-xs text-gray-500 mb-1 block">근무 종료</label>
                    <input
                      type="time"
                      value={form.endTime}
                      onChange={e => patch('endTime', e.target.value)}
                      className="w-full border border-offwhite-200 rounded-xl px-3 py-2.5 text-sm text-navy focus:outline-none focus:border-navy"
                    />
                  </div>
                  <div>
                    <label className="text-xs text-gray-500 mb-1 block">휴게(분)</label>
                    <input
                      type="number"
                      min="0"
                      value={form.breakTime}
                      onChange={e => patch('breakTime', e.target.value)}
                      className="w-full border border-offwhite-200 rounded-xl px-3 py-2.5 text-sm text-navy focus:outline-none focus:border-navy"
                    />
                  </div>
                </div>

                {/* 임금 */}
                <div className="grid grid-cols-2 gap-2">
                  <div>
                    <label className="text-xs text-gray-500 mb-1 block">임금 형태</label>
                    <select
                      value={form.wageType}
                      onChange={e => patch('wageType', e.target.value)}
                      className="w-full border border-offwhite-200 rounded-xl px-3 py-2.5 text-sm text-navy focus:outline-none focus:border-navy bg-white"
                    >
                      {Object.entries(WAGE_LABEL).map(([k, v]) => (
                        <option key={k} value={k}>{v}</option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <label className="text-xs text-gray-500 mb-1 block">
                      {WAGE_LABEL[form.wageType] ?? '금액'} (원)
                    </label>
                    <input
                      type="number"
                      min="0"
                      placeholder="10320"
                      value={form.wageAmount}
                      onChange={e => patch('wageAmount', e.target.value)}
                      className="w-full border border-offwhite-200 rounded-xl px-3 py-2.5 text-sm text-navy focus:outline-none focus:border-navy"
                    />
                  </div>
                </div>

                {/* 근무지 */}
                <div>
                  <label className="text-xs text-gray-500 mb-1 block">근무지</label>
                  <input
                    type="text"
                    placeholder="예: 롯데마트 잠실점"
                    value={form.workLocation}
                    onChange={e => patch('workLocation', e.target.value)}
                    className="w-full border border-offwhite-200 rounded-xl px-3 py-2.5 text-sm text-navy focus:outline-none focus:border-navy"
                  />
                </div>

                {/* 직종 */}
                <div>
                  <label className="text-xs text-gray-500 mb-1 block">직종 / 업무내용</label>
                  <input
                    type="text"
                    placeholder="예: 축산시식행사 홍보"
                    value={form.workType}
                    onChange={e => patch('workType', e.target.value)}
                    className="w-full border border-offwhite-200 rounded-xl px-3 py-2.5 text-sm text-navy focus:outline-none focus:border-navy"
                  />
                </div>
              </div>
            </div>
          )}

          {submitError && (
            <p className="text-sm text-red-500 bg-red-50 border border-red-200 rounded-xl px-4 py-2.5">
              {submitError}
            </p>
          )}
        </div>

        {/* 하단 버튼 */}
        <div className="px-6 pb-6 flex gap-2">
          <button
            onClick={onClose}
            className="flex-1 py-2.5 text-sm font-semibold text-gray-500 bg-offwhite-100 hover:bg-offwhite-200 rounded-xl transition-colors"
          >
            취소
          </button>
          <button
            onClick={handleSubmit}
            disabled={!canSubmit}
            className="flex-1 py-2.5 text-sm font-semibold text-white bg-navy hover:bg-navy-700 rounded-xl transition-colors disabled:opacity-40 flex items-center justify-center gap-2"
          >
            {submitting
              ? <><Loader2 size={14} className="animate-spin" />발송 중...</>
              : <><ChevronRight size={14} />계약서 발송</>
            }
          </button>
        </div>
      </div>
    </div>
  )
}

// ── 메인 페이지 ─────────────────────────────────────────────
export default function ContractsPage() {
  const [contracts,        setContracts]        = useState([])
  const [loading,          setLoading]          = useState(true)
  const [selectedContract, setSelectedContract] = useState(null)
  const [showCreate,       setShowCreate]       = useState(false)

  useEffect(() => {
    contractApi.myList()
      .then(res => res.ok ? res.json() : [])
      .then(data => setContracts(Array.isArray(data) ? data : []))
      .catch(() => setContracts([]))
      .finally(() => setLoading(false))
  }, [])

  function handleCreated(newContract) {
    setContracts(prev => [newContract, ...prev])
  }

  return (
    <div className="space-y-5">
      <div className="flex items-start justify-between">
        <div>
          <h1 className="text-xl font-bold text-navy">계약 상태</h1>
          <p className="text-sm text-gray-500 mt-0.5">총 {contracts.length}건의 계약</p>
        </div>
        <div className="flex items-center gap-2">
          <button
            onClick={() => setShowCreate(true)}
            className="flex items-center gap-2 px-4 py-2 rounded-xl bg-navy text-white text-sm font-semibold hover:bg-navy-700 transition-colors"
          >
            <Plus size={14} />계약서 발송
          </button>
          <Link
            to="/attendance"
            className="flex items-center gap-2 px-4 py-2 rounded-xl border border-offwhite-200 text-navy text-sm font-semibold hover:bg-offwhite-100 transition-colors"
          >
            <ClipboardList size={14} />근태 관리
          </Link>
        </div>
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
            description="합격 처리된 지원자에게 계약서를 발송하세요"
            action={{ label: '계약서 발송', onClick: () => setShowCreate(true) }}
          />
        </Card>
      ) : (
        <>
          {/* 모바일 카드 뷰 */}
          <div className="md:hidden space-y-2">
            {contracts.map(c => {
              const meta = STATUS_META[c.status] ?? STATUS_META.PENDING
              const workHoursLabel = calcWorkHours(c.startTime, c.endTime, c.breakTime)
              return (
                <Card key={c.id} padding={false}>
                  <div className="p-4">
                    <div className="flex items-start justify-between gap-2 mb-2">
                      <div>
                        <p className="font-semibold text-navy">{c.workerName}</p>
                        <p className="text-xs text-gray-400 mt-0.5">{c.jobPostTitle}</p>
                      </div>
                      <span className={`shrink-0 inline-flex text-xs font-semibold px-2.5 py-1 rounded-full border ${meta.color}`}>
                        {meta.label}
                      </span>
                    </div>
                    <p className="text-xs text-gray-500 truncate mb-3">
                      {c.contractStartDate} ~ {c.contractEndDate}
                    </p>
                    <div className="flex items-center justify-between pt-2.5 border-t border-offwhite-100">
                      <div>
                        <p className="font-semibold text-navy tabular-nums">
                          {c.wageAmount ? `₩${Number(c.wageAmount).toLocaleString('ko-KR')}` : '—'}
                        </p>
                        <p className="text-xs text-gray-400 mt-0.5">{workHoursLabel ?? '—'}</p>
                      </div>
                      <button
                        onClick={() => setSelectedContract(c)}
                        className="text-xs font-semibold text-navy bg-offwhite-100 hover:bg-navy hover:text-white border border-offwhite-200 hover:border-navy px-3 py-2 rounded-lg transition-colors"
                      >
                        계약서 보기
                      </button>
                    </div>
                  </div>
                </Card>
              )
            })}
          </div>

          {/* 데스크탑 테이블 */}
          <Card padding={false} className="hidden md:block">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-offwhite-200 bg-offwhite-100">
                  <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">근로자</th>
                  <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">공고</th>
                  <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">계약 기간</th>
                  <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">금액</th>
                  <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">상태</th>
                  <th className="px-5 py-3" />
                </tr>
              </thead>
              <tbody>
                {contracts.map(c => {
                  const meta = STATUS_META[c.status] ?? STATUS_META.PENDING
                  return (
                    <tr key={c.id} className="border-b border-offwhite-100 last:border-0 hover:bg-offwhite-100 transition-colors">
                      <td className="px-5 py-3.5">
                        <p className="font-semibold text-navy">{c.workerName}</p>
                      </td>
                      <td className="px-5 py-3.5 text-gray-600">{c.jobPostTitle}</td>
                      <td className="px-5 py-3.5 text-gray-600 tabular-nums text-xs">
                        {c.contractStartDate} ~ {c.contractEndDate}
                      </td>
                      <td className="px-5 py-3.5 font-semibold text-navy tabular-nums">
                        {c.wageAmount ? `₩${Number(c.wageAmount).toLocaleString('ko-KR')}` : '—'}
                      </td>
                      <td className="px-5 py-3.5">
                        <span className={`inline-flex text-xs font-semibold px-2.5 py-1 rounded-full border ${meta.color}`}>
                          {meta.label}
                        </span>
                      </td>
                      <td className="px-5 py-3.5">
                        <button
                          onClick={() => setSelectedContract(c)}
                          className="text-xs font-semibold text-navy bg-offwhite-100 hover:bg-navy hover:text-white border border-offwhite-200 hover:border-navy px-3 py-1.5 rounded-lg transition-colors"
                        >
                          계약서 보기
                        </button>
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </Card>
        </>
      )}

      {selectedContract && (
        <ContractViewModal contract={selectedContract} onClose={() => setSelectedContract(null)} />
      )}

      {showCreate && (
        <ContractCreateModal
          onClose={() => setShowCreate(false)}
          onCreate={handleCreated}
        />
      )}
    </div>
  )
}
