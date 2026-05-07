import { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { ChevronLeft, AlertCircle, Lock } from 'lucide-react'
import Card from '../../components/ui/Card'
import Button from '../../components/ui/Button'
import EmptyState from '../../components/ui/EmptyState'
import { useAppData } from '../../context/AppDataContext'

const CURRENT_USER = '김운영'

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

export default function JobEditPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { jobs, updateJob } = useAppData()

  const job = jobs.find(j => j.id === id)

  const [form, setForm] = useState(() => ({
    title: job?.title ?? '',
    location: job?.location ?? '',
    headcount: job?.headcount ?? '',
    wage: job?.wage ?? '',
    wageType: job?.wageType ?? 'hourly',
    description: job?.description ?? '',
    requirements: job?.requirements ?? '',
  }))
  const [errors, setErrors] = useState({})
  const [submitting, setSubmitting] = useState(false)

  if (!job) return (
    <EmptyState icon={AlertCircle} title="공고를 찾을 수 없습니다" action={{ label: '목록으로', to: '/jobs' }} />
  )

  if (job.createdBy !== CURRENT_USER) return (
    <div className="max-w-2xl">
      <EmptyState
        icon={Lock}
        title="수정 권한이 없습니다"
        description={`이 공고는 ${job.createdBy}님이 작성한 공고입니다`}
        action={{ label: '공고로 돌아가기', to: `/jobs/${id}` }}
      />
    </div>
  )

  const set = (key) => (e) => setForm(prev => ({ ...prev, [key]: e.target.value }))

  function validate() {
    const e = {}
    if (!form.title.trim())    e.title    = '공고 제목을 입력하세요'
    if (!form.location.trim()) e.location = '근무 지역을 입력하세요'
    if (!form.headcount || Number(form.headcount) < 1) e.headcount = '모집 인원을 1명 이상 입력하세요'
    if (!form.wage.trim())     e.wage     = '급여를 입력하세요'
    return e
  }

  async function handleSubmit(e) {
    e.preventDefault()
    const errs = validate()
    if (Object.keys(errs).length) { setErrors(errs); return }
    setSubmitting(true)
    await new Promise(r => setTimeout(r, 400))
    updateJob(id, form)
    navigate(`/jobs/${id}`, { state: { updated: true } })
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
          <h1 className="text-xl font-bold text-navy">공고 수정</h1>
          <p className="text-xs text-gray-400 mt-0.5">{job.title}</p>
        </div>
      </div>

      <form onSubmit={handleSubmit} noValidate className="space-y-4">
        {/* 기본 정보 */}
        <Card
          header={
            <div className="flex items-center gap-2">
              <span className="w-1 h-4 bg-orange rounded-full" />
              <span className="text-sm font-bold text-navy">기본 정보</span>
            </div>
          }
        >
          <div className="space-y-4">
            <FormField label="공고 제목" required error={errors.title}>
              <input
                className={inputCls(errors.title)}
                placeholder="예: 5월 행사 안내 스태프 모집"
                value={form.title}
                onChange={set('title')}
                onFocus={() => setErrors(p => ({ ...p, title: '' }))}
              />
            </FormField>

            <div className="grid grid-cols-2 gap-4">
              <FormField label="근무 지역" required error={errors.location}>
                <input
                  className={inputCls(errors.location)}
                  placeholder="예: 서울 강남구"
                  value={form.location}
                  onChange={set('location')}
                  onFocus={() => setErrors(p => ({ ...p, location: '' }))}
                />
              </FormField>
              <FormField label="모집 인원" required error={errors.headcount}>
                <div className="relative">
                  <input
                    type="number"
                    min="1"
                    className={inputCls(errors.headcount) + ' pr-8'}
                    placeholder="0"
                    value={form.headcount}
                    onChange={set('headcount')}
                    onFocus={() => setErrors(p => ({ ...p, headcount: '' }))}
                  />
                  <span className="absolute right-3 top-1/2 -translate-y-1/2 text-xs text-gray-400 pointer-events-none">명</span>
                </div>
              </FormField>
            </div>
          </div>
        </Card>

        {/* 급여 정보 */}
        <Card
          header={
            <div className="flex items-center gap-2">
              <span className="w-1 h-4 bg-orange rounded-full" />
              <span className="text-sm font-bold text-navy">급여 정보</span>
            </div>
          }
        >
          <div className="space-y-4">
            <div className="grid grid-cols-3 gap-3">
              {[
                { value: 'hourly', label: '시급' },
                { value: 'daily',  label: '일급' },
                { value: 'fixed',  label: '고정급' },
              ].map(opt => (
                <label
                  key={opt.value}
                  className={`flex items-center justify-center py-2 rounded-lg border cursor-pointer text-sm font-medium transition-all
                    ${form.wageType === opt.value
                      ? 'border-orange bg-orange-50 text-orange'
                      : 'border-offwhite-200 text-gray-500 hover:border-navy-100'}`}
                >
                  <input
                    type="radio"
                    name="wageType"
                    value={opt.value}
                    checked={form.wageType === opt.value}
                    onChange={set('wageType')}
                    className="hidden"
                  />
                  {opt.label}
                </label>
              ))}
            </div>
            <FormField label="급여" required error={errors.wage}>
              <div className="relative">
                <span className="absolute left-3 top-1/2 -translate-y-1/2 text-sm text-gray-400 pointer-events-none">₩</span>
                <input
                  type="text"
                  className={inputCls(errors.wage) + ' pl-7'}
                  placeholder="예: 12,000"
                  value={form.wage}
                  onChange={set('wage')}
                  onFocus={() => setErrors(p => ({ ...p, wage: '' }))}
                />
              </div>
            </FormField>
          </div>
        </Card>

        {/* 상세 정보 */}
        <Card
          header={
            <div className="flex items-center gap-2">
              <span className="w-1 h-4 bg-navy-200 rounded-full" />
              <span className="text-sm font-bold text-navy">상세 내용 <span className="text-xs font-normal text-gray-400">(선택)</span></span>
            </div>
          }
        >
          <div className="space-y-4">
            <FormField label="업무 내용">
              <textarea
                rows={3}
                className="w-full border border-offwhite-200 rounded-lg px-3 py-2.5 text-sm outline-none focus:border-navy hover:border-navy-200 transition-colors resize-none"
                placeholder="담당 업무를 구체적으로 입력하세요"
                value={form.description}
                onChange={set('description')}
              />
            </FormField>
            <FormField label="지원 조건">
              <textarea
                rows={2}
                className="w-full border border-offwhite-200 rounded-lg px-3 py-2.5 text-sm outline-none focus:border-navy hover:border-navy-200 transition-colors resize-none"
                placeholder="우대 조건이나 필수 조건을 입력하세요"
                value={form.requirements}
                onChange={set('requirements')}
              />
            </FormField>
          </div>
        </Card>

        {/* 버튼 */}
        <div className="flex gap-3 justify-end pt-1">
          <Button type="button" variant="secondary" onClick={() => navigate(-1)}>
            취소
          </Button>
          <Button type="submit" loading={submitting}>
            {submitting ? '저장 중...' : '수정 완료'}
          </Button>
        </div>
      </form>
    </div>
  )
}
