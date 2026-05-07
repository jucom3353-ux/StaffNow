import { useState } from 'react'
import { RotateCcw, AlertTriangle, CheckCircle } from 'lucide-react'
import Card from '../../components/ui/Card'
import Button from '../../components/ui/Button'
import { useAppData } from '../../context/AppDataContext'

export default function CompanySettingsPage() {
  const { resetDemoData } = useAppData()
  const [confirmReset, setConfirmReset] = useState(false)
  const [resetDone, setResetDone] = useState(false)

  function handleReset() {
    resetDemoData()
    setConfirmReset(false)
    setResetDone(true)
    setTimeout(() => setResetDone(false), 3000)
  }

  return (
    <div className="max-w-2xl space-y-5">
      <h1 className="text-xl font-bold text-navy">회사 설정</h1>

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
