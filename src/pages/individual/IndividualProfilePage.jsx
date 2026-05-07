import { User, Mail, Phone, MapPin, Pencil } from 'lucide-react'
import { useAuth } from '../../context/AuthContext'

export default function IndividualProfilePage() {
  const { user } = useAuth()

  return (
    <div className="space-y-5 max-w-xl">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-navy">내 프로필</h1>
        <button className="flex items-center gap-2 text-sm font-semibold text-orange hover:underline">
          <Pencil size={14} />
          수정
        </button>
      </div>

      {/* 프로필 카드 */}
      <div className="bg-white rounded-2xl border border-offwhite-200 p-6">
        <div className="flex items-center gap-4 mb-6">
          <div className="w-16 h-16 rounded-full bg-orange flex items-center justify-center">
            <span className="text-white text-2xl font-bold">{user?.avatar}</span>
          </div>
          <div>
            <h2 className="text-lg font-bold text-navy">{user?.name}</h2>
            <p className="text-sm text-orange font-medium">{user?.roleLabel}</p>
          </div>
        </div>

        <div className="space-y-3">
          {[
            { icon: Mail,    label: '이메일',  value: user?.email ?? '-' },
            { icon: Phone,   label: '연락처',  value: '010-1234-5678' },
            { icon: MapPin,  label: '거주지',  value: '서울 강남구' },
            { icon: User,    label: '경력',    value: '신입 / 경력 무관' },
          ].map(item => (
            <div key={item.label} className="flex items-center gap-3 p-3 bg-offwhite rounded-xl">
              <item.icon size={16} className="text-orange shrink-0" />
              <div className="flex-1 flex items-center justify-between">
                <span className="text-xs text-gray-400 w-16">{item.label}</span>
                <span className="text-sm font-medium text-navy">{item.value}</span>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* 보유 스킬 */}
      <div className="bg-white rounded-2xl border border-offwhite-200 p-6">
        <h3 className="font-bold text-navy mb-3">보유 스킬</h3>
        <div className="flex flex-wrap gap-2">
          {['행사 진행', '고객 응대', '엑셀', '포토샵', '운전면허'].map(skill => (
            <span key={skill} className="text-sm bg-offwhite px-3 py-1.5 rounded-full text-gray-600 border border-offwhite-200">
              {skill}
            </span>
          ))}
        </div>
      </div>
    </div>
  )
}
