import AppFooter from '../components/ui/AppFooter'

export default function PageContainer({ children }) {
  return (
    <div className="p-6 min-h-full flex flex-col">
      <div className="flex-1">{children}</div>
      <AppFooter />
    </div>
  )
}
