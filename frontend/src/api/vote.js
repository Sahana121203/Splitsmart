import api from './axios'

export const submitVote = (tripId, maxBudget) =>
  api.post(`/trips/${tripId}/budget-vote`,
    { maxBudget })

export const getVoteStatus = (tripId) =>
  api.get(
    `/trips/${tripId}/budget-vote/status`)

export const getVoteResult = (tripId) =>
  api.get(
    `/trips/${tripId}/budget-vote/result`)

export const closeVote = (tripId) =>
  api.post(
    `/trips/${tripId}/budget-vote/close`, {})
