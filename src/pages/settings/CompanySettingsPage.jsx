import { useState } from 'react'
import { RotateCcw, AlertTriangle, CheckCircle, User, CreditCard, Star, Plus, X } from 'lucide-react'
import Card from '../../components/ui/Card'
import Button from '../../components/ui/Button'
import { useAppData } from '../../context/AppDataContext'

const MOCK_PREFERRED_TAGS = ['활발함', '인근 거주자', '장기 근무 가능', '행사 경험자', '외국어 가능', '야간 근무 가능']

export default function CompanySettingsPage() {
  const { resetDemoData } = useAppData()
  const [confirmReset, setConfirmReset] = useState(false)
  const [resetDone, setResetDone] = useState(false)

  // 담당자 정보
  const [manager, setManager] = useState({
    name: '김운영',
    title: '운영 팀장',
    phone: '010-1234-5678',
    email: 'manager@jucompany.co.kr',
  })
  const [managerSaved, setManagerSaved] = useState(false)

  // 정산 계좌
  const [account, setAccount] = useState({
    bank: '신한은행',
    number: '110-123-456789',
    holder: '주식회사 JU Company',
  })
  const [accountSaved, setAccountSaved] = useState(false)

  // 우대 조건 템플릿
  const [tags, setTags] = useState(['활발함', '인근 거주자', '행사 경험자'])
  const [tagInput, setTagInput] = useState('')

  function handleSave(setSaved) {
    setSaved(true)
    setTimeout(() => setSaved(false), 2500)
  }

  function addTag() {
    const t = tagInput.trim()
    if (t && !tags.includes(t)) setTags(prev => [...prev, t])
    setTagInput('')
  }

  function removeTag(t) {
    setTags(prev => prev.filter(x => x !== t))
  }

  function handleReset() {
    resetDemoData()
    setConfirmReset(false)
    setResetDone(true)
    setTimeout(() => setResetDone(false), 3000)
  }

  return (
    <div className="max-w-2xl space-y-5">
      <h1 className="text-xl font-bold text-navy">회사 설정</h1>

      {/* ── 기본 정보 ─────────────────────────────────────── */}
      <Card header={<h2 className="font-semibold text-navy">기본 정보</h2>}>
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-navy mb-1">회사명</label>
            <input defaultValue="주식회사 JU Company" className="w-full border border-offwhite-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-navy" />
          </div>
          <div>
            <label className="block text-sm font-medium text-navy mb-1">사업자 등록번호</label>
            <input defaultValue="123-45-67890" className="w-full border border-offwhite-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-navy" />
          </div>
        </div>
      </Card>

      <div className="flex justify-end">
        <Button>저장</Button>
      </div>

      {/* ── 채용 / 정산 정보 ───────────────────────────────── */}
      <Card header={
        <div className="flex items-center gap-2">
          <User size={15} className="text-navy" />
          <h2 className="font-semibold text-navy">채용 담당자 정보</h2>
        </div>
      }>
        <p className="text-xs text-gray-400 mb-4">공고에 노출될 담당자 정보를 설정합니다. 매번 공고마다 입력할 필요 없이 기본값으로 사용됩니다.</p>
        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-xs font-medium text-gray-500 mb-1">담당자 성함</label>
            <input
              value={manager.name}
              onChange={e => setManager(p => ({ ...p, name: e.target.value }))}
              className="w-full border border-offwhite-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-navy"
            />
          </div>
          <div>
            <label className="block text-xs font-medium text-gray-500 mb-1">직급</label>
            <input
              value={manager.title}
              onChange={e => setManager(p => ({ ...p, title: e.target.value }))}
              className="w-full border border-offwhite-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-navy"
            />
          </div>
          <div>
            <label className="block text-xs font-medium text-gray-500 mb-1">연락처</label>
            <input
              value={manager.phone}
              onChange={e => setManager(p => ({ ...p, phone: e.target.value }))}
              className="w-full border border-offwhite-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-navy"
            />
          </div>
          <div>
            <label className="block text-xs font-medium text-gray-500 mb-1">이메일</label>
            <input
              value={manager.email}
              onChange={e => setManager(p => ({ ...p, email: e.target.value }))}
              className="w-full border border-offwhite-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-navy"
            />
          </div>
        </div>
        <div className="flex justify-end mt-4">
          {managerSaved
            ? <span className="flex items-center gap-1.5 text-sm text-green-600 font-semibold"><CheckCircle size={14} />저장됐습니다</span>
            : <Button size="sm" onClick={() => handleSave(setManagerSaved)}>저장</Button>
          }
        </div>
      </Card>

      <Card header={
        <div className="flex items-center gap-2">
          <CreditCard size={15} className="text-navy" />
          <h2 className="font-semibold text-navy">정산 계좌 정보</h2>
        </div>
      }>
        <p className="text-xs text-gray-400 mb-4">정산 탭에서 확정된 급여는 이 계좌를 통해 안전하게 지급됩니다.</p>
        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-xs font-medium text-gray-500 mb-1">은행</label>
            <select
              value={account.bank}
              onChange={e => setAccount(p => ({ ...p, bank: e.target.value }))}
              className="w-full border border-offwhite-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-navy bg-white"
            >
              {['신한은행','국민은행','하나은행','우리은행','기업은행','농협은행','카카오뱅크','토스뱅크'].map(b => (
                <option key={b}>{b}</option>
              ))}
            </select>
          </div>
          <div>
            <label className="block text-xs font-medium text-gray-500 mb-1">계좌번호</label>
            <input
              value={account.number}
              onChange={e => setAccount(p => ({ ...p, number: e.target.value }))}
              className="w-full border border-offwhite-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-navy"
            />
          </div>
          <div className="col-span-2">
            <label className="block text-xs font-medium text-gray-500 mb-1">예금주</label>
            <input
              value={account.holder}
              onChange={e => setAccount(p => ({ ...p, holder: e.target.value }))}
              className="w-full border border-offwhite-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-navy"
            />
          </div>
        </div>
        <div className="flex justify-end mt-4">
          {accountSaved
            ? <span className="flex items-center gap-1.5 text-sm text-green-600 font-semibold"><CheckCircle size={14} />저장됐습니다</span>
            : <Button size="sm" onClick={() => handleSave(setAccountSaved)}>저장</Button>
          }
        </div>
      </Card>

      <Card header={
        <div className="flex items-center gap-2">
          <Star size={15} className="text-navy" />
          <h2 className="font-semibold text-navy">우대 조건 템플릿</h2>
        </div>
      }>
        <p className="text-xs text-gray-400 mb-4">자주 찾는 인재상을 태그로 저장해두고 공고 등록 시 불러와 사용합니다.</p>

        {/* 태그 목록 */}
        <div className="flex flex-wrap gap-2 mb-3">
          {tags.map(t => (
            <span key={t} className="flex items-center gap-1 bg-navy-50 text-navy text-xs font-semibold px-2.5 py-1 rounded-full border border-navy-100">
              {t}
              <button onClick={() => removeTag(t)} className="hover:text-red-500 transition-colors ml-0.5">
                <X size={11} />
              </button>
            </span>
          ))}
        </div>

        {/* 추천 태그 */}
        <div className="flex flex-wrap gap-1.5 mb-3">
          {MOCK_PREFERRED_TAGS.filter(t => !tags.includes(t)).map(t => (
            <button
              key={t}
              onClick={() => setTags(prev => [...prev, t])}
              className="text-xs text-gray-500 border border-dashed border-gray-300 px-2.5 py-1 rounded-full hover:border-navy hover:text-navy transition-colors"
            >
              + {t}
            </button>
          ))}
        </div>

        {/* 직접 입력 */}
        <div className="flex gap-2">
          <input
            type="text"
            placeholder="직접 입력..."
            value={tagInput}
            onChange={e => setTagInput(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && addTag()}
            className="flex-1 border border-offwhite-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-navy"
          />
          <Button size="sm" icon={Plus} onClick={addTag}>추가</Button>
        </div>
      </Card>

      {/* ── 데모 관리 ─────────────────────────────────────── */}
      <Card header={
        <div className="flex items-center gap-2">
          <span className="text-xs font-bold text-orange bg-orange-50 border border-orange/20 px-2 py-0.5 rounded-full">DEMO</span>
          <h2 className="font-semibold text-navy">데모 데이터 관리</h2>
        </div>
      }>
        <div className="space-y-4">
          <div className="flex items-start gap-3 p-3.5 bg-amber-50 rounded-xl border border-amber-200">
            <AlertTriangle size={15} className="text-amber-500 shrink-0 mt-0.5" />
            <p className="text-xs text-amber-700 leading-relaxed">
              시연 연습을 여러 번 진행하다가 데이터가 꼬였을 때 사용하세요.
              모든 Shift·공고 상태, 채용 확정 기록, 임시 저장 데이터가 <strong>초기 시연용 상태로 완전히 되돌아갑니다.</strong>
            </p>
          </div>

          <div className="flex items-start justify-between gap-4">
            <div className="text-sm text-gray-600 space-y-1">
              <p className="font-semibold text-navy">초기화 범위</p>
              <ul className="text-xs text-gray-500 space-y-0.5 list-disc list-inside">
                <li>모든 Shift 상태 (예정 / 완료 등)</li>
                <li>채용 확정 인원수 및 지원자 선별 기록</li>
                <li>초대/확정 내역</li>
                <li>임시 저장 데이터 (staffnow_shift_*)</li>
                <li>알림 읽음 상태</li>
              </ul>
            </div>

            {!confirmReset ? (
              <button
                onClick={() => setConfirmReset(true)}
                className="shrink-0 flex items-center gap-2 px-4 py-2.5 rounded-xl border border-red-200 text-red-500 text-sm font-semibold hover:bg-red-50 transition-colors"
              >
                <RotateCcw size={14} />데모 초기화
              </button>
            ) : (
              <div className="shrink-0 flex flex-col items-end gap-2">
                <p className="text-xs font-semibold text-red-500">정말 초기화하시겠습니까?</p>
                <div className="flex gap-2">
                  <button
                    onClick={() => setConfirmReset(false)}
                    className="px-3 py-1.5 rounded-lg border border-offwhite-200 text-gray-500 text-xs font-semibold hover:bg-offwhite transition-colors"
                  >
                    취소
                  </button>
                  <button
                    onClick={handleReset}
                    className="px-3 py-1.5 rounded-lg bg-red-500 hover:bg-red-600 text-white text-xs font-bold transition-colors"
                  >
                    초기화 실행
                  </button>
                </div>
              </div>
            )}
          </div>

          {resetDone && (
            <div className="flex items-center gap-2 p-3 bg-green-50 rounded-xl border border-green-200 text-green-700 text-sm font-semibold">
              <CheckCircle size={15} />
              초기화 완료 — 깨끗한 데모 상태로 되돌아왔습니다
            </div>
          )}
        </div>
      </Card>
    </div>
  )
}
