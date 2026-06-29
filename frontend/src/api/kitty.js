import api from './axios'

export const getKittyStatus = (tripId) =>
  api.get(`/trips/${tripId}/kitty`)

export const depositToKitty = (tripId, data) =>
  api.post(`/trips/${tripId}/kitty/deposit`,
    data)

export const getDepositHistory = (tripId) =>
  api.get(`/trips/${tripId}/kitty/history`)

export const updateKittyTarget =
  (tripId, newTarget) =>
    api.patch(`/trips/${tripId}/kitty/target`,
      { newTarget })
