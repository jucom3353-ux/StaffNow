import { useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { ChevronLeft, AlertCircle } from 'lucide-react'
import Card from '../../components/ui/Card'
import Button from '../../components/ui/Button'
import WheelPicker from '../../components/ui/WheelPicker'
import { useAppData } from '../../context/AppDataContext'

const INITIAL = {
  jobId: '',
  date: '',
  startTime: '',
  endTime: '',
  requiredStaff: '',
  memo: '',
}

function FieldError({ msg }) {
  if (!msg) return null
  return (
    <p className="flex items-center gap-1 text-xs text-red-500 mt-1">
      <AlertCircle size={11} />
      {msg}
    </p>
  )
}

function FormField({ label, required, hint, error, children }) {
  return (
    <div>
      <label className="block text-sm font-semibold text-navy mb-1.5">
        {label}
        {required && <span className="text-orange ml-0.5">*</span>}
        {hint && <span className="text-xs font-normal text-gray-400 ml-2">{hint}</span>}
      </label>
      {children}
      <FieldError msg={error} />
    </div>
  )
}

const inputCls = (err) =>
  `w-full border rounded-lg px-3 py-2.5 text-sm outline-none transition-colors bg-white
  ${err ? 'border-red-400 focus:border-red-500' : 'border-offwhite-200 focus:border-navy hover:border-navy-200'}`

export default function ShiftCreatePage() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const { jobs, addShift } = useAppData()

  const preselectedJobId = searchParams.get('jobId') ?? ''
  const [form, setForm] = useState({ ...INITIAL, jobId: preselectedJobId })
  const [errors, setErrors] = useState({})
  const [submitting, setSubmitting] = useState(false)

  const activeJobs = jobs.filter(j => j.status === 'active')

  const set = (key) => (e) => setForm(prev => ({ ...prev, [key]: e.target.value }))

  function validate() {
    const e = {}
    if (!form.jobId)               e.jobId         = '연결할 공고를 선택하세요'
    if (!form.date)                e.date          = '날짜를 선택하세요'
    if (!form.startTime)           e.startTime     = '시작 시간을 입력하세요'
    if (!form.endTime)             e.endTime       = '종료 시간을 입력하세요'
    if (form.startTime && form.endTime && form.startTime >= form.endTime)
                                   e.endTime       = '종료 시간은 시작 시간 이후여야 합니다'
    if (!form.requiredStaff || Number(form.requiredStaff) < 1)
                                   e.requiredStaff = '필요 인원을 1명 이상 입력하세요'
    return e
  }

  async function handleSubmit(e) {
    e.preventDefault()
    const errs = validate()
    if (Object.keys(errs).length) { setErrors(errs); return }
    setSubmitting(true)
    await new Promise(r => setTimeout(r, 600))
    const newShift = addShift(form)
    navigate(`/shifts/${newShift.id}`)
  }

  return (
    <div className="max-w-2xl space-y-5">
      {/* 헤더 */}
      <div className="flex items-center gap-3">
        <button
          onClick={() => navigate(-1)}
          className="w-8 h-8 rounded-lg border border-offwhite-200 flex items-center justify-center text-gray-500 hover:bg-offwhite-100 transition-colors"
        >
          <ChevronLeft size={18} />
        </button>
        <div>
          <h1 className="text-xl font-bold text-navy">Shift 생성</h1>
          <p className="text-xs text-gray-400 mt-0.5">날짜와 시간을 지정해 근무 일정을 등록하세요</p>
        </div>
      </div>

      <form onSubmit={handleSubmit} noValidate className="space-y-4">
        {/* 공고 연결 */}
        <Card
          header={
            <div className="flex items-center gap-2">
              <span className="w-1 h-4 bg-orange rounded-full" />
              <span className="text-sm font-bold text-navy">공고 연결</span>
            </div>
          }
        >
          <FormField label="연결 공고" required error={errors.jobId}>
            {activeJobs.length === 0 ? (
              <div className="rounded-lg bg-offwhite-100 border border-offwhite-200 px-4 py-3 text-sm text-gray-500">
                진행 중인 공고가 없습니다.{' '}
                <button
                  type="button"
                  className="text-orange font-medium hover:underline"
                  onClick={() => navigate('/jobs/create')}
                >
                  공고를 먼저 생성하세요
                </button>
              </div>
            ) : (
              <div className="space-y-2">
                {activeJobs.map(job => (
                  <label
                    key={job.id}
                    className={`flex items-center gap-3 p-3 rounded-lg border cursor-pointer transition-all
                      ${form.jobId === job.id
                        ? 'border-orange bg-orange-50'
                        : 'border-offwhite-200 hover:border-navy-100 hover:bg-offwhite-100'}`}
                  >
                    <input
                      type="radio"
                      name="jobId"
                      value={job.id}
                      checked={form.jobId === job.id}
                      onChange={set('jobId')}
                      className="hidden"
                    />
                    <div className={`w-4 h-4 rounded-full border-2 flex items-center justify-center shrink-0 transition-all
                      ${form.jobId === job.id ? 'border-orange' : 'border-gray-300'}`}>
                      {form.jobId === job.id && (
                        <div className="w-2 h-2 rounded-full bg-orange" />
                      )}
                    </div>
                    <div className="flex-1 min-w-0">
                      <p className="text-sm font-semibold text-navy truncate">{job.title}</p>
                      <p className="text-xs text-gray-400 mt-0.5">{job.location}</p>
                    </div>
                  </label>
                ))}
              </div>
            )}
          </FormField>
        </Card>

        {/* 일정 정보 */}
        <Card
          header={
            <div className="flex items-center gap-2">
              <span className="w-1 h-4 bg-orange rounded-full" />
              <span className="text-sm font-bold text-navy">일정 정보</span>
            </div>
          }
        >
          <div className="space-y-4">
            <FormField label="근무 날짜" required error={errors.date}>
              <input
                type="date"
                className={inputCls(errors.date)}
                value={form.date}
                onChange={set('date')}
                onFocus={() => setErrors(p => ({ ...p, date: '' }))}
                min={new Date().toISOString().slice(0, 10)}
              />
            </FormField>

            <div className="flex items-end gap-3">
              <FormField label="시작 시간" required error={errors.startTime} className="flex-1">
                <WheelPicker
                  value={form.startTime}
                  onChange={v => { setForm(p => ({ ...p, startTime: v })); setErrors(p => ({ ...p, startTime: '', endTime: '' })) }}
                  label="시작 시간"
                />
              </FormField>
              <span className="text-gray-400 font-bold text-lg pb-2">~</span>
              <FormField label="종료 시간" required error={errors.endTime} className="flex-1">
                <WheelPicker
                  value={form.endTime}
                  onChange={v => { setForm(p => ({ ...p, endTime: v })); setErrors(p => ({ ...p, endTime: '' })) }}
                  label="종료 시간"
                />
              </FormField>
            </div>

            {form.startTime && form.endTime && form.startTime < form.endTime && (
              <div className="flex items-center gap-2 bg-navy-50 rounded-lg px-3 py-2">
                <span className="text-xs text-navy font-medium">근무 시간</span>
                <span className="text-xs text-navy-200">·</span>
                <span className="text-xs font-bold text-orange tabular-nums">
                  {(() => {
                    const [sh, sm] = form.startTime.split(':').map(Number)
                    const [eh, em] = form.endTime.split(':').map(Number)
                    const total = (eh * 60 + em) - (sh * 60 + sm)
                    return `${Math.floor(total / 60)}시간 ${total % 60 > 0 ? `${total % 60}분` : ''}`
                  })()}
                </span>
              </div>
            )}

            <FormField label="필요 인원" required error={errors.requiredStaff}>
              <div className="relative">
                <input
                  type="number"
                  min="1"
                  className={inputCls(errors.requiredStaff) + ' pr-8'}
                  placeholder="0"
                  value={form.requiredStaff}
                  onChange={set('requiredStaff')}
                  onFocus={() => setErrors(p => ({ ...p, requiredStaff: '' }))}
                />
                <span className="absolute right-3 top-1/2 -translate-y-1/2 text-xs text-gray-400 pointer-events-none">명</span>
              </div>
            </FormField>
          </div>
        </Card>

        {/* 메모 */}
        <Card
          header={
            <div className="flex items-center gap-2">
              <span className="w-1 h-4 bg-navy-200 rounded-full" />
              <span className="text-sm font-bold text-navy">메모 <span className="text-xs font-normal text-gray-400">(선택)</span></span>
            </div>
          }
        >
          <textarea
            rows={2}
            className="w-full border border-offwhite-200 rounded-lg px-3 py-2.5 text-sm outline-none focus:border-navy hover:border-navy-200 transition-colors resize-none"
            placeholder="특이사항이나 전달 사항을 입력하세요"
            value={form.memo}
            onChange={set('memo')}
          />
        </Card>

        <div className="flex gap-3 justify-end pt-1">
          <Button type="button" variant="secondary" onClick={() => navigate(-1)}>
            취소
          </Button>
          <Button type="submit" loading={submitting}>
            {submitting ? '생성 중...' : 'Shift 등록하기'}
          </Button>
        </div>
      </form>
    </div>
  )
}
