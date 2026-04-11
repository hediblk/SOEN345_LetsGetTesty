import { describe, it, expect, vi, beforeEach } from 'vitest'
import {
  fetchReservations,
  fetchReservationsByUser,
  fetchReservationsByEvent,
  createReservation,
  cancelReservation,
} from './reservationsApi'

vi.mock('../auth/authStorage', () => ({
  getStoredAuthSession: vi.fn(),
}))

import { getStoredAuthSession } from '../auth/authStorage'

function mockFetch(status, body) {
  return vi.fn().mockResolvedValue({
    ok: status >= 200 && status < 300,
    status,
    json: () => Promise.resolve(body),
  })
}

beforeEach(() => {
  vi.resetAllMocks()
  getStoredAuthSession.mockReturnValue(null)
})

describe('fetchReservations', () => {
  it('returns parsed JSON on success', async () => {
    global.fetch = mockFetch(200, [{ id: 1 }])
    expect(await fetchReservations()).toEqual([{ id: 1 }])
  })

  it('throws with the server message on failure', async () => {
    global.fetch = mockFetch(401, { message: 'Unauthorized' })
    await expect(fetchReservations()).rejects.toThrow('Unauthorized')
  })

  it('thrown error has the response status', async () => {
    global.fetch = mockFetch(401, { message: 'Unauthorized' })
    const err = await fetchReservations().catch(e => e)
    expect(err.status).toBe(401)
  })

  it('calls the correct URL', async () => {
    global.fetch = mockFetch(200, [])
    await fetchReservations()
    const [url] = global.fetch.mock.calls[0]
    expect(url).toBe('/api/reservations')
  })
})

describe('fetchReservationsByUser', () => {
  it('calls the correct URL with the user id', async () => {
    global.fetch = mockFetch(200, [])
    await fetchReservationsByUser(42)
    const [url] = global.fetch.mock.calls[0]
    expect(url).toBe('/api/reservations/user/42')
  })

  it('returns parsed JSON on success', async () => {
    global.fetch = mockFetch(200, [{ id: 1, userId: 42 }])
    expect(await fetchReservationsByUser(42)).toEqual([{ id: 1, userId: 42 }])
  })

  it('throws with the server message on failure', async () => {
    global.fetch = mockFetch(500, { message: 'Server error' })
    await expect(fetchReservationsByUser(1)).rejects.toThrow('Server error')
  })
})

describe('fetchReservationsByEvent', () => {
  it('calls the correct URL with the event id', async () => {
    global.fetch = mockFetch(200, [])
    await fetchReservationsByEvent(7)
    const [url] = global.fetch.mock.calls[0]
    expect(url).toBe('/api/reservations/event/7')
  })

  it('returns parsed JSON on success', async () => {
    global.fetch = mockFetch(200, [{ id: 2, eventId: 7 }])
    expect(await fetchReservationsByEvent(7)).toEqual([{ id: 2, eventId: 7 }])
  })

  it('throws with the server message on failure', async () => {
    global.fetch = mockFetch(404, { message: 'Not found' })
    await expect(fetchReservationsByEvent(99)).rejects.toThrow('Not found')
  })
})

describe('createReservation', () => {
  const payload = { userId: 1, eventId: 2 }

  it('returns parsed JSON on success', async () => {
    global.fetch = mockFetch(200, { id: 10 })
    expect(await createReservation(payload)).toEqual({ id: 10 })
  })

  it('uses POST method', async () => {
    global.fetch = mockFetch(200, { id: 10 })
    await createReservation(payload)
    const [, options] = global.fetch.mock.calls[0]
    expect(options.method).toBe('POST')
  })

  it('serializes the payload as JSON', async () => {
    global.fetch = mockFetch(200, { id: 10 })
    await createReservation(payload)
    const [, options] = global.fetch.mock.calls[0]
    expect(options.body).toBe(JSON.stringify(payload))
  })

  it('throws with the server message on failure', async () => {
    global.fetch = mockFetch(409, { message: 'Already reserved' })
    await expect(createReservation(payload)).rejects.toThrow('Already reserved')
  })
})

describe('cancelReservation', () => {
  it('returns parsed JSON on success', async () => {
    global.fetch = mockFetch(200, { id: 5, status: 'CANCELLED' })
    expect(await cancelReservation(5)).toEqual({ id: 5, status: 'CANCELLED' })
  })

  it('uses PATCH method', async () => {
    global.fetch = mockFetch(200, {})
    await cancelReservation(5)
    const [, options] = global.fetch.mock.calls[0]
    expect(options.method).toBe('PATCH')
  })

  it('calls the correct cancel URL', async () => {
    global.fetch = mockFetch(200, {})
    await cancelReservation(5)
    const [url] = global.fetch.mock.calls[0]
    expect(url).toBe('/api/reservations/5/cancel')
  })

  it('throws with the server message on failure', async () => {
    global.fetch = mockFetch(400, { message: 'Cannot cancel' })
    await expect(cancelReservation(5)).rejects.toThrow('Cannot cancel')
  })
})
