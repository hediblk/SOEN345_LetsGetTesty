import { describe, it, expect } from 'vitest'
import { mapReservationFromApi, buildCreateReservationPayload } from './reservations'

describe('mapReservationFromApi', () => {
  const baseApi = {
    id: 1,
    userId: 10,
    eventId: 20,
    status: 'CONFIRMED',
    createdAt: '2025-01-01T00:00:00',
    cancelledAt: null,
  }

  it('maps all fields correctly', () => {
    expect(mapReservationFromApi(baseApi)).toEqual({
      id: 1,
      userId: 10,
      eventId: 20,
      status: 'CONFIRMED',
      createdAt: '2025-01-01T00:00:00',
      cancelledAt: null,
      isCancelled: false,
    })
  })

  it('sets isCancelled to true for CANCELLED status', () => {
    const result = mapReservationFromApi({ ...baseApi, status: 'CANCELLED', cancelledAt: '2025-02-01T00:00:00' })
    expect(result.isCancelled).toBe(true)
    expect(result.status).toBe('CANCELLED')
  })

  it('sets isCancelled to false for CONFIRMED status', () => {
    expect(mapReservationFromApi(baseApi).isCancelled).toBe(false)
  })

  it('uses the raw status string for unknown status values', () => {
    expect(mapReservationFromApi({ ...baseApi, status: 'PENDING' }).status).toBe('PENDING')
  })

  it('defaults createdAt to empty string when missing', () => {
    const { createdAt, ...rest } = baseApi
    expect(mapReservationFromApi(rest).createdAt).toBe('')
  })

  it('defaults cancelledAt to null when missing', () => {
    const { cancelledAt, ...rest } = baseApi
    expect(mapReservationFromApi(rest).cancelledAt).toBeNull()
  })
})

describe('buildCreateReservationPayload', () => {
  it('returns an object with userId and eventId', () => {
    expect(buildCreateReservationPayload(5, 10)).toEqual({ userId: 5, eventId: 10 })
  })

  it('preserves the exact values passed in', () => {
    const result = buildCreateReservationPayload(42, 99)
    expect(result.userId).toBe(42)
    expect(result.eventId).toBe(99)
  })
})
