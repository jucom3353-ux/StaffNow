import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { ChevronLeft, AlertCircle, ChevronRight, X, Info } from 'lucide-react'
import Card from '../../components/ui/Card'
import Button from '../../components/ui/Button'
import WheelPicker from '../../components/ui/WheelPicker'
import { useAppData } from '../../context/AppDataContext'
import { useAuth } from '../../context/AuthContext'
import { LOCATION_DATA } from '../../data/locationData'

// ─── 상수 ──────────────────────────────────────────────
const MIN_HOURLY = 10320
const MIN_DAILY  = MIN_HOURLY * 8   // 82,560 (8시간 기준)

const JOB_CATEGORIES = [
  '판촉 업무', '마케팅', '단순 노무', '행사 보조',
  '안내/접수', '서빙', '주방 보조', '물류/배송',
  '고객 응대', '전시/부스', '스포츠/레저', '교육 보조',
]

const REQUIRED_GROUPS = [
  {
    label: '성격',
    chips: ['꼼꼼한 분을 원해요', '활발하고 외향적인 분을 원해요', '차분하고 침착한 분을 원해요', '팀워크를 중시하는 분을 원해요'],
  },
  {
    label: '나이',
    chips: ['20대 선호', '30대 선호', '나이 무관'],
  },
  {
    label: '필수 조건',
    chips: ['경력 무관', '대졸 이상', '운전면허 소지자', '단정한 외모 및 복장', '시간 약속을 잘 지키는 분'],
  },
]

const PREFERRED_GROUPS = [
  {
    label: '경험',
    chips: ['관련 업무 유경험자 우대', '행사/이벤트 경험자 우대', '서비스직 경험자 우대'],
  },
  {
    label: '언어/기술',
    chips: ['영어 가능자 우대', '일본어 가능자 우대', '포토샵 활용 가능자 우대', 'SNS 운영 경험자 우대'],
  },
  {
    label: '기타',
    chips: ['포트폴리오 제출 가능자', '즉시 출근 가능자 우대', '장기 근무 가능자 우대'],
  },
]

// ─── 헬퍼 ──────────────────────────────────────────────
function numVal(str) {
  return parseInt(str.replace(/,/g, ''), 10) || 0
}
function fmtNum(n) {
  return n ? n.toLocaleString('ko-KR') : ''
}
function FieldError({ msg }) {
  if (!msg) return null
  return (
    <p className="flex items-center gap-1 text-xs text-red-500 mt-1.5">
      <AlertCircle size={11} className="shrink-0" />{msg}
    </p>
  )
}
function SectionHeader({ accent = 'orange', children }) {
  return (
    <div className="flex items-center gap-2">
      <span className={`w-1 h-4 rounded-full ${accent === 'orange' ? 'bg-orange' : 'bg-navy-200'}`} />
      <span className="text-sm font-bold text-navy">{children}</span>
    </div>
  )
}
function Chip({ label, selected, onClick }) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={`text-xs font-medium px-3 py-1.5 rounded-full border transition-all ${
        selected
          ? 'bg-orange text-white border-orange'
          : 'bg-white text-gray-600 border-offwhite-200 hover:border-orange hover:text-orange'
      }`}
    >
      {label}
    </button>
  )
}

