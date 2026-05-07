import { useState } from 'react'
import { DollarSign, CheckCircle2, Clock, AlertCircle, Check } from 'lucide-react'
import Card from '../../components/ui/Card'
import Button from '../../components/ui/Button'

const INITIAL_PAYROLL = [
  { id: 1, staff: '이영희', role: '부스 운영 보조',    shift: '주말 행사 스태프 · 5월 10일', hours: '9h',  amount: 117000, status: 'unpaid' },
  { id: 2, staff: '홍길동', role: '행사 안내 스태프',  shift: '주말 행사 스태프 · 5월 10일', hours: '9h',  amount: 117000, status: 'unpaid' },
  { id: 3, staff: '박지수', role: '안내 데스크',        shift: '주말 행사 스태프 · 5월 10일', hours: '0h',  amount: 0,      status: 'pending_confirm' },
  { id: 4, staff: '김철수', role: '행사 진행 보조',    shift: '강남 매장 오픈 지원 · 4월 25일', hours: '10h', amount: 125000, status: 'paid' },
  { id: 5, staff: '최민준', role: '행사 안내 스태프',  shift: '강남 매장 오픈 지원 · 4월 25일', hours: '9h',  amount: 117000, status: 'paid' },
]

const STATUS_META = {
  unpaid:          { label: '미정산',     color: 'text-amber-600 bg-amber-50 border-amber-200' },
  paid:            { label: '정산 완료',  color: 'text-green-600 bg-green-50 border-green-200' },
  pending_confirm: { label: '확인 중',    color: 'text-gray-500 bg-gray-50 border-gray-200' },
}

const TABS = [
  { key: 'all',             label: '전체' },
  { key: 'unpaid',          label: '미정산' },
  { key: 'paid',            label: '완료' },
  { key: 'pending_confirm', label: '확인 중' },
]

function fmt(n) { return n.toLocaleString('ko-KR') }

function StatusPill({ status }) {
  const meta = STATUS_META[status] || { label: status, color: 'text-gray-500 bg-gray-50 border-gray-200' }
  return (
    <span className={`inline-flex text-xs font-semibold px-2.5 py-1 rounded-full border ${meta.color}`}>
      {meta.label}
    </span>
  )
}

export default function PayrollPage() {
  const [payroll, setPayroll] = useState(INITIAL_PAYROLL)
  const [tab, setTab] = useState('all')

  const unpaid = payroll.filter(p => p.status === 'unpaid')
  const filtered = payroll.filter(p => tab === 'all' || p.status === tab)

  function approveOne(id) {
    setPayroll(prev => prev.map(p => p.id === id && p.status === 'unpaid' ? { ...p, status: 'paid' } : p))
  }
  function approveAll() {
    setPayroll(prev => prev.map(p => p.status === 'unpaid' ? { ...p, status: 'paid' } : p))
  }

  const totalUnpaid = unpaid.reduce((s, p) => s + p.amount, 0)
  const totalPaid   = payroll.filter(p => p.status === 'paid').reduce((s, p) => s + p.amount, 0)

  return (
    <div className="space-y-5">
      {/* 헤더 */}
      <div className="flex items-start justify-between">
        <div>
          <h1 className="text-xl font-bold text-navy">정산 관리</h1>
          <p className="text-sm text-gray-500 mt-0.5">
            미정산 <strong className="text-amber-600">{unpaid.length}건</strong>
            {totalUnpaid > 0 && <span className="ml-1 text-amber-600">· ₩{fmt(totalUnpaid)}</span>}
          </p>
        </div>
        {unpaid.length > 0 && (
          <Button icon={CheckCircle2} onClick={approveAll}>일괄 정산 승인</Button>
        )}
      </div>

      {/* 요약 */}
      <div className="grid grid-cols-3 gap-3">
        {[
          { label: '미정산',    value: `₩${fmt(totalUnpaid)}`,  sub: `${unpaid.length}건`,            icon: AlertCircle,  color: 'text-amber-500',  bg: 'bg-amber-50' },
          { label: '정산 완료', value: `₩${fmt(totalPaid)}`,    sub: `${payroll.filter(p=>p.status==='paid').length}건`, icon: CheckCircle2, color: 'text-green-600', bg: 'bg-green-50' },
          { label: '이번 달 총', value: `₩${fmt(totalUnpaid + totalPaid)}`, sub: `${payroll.filter(p=>p.status!=='pending_confirm').length}건`, icon: DollarSign, color: 'text-navy', bg: 'bg-navy/10' },
        ].map(({ label, value, sub, icon: Icon, color, bg }) => (
          <Card key={label}>
            <div className="flex items-center gap-3">
              <div className={`w-9 h-9 rounded-lg ${bg} flex items-center justify-center shrink-0`}>
                <Icon size={16} className={color} />
              </div>
              <div>
                <p className="text-lg font-bold text-navy tabular-nums">{value}</p>
                <p className="text-xs text-gray-500">{label} · {sub}</p>
              </div>
            </div>
          </Card>
        ))}
      </div>

      {/* 탭 */}
      <div className="flex gap-1 border-b border-offwhite-200">
        {TABS.map(t => {
          const cnt = t.key === 'all' ? 0 : payroll.filter(p => p.status === t.key).length
          return (
            <button
              key={t.key}
              onClick={() => setTab(t.key)}
              className={`px-4 py-2.5 text-sm font-semibold transition-colors border-b-2 -mb-px ${
                tab === t.key
                  ? 'border-orange text-orange'
                  : 'border-transparent text-gray-500 hover:text-navy'
              }`}
            >
              {t.label}
              {t.key !== 'all' && cnt > 0 && (
                <span className="ml-1.5 text-xs bg-offwhite-200 text-gray-600 px-1.5 py-0.5 rounded-full">{cnt}</span>
              )}
            </button>
          )
        })}
      </div>

      {/* 테이블 */}
      <Card padding={false}>
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-offwhite-200">
              <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">스태프</th>
              <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider hidden md:table-cell">Shift</th>
              <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">근무 시간</th>
              <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">금액</th>
              <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">상태</th>
              <th className="px-5 py-3" />
            </tr>
          </thead>
          <tbody>
            {filtered.map(p => (
              <tr key={p.id} className="border-b border-offwhite-100 hover:bg-offwhite-100 transition-colors">
                <td className="px-5 py-3.5">
                  <p className="font-semibold text-navy">{p.staff}</p>
                  <p className="text-xs text-gray-400 mt-0.5">{p.role}</p>
                </td>
                <td className="px-5 py-3.5 text-gray-600 hidden md:table-cell">{p.shift}</td>
                <td className="px-5 py-3.5 text-gray-700 tabular-nums">{p.hours}</td>
                <td className="px-5 py-3.5 font-semibold text-navy tabular-nums">
                  {p.amount > 0 ? `₩${fmt(p.amount)}` : '—'}
                </td>
                <td className="px-5 py-3.5"><StatusPill status={p.status} /></td>
                <td className="px-5 py-3.5">
                  {p.status === 'unpaid' && (
                    <button
                      onClick={() => approveOne(p.id)}
                      className="flex items-center gap-1 text-xs font-semibold text-green-600 bg-green-50 hover:bg-green-100 border border-green-200 px-2.5 py-1.5 rounded-lg transition-colors"
                    >
                      <Check size={12} />승인
                    </button>
                  )}
                  {p.status === 'paid' && (
                    <span className="text-xs text-green-600 flex items-center gap-1 font-medium">
                      <CheckCircle2 size={11} />완료
                    </span>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </Card>
    </div>
  )
}
