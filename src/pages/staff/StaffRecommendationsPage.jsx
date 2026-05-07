import Card from '../../components/ui/Card'
import Avatar from '../../components/ui/Avatar'
import Button from '../../components/ui/Button'
import { MOCK_STAFF } from '../../data/mockUsers'
import { Star } from 'lucide-react'

export default function StaffRecommendationsPage() {
  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-xl font-bold text-navy">추천 인력</h1>
        <p className="text-sm text-gray-500 mt-0.5">현재 가용 가능한 인력 {MOCK_STAFF.filter(s => s.status === 'available').length}명</p>
      </div>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {MOCK_STAFF.map(staff => (
          <Card key={staff.id}>
            <div className="flex items-start gap-3">
              <Avatar initials={staff.initials} size="lg" />
              <div className="flex-1 min-w-0">
                <div className="flex items-center justify-between">
                  <p className="font-semibold text-navy text-sm">{staff.name}</p>
                  <span className="flex items-center gap-0.5 text-xs text-yellow-600 font-medium">
                    <Star size={12} className="fill-yellow-400 text-yellow-400" />
                    {staff.rating}
                  </span>
                </div>
                <p className="text-xs text-gray-500 mt-0.5">완료 {staff.completedShifts}회 · {staff.availableDate} 가용</p>
                <div className="flex flex-wrap gap-1 mt-2">
                  {staff.skills.map(s => (
                    <span key={s} className="bg-navy-50 text-navy text-xs px-2 py-0.5 rounded-full">{s}</span>
                  ))}
                </div>
              </div>
            </div>
            <div className="mt-3 pt-3 border-t border-offwhite-200">
              <Button size="sm" className="w-full justify-center">초대 보내기</Button>
            </div>
          </Card>
        ))}
      </div>
    </div>
  )
}
