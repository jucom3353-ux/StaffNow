import { Link } from 'react-router-dom'
import Button from './Button'

export default function EmptyState({ icon: Icon, title, description, action }) {
  return (
    <div className="flex flex-col items-center justify-center py-12 px-4 text-center">
      {Icon && (
        <div className="w-12 h-12 rounded-full bg-offwhite-100 flex items-center justify-center mb-4">
          <Icon size={24} className="text-navy-200" />
        </div>
      )}
      <p className="text-base font-semibold text-navy mb-1">{title}</p>
      {description && <p className="text-sm text-gray-500 mb-4">{description}</p>}
      {action && (
        <Button
          variant="primary"
          size="sm"
          as={action.to ? Link : undefined}
          to={action.to}
          onClick={action.onClick}
        >
          {action.label}
        </Button>
      )}
    </div>
  )
}
