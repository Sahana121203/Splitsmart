import api from './axios'

export const createTrip = (data) =>
  api.post('/trips', data)

export const getMyTrips = () =>
  api.get('/trips')

export const getTripById = (tripId) =>
  api.get(`/trips/${tripId}`)

export const getTripMembers = (tripId) =>
  api.get(`/trips/${tripId}/members`)

export const updateTripStatus =
  (tripId, newStatus) =>
    api.patch(`/trips/${tripId}/status`,
      { newStatus })

export const inviteMember = (tripId, data) =>
  api.post(`/trips/${tripId}/invite`, data)

export const removeMember = (tripId, userId) =>
  api.delete(
    `/trips/${tripId}/members/${userId}`)
