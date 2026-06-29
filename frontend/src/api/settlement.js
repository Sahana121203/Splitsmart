import api from './axios'

export const previewSettlement = (tripId) =>
  api.get(`/trips/${tripId}/settlement/preview`)

export const finaliseSettlement = (tripId) =>
  api.post(`/trips/${tripId}/settlement/finalise`)

export const getSettlementResult = (tripId) =>
  api.get(`/trips/${tripId}/settlement/result`)
