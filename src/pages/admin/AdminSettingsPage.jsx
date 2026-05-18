import { useState } from 'react'
import { Shield, Bell, Database, Save, Check } from 'lucide-react'

const DEFAULT_SETTINGS = {
  twoFactorAuth: true,
  sessionTimeout: 30,
  reportAlert: 'immediate',
  bizVerifyAlert: 'immediate',
  autoCloseJobs: 'deadline',
  inactiveCleanupDays: 180,
}

function Toggle({ checked, onChange }) {
  return (
    <button
      type="button"
      onClick={() => onChange(!checked)}
      className={`relative w-11 h-6 rounded-full transition-colors shrink-0 ${checked ? 'bg-orange' : 'bg-gray-200'}`}
    >
      <span className={`absolute top-1 w-4 h-4 rounded-full bg-white shadow transition-transform ${checked ? 'translate-x-6' : 'translate-x-1'}`} />
    </button>
  )
}

export default function AdminSettingsPage() {
  const [settings, setSettings] = useState(DEFAULT_SETTINGS)
  const [saved, setSaved] = useState(false)

  function set(key, value) {
    setSettings(prev => ({ ...prev, [key]: value }))
    setSaved(false)
  }

  function save() {
    setSaved(true)
    setTimeout(() => setSaved(false), 2000)
  }

  return (
    <div className="space-y-5 max-w-xl">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-navy">시스템 설정</h1>
          <p className="text-sm text-gray-500 mt-1">플랫폼 운영 환경 설정</p>
        </div>
        <button
          onClick={save}
          className={`flex items-center gap-2 px-4 py-2.5 rounded-xl text-sm font-semibold transition-all ${
            saved
              ? 'bg-green-50 text-green-600 border border-green-200'
              : 'bg-orange text-white hover:bg-orange-600'
          }`}
        >
          {saved ? <><Check size={15} />저장됨</> : <><Save size={15} />저장</>}
        </button>
      </div>

      {/* 보안 설정 */}
      <div className="bg-white rounded-2xl border border-offwhite-200 p-5">
        <div className="flex items-center gap-2 mb-4">
          <Shield size={18} className="text-orange" />
          <h2 className="font-bold text-navy">보안 설정</h2>
        </div>
        <div className="space-y-1">
          <div className="flex items-center justify-between py-3 border-b border-offwhite-200">
            <div>
              <p className="text-sm font-medium text-navy">2단계 인증 강제</p>
              <p className="text-xs text-gray-400 mt-0.5">모든 관리자 계정에 2FA 적용</p>
            </div>
            <Toggle checked={settings.twoFactorAuth} onChange={v => set('twoFactorAuth', v)} />
          </div>
          <div className="flex items-center justify-between py-3">
            <div>
              <p className="text-sm font-medium text-navy">세션 타임아웃</p>
              <p className="text-xs text-gray-400 mt-0.5">비활성 시 자동 로그아웃 시간</p>
            </div>
            <div className="flex items-center gap-2">
              <input
                type="number"
                min={5}
                max={480}
                value={settings.sessionTimeout}
                onChange={e => set('sessionTimeout', Number(e.target.value))}
                className="w-16 text-center text-sm border border-offwhite-200 rounded-lg px-2 py-1.5 focus:outline-none focus:border-orange"
              />
              <span className="text-sm text-gray-500">분</span>
            </div>
          </div>
        </div>
      </div>

      {/* 알림 설정 */}
      <div className="bg-white rounded-2xl border border-offwhite-200 p-5">
        <div className="flex items-center gap-2 mb-4">
          <Bell size={18} className="text-orange" />
          <h2 className="font-bold text-navy">알림 설정</h2>
        </div>
        <div className="space-y-1">
          {[
            { key: 'reportAlert',    label: '신고 접수 알림',      desc: '신규 신고 발생 시 알림 시점' },
            { key: 'bizVerifyAlert', label: '기업 인증 요청 알림', desc: '기업 심사 요청 시 알림 시점' },
          ].map(({ key, label, desc }) => (
            <div key={key} className="flex items-center justify-between py-3 border-b border-offwhite-200 last:border-0">
              <div>
                <p className="text-sm font-medium text-navy">{label}</p>
                <p className="text-xs text-gray-400 mt-0.5">{desc}</p>
              </div>
              <select
                value={settings[key]}
                onChange={e => set(key, e.target.value)}
                className="text-sm border border-offwhite-200 rounded-lg px-3 py-1.5 focus:outline-none focus:border-orange text-navy bg-white"
              >
                <option value="immediate">즉시</option>
                <option value="hourly">매 1시간</option>
                <option value="daily">매일 오전 9시</option>
                <option value="off">끄기</option>
              </select>
            </div>
          ))}
        </div>
      </div>

      {/* 데이터 설정 */}
      <div className="bg-white rounded-2xl border border-offwhite-200 p-5">
        <div className="flex items-center gap-2 mb-4">
          <Database size={18} className="text-orange" />
          <h2 className="font-bold text-navy">데이터 설정</h2>
        </div>
        <div className="space-y-1">
          <div className="flex items-center justify-between py-3 border-b border-offwhite-200">
            <div>
              <p className="text-sm font-medium text-navy">공고 자동 마감</p>
              <p className="text-xs text-gray-400 mt-0.5">공고 자동 마감 처리 기준</p>
            </div>
            <select
              value={settings.autoCloseJobs}
              onChange={e => set('autoCloseJobs', e.target.value)}
              className="text-sm border border-offwhite-200 rounded-lg px-3 py-1.5 focus:outline-none focus:border-orange text-navy bg-white"
            >
              <option value="deadline">마감일 기준</option>
              <option value="headcount">인원 충족 시</option>
              <option value="manual">수동</option>
            </select>
          </div>
          <div className="flex items-center justify-between py-3">
            <div>
              <p className="text-sm font-medium text-navy">비활성 계정 정리</p>
              <p className="text-xs text-gray-400 mt-0.5">마지막 활동 후 자동 비활성화 기간</p>
            </div>
            <div className="flex items-center gap-2">
              <input
                type="number"
                min={30}
                max={365}
                value={settings.inactiveCleanupDays}
                onChange={e => set('inactiveCleanupDays', Number(e.target.value))}
                className="w-16 text-center text-sm border border-offwhite-200 rounded-lg px-2 py-1.5 focus:outline-none focus:border-orange"
              />
              <span className="text-sm text-gray-500">일 후</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
