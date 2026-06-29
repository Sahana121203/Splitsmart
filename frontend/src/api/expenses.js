import api from './axios'

export const getExpenses = (tripId, filters = {}) => {
  const params = new URLSearchParams()
  if (filters.category) params.append('category', filters.category)
  if (filters.paidBy) params.append('paidBy', filters.paidBy)
  if (filters.dateFrom) params.append('dateFrom', filters.dateFrom)
  if (filters.dateTo) params.append('dateTo', filters.dateTo)
  const qs = params.toString()
  return api.get(`/trips/${tripId}/expenses` + (qs ? '?' + qs : ''))
}

export const addExpense = (tripId, data) =>
  api.post(`/trips/${tripId}/expenses`, data)

export const getExpenseById = (tripId, expenseId) =>
  api.get(`/trips/${tripId}/expenses/${expenseId}`)

export const deleteExpense = (tripId, expenseId) =>
  api.delete(`/trips/${tripId}/expenses/${expenseId}`)

export const requestEdit = (tripId, expenseId, data) =>
  api.patch(`/trips/${tripId}/expenses/${expenseId}`, data)

export const approveEdit = (tripId, expenseId) =>
  api.post(`/trips/${tripId}/expenses/${expenseId}/approve`)

export const rejectEdit = (tripId, expenseId) =>
  api.post(`/trips/${tripId}/expenses/${expenseId}/reject`)

export const getPendingEdits = (tripId) =>
  api.get(`/trips/${tripId}/expenses/pending-edits`)
