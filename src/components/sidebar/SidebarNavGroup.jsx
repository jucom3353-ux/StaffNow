import SidebarNavItem from './SidebarNavItem'

export default function SidebarNavGroup({ label, items, collapsed, onNavClick }) {
  return (
    <div className="mb-2">
      {label && !collapsed && (
        <p className="px-5 py-1 text-xs font-semibold text-navy-200 uppercase tracking-wider">
          {label}
        </p>
      )}
      {label && collapsed && <div className="mx-2 my-1 border-t border-navy-700" />}
      <div className="space-y-0.5">
        {items.map(item => (
          <SidebarNavItem key={item.path} {...item} collapsed={collapsed} onNavClick={onNavClick} />
        ))}
      </div>
    </div>
  )
}
