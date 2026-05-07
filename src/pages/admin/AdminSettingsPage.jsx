import { Settings, Shield, Bell, Database } from 'lucide-react'

export default function AdminSettingsPage() {
  return (
    <div className="space-y-5 max-w-xl">
      <div>
        <h1 className="text-2xl font-bold text-navy">시스템 설정</h1>
        <p className="text-sm text-gray-500 mt-1">플랫폼 운영 환경 설정</p>
      </div>

      {[
        {
          icon: Shield, title: '보안 설정', items: [
            { label: '2단계 인증 강제', value: '활성화됨' },
            { label: '세션 타임아웃', value: '30분' },
          ]
        },
        {
          icon: Bell, title: '알림 설정', items: [
            { label: '신고 접수 알림', value: '즉시' },
            { label: '기업 인증 요청 알림', value: '즉시' },
          ]
        },
        {
          icon: Database, title: '데이터 설정', items: [
            { label: '공고 자동 마감', value: '마감일 기준' },
            { label: '비활성 계정 정리', value: '180일 후' },
          ]
        },
      ].map(section => (
        <div key={section.title} className="bg-white rounded-2xl border border-offwhite-200 p-5">
          <div className="flex items-center gap-2 mb-4">
            <section.icon size={18} className="text-orange" />
            <h2 className="font-bold text-navy">{section.title}</h2>
          </div>
          <div className="space-y-3">
            {section.items.map(item => (
              <div key={item.label} className="flex items-center justify-between py-2 border-b border-offwhite-200 last:border-0">
                <span className="text-sm text-gray-600">{item.label}</span>
                <span className="text-sm font-semibold text-navy">{item.value}</span>
              </div>
            ))}
          </div>
        </div>
      ))}
    </div>
  )
}
