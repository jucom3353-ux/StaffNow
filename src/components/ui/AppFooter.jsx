export default function AppFooter() {
  return (
    <footer className="mt-auto pt-8 pb-4 text-center text-xs text-gray-400 space-y-1 border-t border-offwhite-200">
      <p className="font-semibold text-gray-500">운영 문의</p>
      <p>이메일: <a href="mailto:support@staffnow.kr" className="hover:text-navy transition-colors">support@staffnow.kr</a></p>
      <p>전화: <a href="tel:0215881234" className="hover:text-navy transition-colors">02-1588-1234</a> (평일 09:00 ~ 18:00)</p>
      <p className="pt-1 text-gray-300">© 2026 StaffNow. All rights reserved.</p>
    </footer>
  )
}
