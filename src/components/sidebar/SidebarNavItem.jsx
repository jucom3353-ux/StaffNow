import clsx from 'clsx'
import { NavLink } from 'react-router-dom'

export default function SidebarNavItem({ icon: Icon, label, path, badge, collapsed }) {
  return (
    <NavLink
      to={path}
      className={({ isActive }) =>
        clsx(
          'flex items-center mx-2 rounded-lg transition-colors duration-150 relative group',
          collapsed ? 'justify-center p-2' : 'gap-3 px-3 py-2',
          isActive
            ? 'bg-navy-700 text-white'
            : 'text-navy-100 hover:bg-navy-700 hover:text-white'
        )
      }
    >
      {({ isActive }) => (
        <>
          {isActive && !collapsed && (
            <span className="absolute left-0 top-1/2 -translate-y-1/2 w-1 h-5 bg-orange rounded-r-full" />
          )}
          <div className="relative shrink-0">
            <Icon size={18} />
            {badge > 0 && collapsed && (
              <span className="absolute -top-1 -right-1 w-2 h-2 bg-orange rounded-full" />
            )}
          </div>
          {!collapsed && (
            <>
              <span className="text-sm font-medium flex-1 whitespace-nowrap">{label}</span>
              {badge > 0 && (
                <span className="ml-auto bg-orange text-white text-xs font-bold rounded-full w-5 h-5 flex items-center justify-center shrink-0">
                  {badge > 9 ? '9+' : badge}
                </span>
              )}
            </>
          )}
          {collapsed && (
            <span className="absolute left-full ml-2 px-2 py-1 bg-navy-900 text-white text-xs rounded-md whitespace-nowrap opacity-0 group-hover:opacity-100 pointer-events-none transition-opacity z-50">
              {label}
            </span>
          )}
        </>
      )}
    </NavLink>
  )
}
