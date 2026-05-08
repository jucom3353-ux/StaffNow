import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AppDataProvider } from './context/AppDataContext'
import { AuthProvider, useAuth } from './context/AuthContext'
import ToastContainer from './components/ui/Toast'

import AppShell from './layouts/AppShell'
import AuthLayout from './layouts/AuthLayout'
import IndividualLayout from './layouts/IndividualLayout'
import AdminLayout from './layouts/AdminLayout'

// Auth
import LoginPage from './pages/auth/LoginPage'
import RegisterPage from './pages/auth/RegisterPage'
import ForgotPasswordPage from './pages/auth/ForgotPasswordPage'

// Business portal
import DashboardPage from './pages/dashboard/DashboardPage'
import JobListPage from './pages/jobs/JobListPage'
import JobCreatePage from './pages/jobs/JobCreatePage'
import JobDetailPage from './pages/jobs/JobDetailPage'
import JobEditPage from './pages/jobs/JobEditPage'
import ShiftListPage from './pages/shifts/ShiftListPage'
import ShiftCreatePage from './pages/shifts/ShiftCreatePage'
import ShiftDetailPage from './pages/shifts/ShiftDetailPage'
import StaffRecommendationsPage from './pages/staff/StaffRecommendationsPage'
import InvitationsPage from './pages/invitations/InvitationsPage'
import ContractsPage from './pages/contracts/ContractsPage'
import AttendancePage from './pages/attendance/AttendancePage'
import PayrollPage from './pages/payroll/PayrollPage'
import MessagesPage from './pages/messages/MessagesPage'
import CompanySettingsPage from './pages/settings/CompanySettingsPage'
import MemberManagementPage from './pages/settings/MemberManagementPage'

// Individual portal
import IndividualDashboardPage from './pages/individual/IndividualDashboardPage'
import IndividualJobsPage from './pages/individual/IndividualJobsPage'
import IndividualJobDetailPage from './pages/individual/IndividualJobDetailPage'
import IndividualApplicationsPage from './pages/individual/IndividualApplicationsPage'
import IndividualSavedPage from './pages/individual/IndividualSavedPage'
import IndividualMessagesPage from './pages/individual/IndividualMessagesPage'
import IndividualProfilePage from './pages/individual/IndividualProfilePage'
import IndividualCompanyPage from './pages/individual/IndividualCompanyPage'
import IndividualAttendancePage from './pages/individual/IndividualAttendancePage'

// Admin portal
import AdminDashboardPage from './pages/admin/AdminDashboardPage'
import AdminUsersPage from './pages/admin/AdminUsersPage'
import AdminBusinessesPage from './pages/admin/AdminBusinessesPage'
import AdminJobsPage from './pages/admin/AdminJobsPage'
import AdminReportsPage from './pages/admin/AdminReportsPage'
import AdminSettingsPage from './pages/admin/AdminSettingsPage'

function ProtectedRoute({ children, requiredRole }) {
  const { user } = useAuth()
  if (!user) return <Navigate to="/login" replace />
  if (requiredRole && user.role !== requiredRole) return <Navigate to="/login" replace />
  return children
}

function RootRedirect() {
  const { user } = useAuth()
  if (!user) return <Navigate to="/login" replace />
  if (user.role === 'INDIVIDUAL') return <Navigate to="/individual" replace />
  if (user.role === 'ADMIN') return <Navigate to="/admin" replace />
  return <Navigate to="/dashboard" replace />
}

export default function App() {
  return (
    <AuthProvider>
      <AppDataProvider>
        <BrowserRouter>
          <Routes>
            <Route path="/" element={<RootRedirect />} />

            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/forgot-password" element={<ForgotPasswordPage />} />

            {/* Business portal */}
            <Route element={
              <ProtectedRoute requiredRole="BUSINESS"><AppShell /></ProtectedRoute>
            }>
              <Route path="/dashboard" element={<DashboardPage />} />
              <Route path="/jobs" element={<JobListPage />} />
              <Route path="/jobs/create" element={<JobCreatePage />} />
              <Route path="/jobs/:id" element={<JobDetailPage />} />
              <Route path="/jobs/:id/edit" element={<JobEditPage />} />
              <Route path="/shifts" element={<ShiftListPage />} />
              <Route path="/shifts/create" element={<ShiftCreatePage />} />
              <Route path="/shifts/:id" element={<ShiftDetailPage />} />
              <Route path="/staff" element={<StaffRecommendationsPage />} />
              <Route path="/invitations" element={<InvitationsPage />} />
              <Route path="/contracts" element={<ContractsPage />} />
              <Route path="/attendance" element={<AttendancePage />} />
              <Route path="/payroll" element={<PayrollPage />} />
              <Route path="/messages" element={<MessagesPage />} />
              <Route path="/settings/company" element={<CompanySettingsPage />} />
              <Route path="/settings/members" element={<MemberManagementPage />} />
            </Route>

            {/* Individual portal */}
            <Route element={
              <ProtectedRoute requiredRole="INDIVIDUAL"><IndividualLayout /></ProtectedRoute>
            }>
              <Route path="/individual" element={<IndividualDashboardPage />} />
              <Route path="/individual/jobs" element={<IndividualJobsPage />} />
              <Route path="/individual/jobs/:id" element={<IndividualJobDetailPage />} />
              <Route path="/individual/applications" element={<IndividualApplicationsPage />} />
              <Route path="/individual/saved" element={<IndividualSavedPage />} />
              <Route path="/individual/attendance" element={<IndividualAttendancePage />} />
              <Route path="/individual/messages" element={<IndividualMessagesPage />} />
              <Route path="/individual/company/:name" element={<IndividualCompanyPage />} />
              <Route path="/individual/profile" element={<IndividualProfilePage />} />
            </Route>

            {/* Admin portal */}
            <Route element={
              <ProtectedRoute requiredRole="ADMIN"><AdminLayout /></ProtectedRoute>
            }>
              <Route path="/admin" element={<AdminDashboardPage />} />
              <Route path="/admin/users" element={<AdminUsersPage />} />
              <Route path="/admin/businesses" element={<AdminBusinessesPage />} />
              <Route path="/admin/jobs" element={<AdminJobsPage />} />
              <Route path="/admin/reports" element={<AdminReportsPage />} />
              <Route path="/admin/settings" element={<AdminSettingsPage />} />
            </Route>
          </Routes>
          <ToastContainer />
        </BrowserRouter>
      </AppDataProvider>
    </AuthProvider>
  )
}
