import { Search, Users, MoreVertical } from 'lucide-react'
import { RECENT_USERS } from '../../data/mockAdmin'

const ROLE_LABEL = { INDIVIDUAL: '개인', BUSINESS: '기업' }

export default function AdminUsersPage() {
  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-navy">유저 관리</h1>
          <p className="text-sm text-gray-500 mt-1">전체 회원 현황</p>
        </div>
      </div>

      <div className="flex items-center gap-2 bg-white border border-offwhite-200 rounded-xl px-4 py-2.5 max-w-sm">
        <Search size={15} className="text-gray-400" />
        <input type="text" placeholder="이름, 이메일 검색..." className="bg-transparent text-sm outline-none w-full placeholder-gray-400 text-navy" />
      </div>

      <div className="bg-white rounded-2xl border border-offwhite-200 overflow-hidden">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-offwhite-200 bg-offwhite">
              <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500">이름</th>
              <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500">유형</th>
              <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500">이메일</th>
              <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500">가입일</th>
              <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500">상태</th>
              <th className="px-5 py-3" />
            </tr>
          </thead>
          <tbody className="divide-y divide-offwhite-200">
            {RECENT_USERS.map(u => (
              <tr key={u.id} className="hover:bg-offwhite-100 transition-colors">
                <td className="px-5 py-3.5 font-semibold text-navy">{u.name}</td>
                <td className="px-5 py-3.5">
                  <span className={`text-xs font-semibold px-2 py-0.5 rounded-full ${u.role === 'INDIVIDUAL' ? 'bg-blue-100 text-blue-700' : 'bg-purple-100 text-purple-700'}`}>
                    {ROLE_LABEL[u.role]}
                  </span>
                </td>
                <td className="px-5 py-3.5 text-gray-500">{u.email}</td>
                <td className="px-5 py-3.5 text-gray-500">{u.joinedAt}</td>
                <td className="px-5 py-3.5">
                  <span className={`flex items-center gap-1.5 text-xs font-medium ${u.isActive ? 'text-green-600' : 'text-gray-400'}`}>
                    <span className={`w-1.5 h-1.5 rounded-full ${u.isActive ? 'bg-green-500' : 'bg-gray-300'}`} />
                    {u.isActive ? '활성' : '비활성'}
                  </span>
                </td>
                <td className="px-5 py-3.5">
                  <button className="p-1 rounded hover:bg-offwhite text-gray-400 hover:text-navy">
                    <MoreVertical size={15} />
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
