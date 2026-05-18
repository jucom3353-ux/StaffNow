import { Link } from 'react-router-dom'
import clsx from 'clsx'
import { TrendingUp, TrendingDown, Minus } from 'lucide-react'

const DeltaIcon = ({ dir }) => {
  if (dir === 'up') return <TrendingUp size={13} className="text-green-500" />
  if (dir === 'down') return <TrendingDown size={13} className="text-red-500" />
  return <Minus size={13} className="text-gray-400" />
}

export default function StatsCard({ icon: Icon, label, value, delta, accentColor = 'navy', to }) {
  const iconBg = accentColor === 'orange' ? 'bg-orange-50' : 'bg-navy-50'
  const iconColor = accentColor === 'orange' ? 'text-orange' : 'text-navy'
  const Wrapper = to ? Link : 'div'
  const wrapperProps = to ? { to, className: 'block' } : {}

  return (
    <Wrapper {...wrapperProps}>
    <div className={`bg-white rounded-xl border border-offwhite-200 shadow-[0_1px_3px_0_rgba(27,43,72,0.08)] p-5 flex flex-col gap-3 ${to ? 'hover:border-navy cursor-pointer transition-colors' : ''}`}>
      <div className="flex items-start justify-between">
        <div className={clsx('w-10 h-10 rounded-lg flex items-center justify-center shrink-0', iconBg)}>
          <Icon size={20} className={iconColor} />
        </div>
        {delta && (
          <span className="flex items-center gap-1 text-xs text-gray-400">
            <DeltaIcon dir={delta.dir} />
            {delta.value > 0 ? `+${delta.value}` : delta.value}
          </span>
        )}
      </div>
      <div>
        <p className="text-3xl font-bold text-navy leading-none">{value}</p>
        <p className="text-sm text-gray-500 mt-1">{label}</p>
      </div>
      {delta && (
        <p className="text-xs text-gray-400">{delta.label}</p>
      )}
    </div>
    </Wrapper>
  )
}
