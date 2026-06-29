import { create } from 'zustand'
import * as tripApi from '../api/trips'
import * as kittyApi from '../api/kitty'
import * as voteApi from '../api/vote'

const useTripStore = create((set, get) => ({
  trips: [],
  currentTrip: null,
  members: [],
  kitty: null,
  voteStatus: null,
  voteResult: null,
  loading: false,
  error: null,

  fetchMyTrips: async () => {
    set({ loading: true, error: null })
    try {
      const res = await tripApi.getMyTrips()
      set({ trips: res.data || [],
        loading: false })
    } catch (err) {
      set({
        error: err.response?.data?.error
          || 'Failed to load trips',
        loading: false
      })
    }
  },

  fetchTrip: async (tripId) => {
    set({ loading: true, error: null })
    try {
      const [tripRes, membersRes] =
        await Promise.all([
          tripApi.getTripById(tripId),
          tripApi.getTripMembers(tripId)
        ])
      set({
        currentTrip: tripRes.data,
        members: membersRes.data || [],
        loading: false
      })
    } catch (err) {
      set({
        error: err.response?.data?.error
          || 'Failed to load trip',
        loading: false
      })
    }
  },

  createTrip: async (data) => {
    const res = await tripApi.createTrip(data)
    const newTrip = res.data
    set(state => ({
      trips: [newTrip, ...state.trips]
    }))
    return newTrip
  },

  updateStatus: async (tripId, newStatus) => {
    const res = await tripApi.updateTripStatus(
      tripId, newStatus)
    set(state => ({
      currentTrip: res.data,
      trips: state.trips.map(t =>
        t.tripId === tripId ? res.data : t)
    }))
    return res.data
  },

  inviteMember: async (tripId, data) => {
    const res =
      await tripApi.inviteMember(tripId, data)
    await get().fetchTrip(tripId)
    return res
  },

  removeMember: async (tripId, userId) => {
    await tripApi.removeMember(tripId, userId)
    await get().fetchTrip(tripId)
  },

  fetchKitty: async (tripId) => {
    try {
      const res =
        await kittyApi.getKittyStatus(tripId)
      set({ kitty: res.data })
    } catch {}
  },

  deposit: async (tripId, data) => {
    const res =
      await kittyApi.depositToKitty(tripId, data)
    set({ kitty: res.data })
    return res.data
  },

  fetchVoteStatus: async (tripId) => {
    try {
      const res =
        await voteApi.getVoteStatus(tripId)
      set({ voteStatus: res.data })
    } catch {}
  },

  fetchVoteResult: async (tripId) => {
    try {
      const res =
        await voteApi.getVoteResult(tripId)
      set({ voteResult: res.data })
    } catch {}
  },

  submitVote: async (tripId, maxBudget) => {
    const res =
      await voteApi.submitVote(tripId, maxBudget)
    set({ voteStatus: res.data })
    return res.data
  },

  closeVote: async (tripId) => {
    const res = await voteApi.closeVote(tripId)
    set({ voteStatus: res.data })
  },

  setCurrentTrip: (trip) =>
    set({ currentTrip: trip }),

  updateKittyFromEvent: (event) =>
    set(state => ({
      kitty: state.kitty ? {
        ...state.kitty,
        kittyBalance: event.kittyBalance,
        kittyFundedPercent:
          event.kittyFundedPercent
      } : state.kitty,
      currentTrip: state.currentTrip ? {
        ...state.currentTrip,
        kittyBalance: event.kittyBalance,
        kittyFundedPercent: event.kittyFundedPercent
      } : state.currentTrip
    })),

  clearError: () =>
    set({ error: null })
}))

export default useTripStore