// ─── 메인 컴포넌트 ──────────────────────────────────────
export default function JobCreatePage() {
  const navigate = useNavigate()
  const { addJob } = useAppData()
  const { user } = useAuth()

  // 위치
  const [sido, setSido]       = useState('')
  const [sigungu, setSigungu] = useState('')
  const [dong, setDong]       = useState('')
  const [detailAddr, setDetailAddr] = useState('')

  // 기본
  const [title, setTitle]         = useState('')
  const [headcount, setHeadcount] = useState('')

  // 근무 시간
  const [startTime, setStartTime] = useState('')
  const [endTime, setEndTime]     = useState('')
  const [breakMin, setBreakMin]   = useState('60')

  // 급여
  const [wageType, setWageType] = useState('hourly')
  const [wageRaw, setWageRaw]   = useState('')
  const [wageError, setWageError] = useState('')
  const [wageWarning, setWageWarning] = useState('')

  // 카테고리
  const [categories, setCategories] = useState([])

  // 업무 내용
  const [description, setDescription] = useState('')

  // 지원 조건 탭
  const [reqTab, setReqTab]         = useState('required')
  const [requiredChips, setRequiredChips]   = useState([])
  const [preferredChips, setPreferredChips] = useState([])

  const [errors, setErrors]     = useState({})
  const [submitting, setSubmitting] = useState(false)

  // ── 위치 핸들러 ──
  function selectSido(v)    { setSido(v); setSigungu(''); setDong(''); setDetailAddr('') }
  function selectSigungu(v) { setSigungu(v); setDong(''); setDetailAddr('') }
  function selectDong(v)    { setDong(v) }

  const locationStr = [sido, sigungu, dong, detailAddr].filter(Boolean).join(' ')

  // 근무 시간 계산
  const totalWorkHours = (() => {
    if (!startTime || !endTime) return null
    const [sh, sm] = startTime.split(':').map(Number)
    const [eh, em] = endTime.split(':').map(Number)
    const totalMin = (eh * 60 + em) - (sh * 60 + sm)
    if (totalMin <= 0) return null
    const workMin = totalMin - Number(breakMin)
    const h = Math.floor(workMin / 60)
    const m = workMin % 60
    return m > 0 ? `${h}시간 ${m}분` : `${h}시간`
  })()

  // ── 급여 검증 ──
  function validateWage(raw, type) {
    const n = numVal(raw)
    if (!n) { setWageError(''); setWageWarning(''); return }
    if (type === 'hourly') {
      if (n < MIN_HOURLY) {
        setWageError(`최저임금 미만입니다. 최소 ${fmtNum(MIN_HOURLY)}원 이상 입력하세요.`)
        setWageWarning('')
      } else {
        setWageError('')
        setWageWarning('')
      }
    } else if (type === 'daily') {
      if (n < MIN_DAILY) {
        setWageError(`일급 기준 최소 ${fmtNum(MIN_DAILY)}원 이상이어야 합니다. (시급 ${fmtNum(MIN_HOURLY)}원 × 8시간)`)
        setWageWarning('')
      } else {
        setWageError('')
        setWageWarning('')
      }
    } else {
      // 고정급 — 환산 안내만
      setWageError('')
      const monthly = n
      const perHour = Math.round(monthly / 209)  // 주 40h 기준 월 209시간
      if (perHour < MIN_HOURLY) {
        setWageWarning(`월 ${fmtNum(monthly)}원은 시급 환산 약 ${fmtNum(perHour)}원으로, 최저임금(${fmtNum(MIN_HOURLY)}원)에 미달할 수 있습니다.`)
      } else {
        setWageWarning('')
      }
    }
  }

  function handleWageChange(e) {
    const digits = e.target.value.replace(/[^0-9]/g, '')
    const n = parseInt(digits, 10) || 0
    setWageRaw(n ? fmtNum(n) : '')
    validateWage(digits, wageType)
    setErrors(p => ({ ...p, wage: '' }))
  }

  function handleWageTypeChange(type) {
    setWageType(type)
    validateWage(wageRaw, type)
  }

  // ── 카테고리 ──
  function toggleCategory(cat) {
    setCategories(prev =>
      prev.includes(cat) ? prev.filter(c => c !== cat) : [...prev, cat]
    )
  }

  // ── 조건 칩 ──
  function toggleChip(chip, isRequired) {
    if (isRequired) {
      setRequiredChips(prev => prev.includes(chip) ? prev.filter(c => c !== chip) : [...prev, chip])
    } else {
      setPreferredChips(prev => prev.includes(chip) ? prev.filter(c => c !== chip) : [...prev, chip])
    }
  }

  // ── 유효성 검사 ──
  function validate() {
    const e = {}
    if (!title.trim())      e.title      = '공고 제목을 입력하세요'
    if (!locationStr)       e.location   = '근무 지역을 선택하세요'
    if (!startTime)         e.startTime  = '근무 시작 시간을 입력하세요'
    if (!endTime)           e.endTime    = '근무 종료 시간을 입력하세요'
    if (startTime && endTime && totalWorkHours === null) e.endTime = '종료 시간이 시작 시간보다 늦어야 합니다'
    if (!headcount || Number(headcount) < 1) e.headcount = '모집 인원을 1명 이상 입력하세요'
    if (!wageRaw.trim())    e.wage       = '급여를 입력하세요'
    if (wageError)          e.wage       = wageError
    return e
  }

  async function handleSubmit(e) {
    e.preventDefault()
    const errs = validate()
    if (Object.keys(errs).length) { setErrors(errs); return }
    setSubmitting(true)
    await new Promise(r => setTimeout(r, 600))
    const wageLabel =
      wageType === 'hourly' ? `시급 ${wageRaw}원` :
      wageType === 'daily'  ? `일급 ${wageRaw}원` :
      `월 ${wageRaw}원`
    const newJob = addJob({
      title,
      location: locationStr,
      createdBy: user?.name,
      headcount,
      wage: wageLabel,
      wageType,
      description: [
        categories.length ? `[업무 유형] ${categories.join(', ')}` : '',
        description,
      ].filter(Boolean).join('\n\n'),
      requirements: [
        requiredChips.length   ? `[필수] ${requiredChips.join(' / ')}` : '',
        preferredChips.length  ? `[우대] ${preferredChips.join(' / ')}` : '',
      ].filter(Boolean).join('\n'),
    })
    navigate(`/jobs/${newJob.id}`, { state: { created: true } })
  }

  const inputCls = (err) =>
    `w-full border rounded-lg px-3 py-2.5 text-sm outline-none transition-colors bg-white
    ${err ? 'border-red-400 focus:border-red-500' : 'border-offwhite-200 focus:border-navy hover:border-navy-200'}`

  // ── 렌더 ──────────────────────────────────────────────
  return (
    <div className="max-w-2xl space-y-5">
      {/* 헤더 */}
      <div className="flex items-center gap-3">
        <button
          type="button"
          onClick={() => navigate(-1)}
          className="w-8 h-8 rounded-lg border border-offwhite-200 flex items-center justify-center text-gray-500 hover:bg-offwhite-100 transition-colors"
        >
          <ChevronLeft size={18} />
        </button>
        <div>
          <h1 className="text-xl font-bold text-navy">공고 생성</h1>
          <p className="text-xs text-gray-400 mt-0.5">공고를 등록하면 적합한 스태프를 추천받을 수 있습니다</p>
        </div>
      </div>

      <form onSubmit={handleSubmit} noValidate className="space-y-4">

        {/* ── 1. 기본 정보 ── */}
        <Card header={<SectionHeader>기본 정보</SectionHeader>}>
          <div className="space-y-5">
            {/* 공고 제목 */}
            <div>
              <label className="block text-sm font-semibold text-navy mb-1.5">
                공고 제목 <span className="text-orange">*</span>
              </label>
              <input
                className={inputCls(errors.title)}
                placeholder="예: 5월 행사 안내 스태프 모집"
                value={title}
                onChange={e => { setTitle(e.target.value); setErrors(p => ({ ...p, title: '' })) }}
              />
              <FieldError msg={errors.title} />
            </div>

            {/* 계층형 지역 선택 */}
            <div>
              <label className="block text-sm font-semibold text-navy mb-1.5">
                근무 지역 <span className="text-orange">*</span>
              </label>

              {/* 선택 경로 표시 */}
              {(sido || sigungu || dong) && (
                <div className="flex items-center gap-1.5 mb-3 flex-wrap">
                  {[sido, sigungu, dong, detailAddr].filter(Boolean).map((v, i, arr) => (
                    <span key={v} className="flex items-center gap-1 text-sm">
                      <span className={`font-semibold ${i === arr.length - 1 ? 'text-orange' : 'text-navy'}`}>{v}</span>
                      {i < arr.length - 1 && <ChevronRight size={13} className="text-gray-400" />}
                    </span>
                  ))}
                  <button
                    type="button"
                    onClick={() => { setSido(''); setSigungu(''); setDong(''); setDetailAddr('') }}
                    className="ml-1 p-0.5 rounded text-gray-400 hover:text-red-400"
                  >
                    <X size={13} />
                  </button>
                </div>
              )}

              {/* 시/도 */}
              <div className="space-y-2">
                <p className="text-xs font-medium text-gray-400">시/도</p>
                <div className="flex flex-wrap gap-2">
                  {Object.keys(LOCATION_DATA).map(s => (
                    <button
                      type="button" key={s}
                      onClick={() => selectSido(s)}
                      className={`text-xs font-semibold px-3 py-1.5 rounded-full border transition-all ${
                        sido === s
                          ? 'bg-navy text-white border-navy'
                          : 'bg-white text-navy border-offwhite-200 hover:border-navy'
                      }`}
                    >
                      {s}
                    </button>
                  ))}
                </div>
              </div>

              {/* 구/군 */}
              {sido && (
                <div className="mt-3 space-y-2">
                  <p className="text-xs font-medium text-gray-400">구/군</p>
                  <div className="flex flex-wrap gap-2">
                    {Object.keys(LOCATION_DATA[sido]).map(g => (
                      <button
                        type="button" key={g}
                        onClick={() => selectSigungu(g)}
                        className={`text-xs font-semibold px-3 py-1.5 rounded-full border transition-all ${
                          sigungu === g
                            ? 'bg-navy text-white border-navy'
                            : 'bg-white text-navy border-offwhite-200 hover:border-navy'
                        }`}
                      >
                        {g}
                      </button>
                    ))}
                  </div>
                </div>
              )}

              {/* 동/읍/면 */}
              {sigungu && LOCATION_DATA[sido][sigungu].length > 0 && (
                <div className="mt-3 space-y-2">
                  <p className="text-xs font-medium text-gray-400">동/읍/면</p>
                  <div className="flex flex-wrap gap-2">
                    {LOCATION_DATA[sido][sigungu].map(d => (
                      <button
                        type="button" key={d}
                        onClick={() => selectDong(d)}
                        className={`text-xs font-semibold px-3 py-1.5 rounded-full border transition-all ${
                          dong === d
                            ? 'bg-orange text-white border-orange'
                            : 'bg-white text-gray-600 border-offwhite-200 hover:border-orange hover:text-orange'
                        }`}
                      >
                        {d}
                      </button>
                    ))}
                  </div>
                </div>
              )}

              {/* 세부 주소 */}
              {dong && (
                <div className="mt-3 space-y-2">
                  <p className="text-xs font-medium text-gray-400">상세 주소 <span className="text-gray-300">(선택)</span></p>
                  <input
                    type="text"
                    className={inputCls('')}
                    placeholder="건물명, 층수, 상세 위치 등을 입력하세요"
                    value={detailAddr}
                    onChange={e => setDetailAddr(e.target.value)}
                  />
                </div>
              )}

              <FieldError msg={errors.location} />
            </div>

            {/* 근무 시간 */}
            <div>
              <label className="block text-sm font-semibold text-navy mb-1.5">
                근무 시간 <span className="text-orange">*</span>
              </label>
              <div className="flex items-center gap-3 flex-wrap">
                <WheelPicker
                  value={startTime}
                  onChange={v => { setStartTime(v); setErrors(p => ({ ...p, startTime: '', endTime: '' })) }}
                  label="시작 시간"
                />
                <span className="text-gray-400 font-bold text-lg">~</span>
                <WheelPicker
                  value={endTime}
                  onChange={v => { setEndTime(v); setErrors(p => ({ ...p, endTime: '' })) }}
                  label="종료 시간"
                />
                <select
                  value={breakMin}
                  onChange={e => setBreakMin(e.target.value)}
                  className="border border-offwhite-200 rounded-xl px-3 py-2.5 text-sm bg-white outline-none focus:border-navy hover:border-navy-200 text-gray-600"
                >
                  <option value="0">휴게 없음</option>
                  <option value="30">휴게 30분</option>
                  <option value="60">휴게 1시간</option>
                  <option value="90">휴게 1.5시간</option>
                </select>
              </div>
              <FieldError msg={errors.startTime || errors.endTime} />
              {totalWorkHours && (
                <p className="text-xs text-green-600 font-semibold mt-1.5 flex items-center gap-1">
                  <span>⏱</span>
                  {startTime} ~ {endTime} · 총 근무 <strong>{totalWorkHours}</strong>
                  {Number(breakMin) > 0 && <span className="text-gray-400 font-normal">(휴게 {Number(breakMin) >= 60 ? `${Number(breakMin)/60}시간` : `${breakMin}분`} 제외)</span>}
                </p>
              )}
            </div>

            {/* 모집 인원 */}
            <div>
              <label className="block text-sm font-semibold text-navy mb-1.5">
                모집 인원 <span className="text-orange">*</span>
              </label>
              <div className="relative w-36">
                <input
                  type="number" min="1"
                  className={inputCls(errors.headcount) + ' pr-8'}
                  placeholder="0"
                  value={headcount}
                  onChange={e => { setHeadcount(e.target.value); setErrors(p => ({ ...p, headcount: '' })) }}
                />
                <span className="absolute right-3 top-1/2 -translate-y-1/2 text-xs text-gray-400 pointer-events-none">명</span>
              </div>
              <FieldError msg={errors.headcount} />
            </div>
          </div>
        </Card>

        {/* ── 2. 급여 정보 ── */}
        <Card header={<SectionHeader>급여 정보</SectionHeader>}>
          <div className="space-y-4">
            {/* 급여 유형 */}
            <div className="grid grid-cols-3 gap-2.5">
              {[{ value: 'hourly', label: '시급' }, { value: 'daily', label: '일급' }, { value: 'fixed', label: '고정급(월)' }].map(opt => (
                <button
                  type="button" key={opt.value}
                  onClick={() => handleWageTypeChange(opt.value)}
                  className={`py-2 rounded-lg border text-sm font-semibold transition-all ${
                    wageType === opt.value
                      ? 'border-orange bg-orange-50 text-orange'
                      : 'border-offwhite-200 text-gray-500 hover:border-navy-100'
                  }`}
                >
                  {opt.label}
                </button>
              ))}
            </div>

            {/* 급여 입력 */}
            <div>
              <label className="block text-sm font-semibold text-navy mb-1.5">
                금액 <span className="text-orange">*</span>
                {wageType === 'daily'  && <span className="text-xs font-normal text-gray-400 ml-2">8시간 기준 최소 {fmtNum(MIN_DAILY)}원</span>}
                {wageType === 'fixed'  && <span className="text-xs font-normal text-gray-400 ml-2">월 209시간 기준 환산 안내 제공</span>}
              </label>
              <div className="relative">
                <span className="absolute left-3 top-1/2 -translate-y-1/2 text-sm text-gray-400 pointer-events-none">₩</span>
                <input
                  type="text"
                  inputMode="numeric"
                  className={`${inputCls(errors.wage || wageError)} pl-7`}
                  placeholder={
                    wageType === 'hourly' ? `최소 ${fmtNum(MIN_HOURLY)}` :
                    wageType === 'daily'  ? `최소 ${fmtNum(MIN_DAILY)}` :
                    '예: 2,500,000'
                  }
                  value={wageRaw}
                  onChange={handleWageChange}
                  onFocus={() => setErrors(p => ({ ...p, wage: '' }))}
                />
              </div>

              {/* 최저임금 고정 안내 */}
              <div className="flex items-center gap-1.5 mt-1.5">
                <Info size={12} className="text-gray-400 shrink-0" />
                <p className="text-xs text-gray-400">
                  2026년 기준 최저임금 시급 <strong className="text-navy">10,320원</strong>
                </p>
              </div>

              {/* 에러/경고 */}
              {(errors.wage || wageError) && <FieldError msg={errors.wage || wageError} />}
              {wageWarning && !wageError && (
                <p className="flex items-start gap-1 text-xs text-amber-600 mt-1.5">
                  <AlertCircle size={11} className="shrink-0 mt-0.5" />{wageWarning}
                </p>
              )}

              {/* 입력 시 실시간 표시 */}
              {wageRaw && !wageError && (
                <p className="text-xs text-green-600 mt-1.5 font-medium">
                  ✓ {wageType === 'hourly' ? `시급 ${wageRaw}원` : wageType === 'daily' ? `일급 ${wageRaw}원` : `월 ${wageRaw}원`}으로 설정됩니다.
                </p>
              )}
            </div>
          </div>
        </Card>

        {/* ── 3. 업무 내용 ── */}
        <Card header={<SectionHeader accent="navy">업무 내용 <span className="text-xs font-normal text-gray-400 ml-1">(선택)</span></SectionHeader>}>
          <div className="space-y-4">
            {/* 업무 카테고리 */}
            <div>
              <label className="block text-sm font-semibold text-navy mb-2">업무 유형 선택</label>
              <div className="flex flex-wrap gap-2">
                {JOB_CATEGORIES.map(cat => (
                  <Chip
                    key={cat}
                    label={cat}
                    selected={categories.includes(cat)}
                    onClick={() => toggleCategory(cat)}
                  />
                ))}
              </div>
              {categories.length > 0 && (
                <div className="flex flex-wrap gap-1.5 mt-3 pt-3 border-t border-offwhite-200">
                  <span className="text-xs text-gray-400 mr-1 self-center">선택됨:</span>
                  {categories.map(cat => (
                    <span key={cat} className="flex items-center gap-1 text-xs bg-orange-50 text-orange border border-orange/20 px-2 py-0.5 rounded-full">
                      {cat}
                      <button type="button" onClick={() => toggleCategory(cat)}>
                        <X size={10} />
                      </button>
                    </span>
                  ))}
                </div>
              )}
            </div>

            {/* 자유 입력 */}
            <div>
              <label className="block text-sm font-semibold text-navy mb-1.5">상세 설명</label>
              <textarea
                rows={3}
                className="w-full border border-offwhite-200 rounded-lg px-3 py-2.5 text-sm outline-none focus:border-navy hover:border-navy-200 transition-colors resize-none"
                placeholder="담당 업무를 구체적으로 입력하세요 (선택 사항)"
                value={description}
                onChange={e => setDescription(e.target.value)}
              />
            </div>
          </div>
        </Card>

        {/* ── 4. 지원 조건 ── */}
        <Card header={<SectionHeader accent="navy">지원 조건 <span className="text-xs font-normal text-gray-400 ml-1">(선택)</span></SectionHeader>}>
          {/* 탭 */}
          <div className="flex gap-1 mb-5 bg-offwhite rounded-xl p-1">
            {[{ key: 'required', label: '필수 조건' }, { key: 'preferred', label: '우대 조건' }].map(tab => (
              <button
                type="button" key={tab.key}
                onClick={() => setReqTab(tab.key)}
                className={`flex-1 py-2 text-sm font-semibold rounded-lg transition-all ${
                  reqTab === tab.key
                    ? 'bg-white text-navy shadow-sm'
                    : 'text-gray-400 hover:text-navy'
                }`}
              >
                {tab.label}
                {tab.key === 'required'  && requiredChips.length  > 0 && (
                  <span className="ml-1.5 text-xs bg-orange text-white font-bold px-1.5 py-0.5 rounded-full">{requiredChips.length}</span>
                )}
                {tab.key === 'preferred' && preferredChips.length > 0 && (
                  <span className="ml-1.5 text-xs bg-orange text-white font-bold px-1.5 py-0.5 rounded-full">{preferredChips.length}</span>
                )}
              </button>
            ))}
          </div>

          {(reqTab === 'required' ? REQUIRED_GROUPS : PREFERRED_GROUPS).map(group => (
            <div key={group.label} className="mb-4 last:mb-0">
              <p className="text-xs font-semibold text-gray-400 mb-2 uppercase tracking-wide">{group.label}</p>
              <div className="flex flex-wrap gap-2">
                {group.chips.map(chip => {
                  const isRequired = reqTab === 'required'
                  const selected = isRequired ? requiredChips.includes(chip) : preferredChips.includes(chip)
                  return (
                    <Chip
                      key={chip}
                      label={chip}
                      selected={selected}
                      onClick={() => toggleChip(chip, isRequired)}
                    />
                  )
                })}
              </div>
            </div>
          ))}

          {/* 선택된 조건 요약 */}
          {(requiredChips.length > 0 || preferredChips.length > 0) && (
            <div className="mt-4 pt-4 border-t border-offwhite-200 space-y-2">
              {requiredChips.length > 0 && (
                <div className="flex flex-wrap gap-1.5 items-center">
                  <span className="text-xs text-gray-400 w-12 shrink-0">필수</span>
                  {requiredChips.map(c => (
                    <span key={c} className="text-xs bg-navy/5 text-navy px-2 py-0.5 rounded-full border border-navy/10">{c}</span>
                  ))}
                </div>
              )}
              {preferredChips.length > 0 && (
                <div className="flex flex-wrap gap-1.5 items-center">
                  <span className="text-xs text-gray-400 w-12 shrink-0">우대</span>
                  {preferredChips.map(c => (
                    <span key={c} className="text-xs bg-green-50 text-green-700 px-2 py-0.5 rounded-full border border-green-100">{c}</span>
                  ))}
                </div>
              )}
            </div>
          )}
        </Card>

        {/* 버튼 */}
        <div className="flex gap-3 justify-end pt-1">
          <Button type="button" variant="secondary" onClick={() => navigate(-1)}>취소</Button>
          <Button type="submit" loading={submitting}>
            {submitting ? '등록 중...' : '공고 등록하기'}
          </Button>
        </div>
      </form>
    </div>
  )
}
