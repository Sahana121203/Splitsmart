import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { Toaster } from 'react-hot-toast'
import { useEffect } from 'react'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import DashboardPage from './pages/DashboardPage'
import TripDetailPage from './pages/TripDetailPage'
import ExpensesPage from './pages/ExpensesPage'
import KittyPage from './pages/KittyPage'
import SettlementPage from './pages/SettlementPage'
import BudgetVotePage from './pages/BudgetVotePage'
import ProtectedRoute from './components/common/ProtectedRoute'
import useAuthStore from './store/authStore'

export default function App() {
  const token = useAuthStore((s) => s.token)
  const fetchMe = useAuthStore((s) => s.fetchMe)

  useEffect(() => {
    if (token) {
      fetchMe()
    }
  }, [token, fetchMe])

  return (
    <BrowserRouter>
      <Toaster position="top-right" />
      <Routes>
        <Route path="/" element={<Navigate to="/dashboard" replace />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route
          path="/dashboard"
          element={
            <ProtectedRoute>
              <DashboardPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/trips/:tripId"
          element={
            <ProtectedRoute>
              <TripDetailPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/trips/:tripId/expenses"
          element={
            <ProtectedRoute>
              <ExpensesPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/trips/:tripId/kitty"
          element={
            <ProtectedRoute>
              <KittyPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/trips/:tripId/settlement"
          element={
            <ProtectedRoute>
              <SettlementPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/trips/:tripId/vote"
          element={
            <ProtectedRoute>
              <BudgetVotePage />
            </ProtectedRoute>
          }
        />
      </Routes>
    </BrowserRouter>
  )
}
