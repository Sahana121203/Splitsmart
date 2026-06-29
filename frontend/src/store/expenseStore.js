import { create } from 'zustand'
import * as expenseApi from '../api/expenses'
import * as settlementApi from '../api/settlement'

const useExpenseStore = create((set, get) => ({
  summary: null,
  pendingEdits: [],
  settlement: null,
  settlementLoading: false,
  loading: false,
  error: null,

  fetchExpenses: async (tripId, filters) => {
    set({ loading: true, error: null })
    try {
      const res = await expenseApi.getExpenses(tripId, filters)
      set({ summary: res.data, loading: false })
    } catch (err) {
      set({
        error: err.response?.data?.error || 'Failed to load expenses',
        loading: false,
      })
    }
  },

  addExpense: async (tripId, data) => {
    const res = await expenseApi.addExpense(tripId, data)
    await get().fetchExpenses(tripId)
    return res.data
  },

  deleteExpense: async (tripId, expenseId) => {
    await expenseApi.deleteExpense(tripId, expenseId)
    await get().fetchExpenses(tripId)
  },

  requestEdit: async (tripId, expenseId, data) => {
    const res = await expenseApi.requestEdit(tripId, expenseId, data)
    await get().fetchExpenses(tripId)
    return res.data
  },

  approveEdit: async (tripId, expenseId) => {
    await expenseApi.approveEdit(tripId, expenseId)
    await get().fetchExpenses(tripId)
    await get().fetchPendingEdits(tripId)
  },

  rejectEdit: async (tripId, expenseId) => {
    await expenseApi.rejectEdit(tripId, expenseId)
    await get().fetchExpenses(tripId)
    await get().fetchPendingEdits(tripId)
  },

  fetchPendingEdits: async (tripId) => {
    try {
      const res = await expenseApi.getPendingEdits(tripId)
      set({ pendingEdits: res.data || [] })
    } catch {
      set({ pendingEdits: [] })
    }
  },

  fetchSettlement: async (tripId) => {
    set({ settlementLoading: true, error: null })
    try {
      const res = await settlementApi.previewSettlement(tripId)
      set({ settlement: res.data, settlementLoading: false })
    } catch (err) {
      set({
        error: err.response?.data?.error || 'Failed to load settlement',
        settlementLoading: false,
      })
    }
  },

  finaliseSettlement: async (tripId) => {
    const res = await settlementApi.finaliseSettlement(tripId)
    set({ settlement: res.data })
    return res.data
  },

  refreshAfterExpenseEvent: async (tripId) => {
    await get().fetchExpenses(tripId)
  },

  clearSettlement: () => set({ settlement: null }),

  clearSummary: () => set({ summary: null }),

  clearError: () => set({ error: null }),
}))

export default useExpenseStore
