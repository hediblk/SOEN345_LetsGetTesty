import { describe, it, expect, vi, beforeEach } from 'vitest'
import { fetchEvents, createEvent, updateEvent, cancelEvent } from './eventsApi'

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

describe('fetchEvents', () => {
  it('returns parsed JSON on success', async () => {
    global.fetch = mockFetch(200, [{ id: 1 }])
    expect(await fetchEvents()).toEqual([{ id: 1 }])
  })

  it('throws with the server message on failure', async () => {
    global.fetch = mockFetch(500, { message: 'Server error' })
    await expect(fetchEvents()).rejects.toThrow('Server error')
  })

  it('thrown error has the response status', async () => {
    global.fetch = mockFetch(500, { message: 'oops' })
    const err = await fetchEvents().catch(e => e)
    expect(err.status).toBe(500)
  })

  it('includes Authorization header when a session exists', async () => {
    getStoredAuthSession.mockReturnValue({ token: 'mytoken' })
    global.fetch = mockFetch(200, [])
    await fetchEvents()
    const [, options] = global.fetch.mock.calls[0]
    expect(options.headers.Authorization).toBe('Bearer mytoken')
  })

  it('omits Authorization header when no session', async () => {
    global.fetch = mockFetch(200, [])
    await fetchEvents()
    const [, options] = global.fetch.mock.calls[0]
    expect(options.headers.Authorization).toBeUndefined()
  })

  it('calls the correct URL', async () => {
    global.fetch = mockFetch(200, [])
    await fetchEvents()
    const [url] = global.fetch.mock.calls[0]
    expect(url).toBe('/api/events')
  })
})

describe('createEvent', () => {
  const payload = { title: 'Test', category: 'MOVIE' }

  it('returns parsed JSON on success', async () => {
    global.fetch = mockFetch(200, { id: 1 })
    expect(await createEvent(payload)).toEqual({ id: 1 })
  })

  it('uses POST method', async () => {
    global.fetch = mockFetch(200, { id: 1 })
    await createEvent(payload)
    const [, options] = global.fetch.mock.calls[0]
    expect(options.method).toBe('POST')
  })

  it('serializes the payload as JSON', async () => {
    global.fetch = mockFetch(200, { id: 1 })
    await createEvent(payload)
    const [, options] = global.fetch.mock.calls[0]
    expect(options.body).toBe(JSON.stringify(payload))
  })

  it('sends Content-Type header', async () => {
    global.fetch = mockFetch(200, { id: 1 })
    await createEvent(payload)
    const [, options] = global.fetch.mock.calls[0]
    expect(options.headers['Content-Type']).toBe('application/json')
  })

  it('throws with the server message on failure', async () => {
    global.fetch = mockFetch(400, { message: 'Bad request' })
    await expect(createEvent(payload)).rejects.toThrow('Bad request')
  })
})

describe('updateEvent', () => {
  const payload = { title: 'Updated' }

  it('returns parsed JSON on success', async () => {
    global.fetch = mockFetch(200, { id: 5 })
    expect(await updateEvent(5, payload)).toEqual({ id: 5 })
  })

  it('uses PUT method', async () => {
    global.fetch = mockFetch(200, { id: 5 })
    await updateEvent(5, payload)
    const [, options] = global.fetch.mock.calls[0]
    expect(options.method).toBe('PUT')
  })

  it('calls the correct URL with the event id', async () => {
    global.fetch = mockFetch(200, { id: 5 })
    await updateEvent(5, payload)
    const [url] = global.fetch.mock.calls[0]
    expect(url).toBe('/api/events/5')
  })

  it('throws with the server message on failure', async () => {
    global.fetch = mockFetch(404, { message: 'Not found' })
    await expect(updateEvent(99, payload)).rejects.toThrow('Not found')
  })
})

describe('cancelEvent', () => {
  it('returns parsed JSON on success', async () => {
    global.fetch = mockFetch(200, { id: 3, isCancelled: true })
    expect(await cancelEvent(3)).toEqual({ id: 3, isCancelled: true })
  })

  it('uses PATCH method', async () => {
    global.fetch = mockFetch(200, {})
    await cancelEvent(3)
    const [, options] = global.fetch.mock.calls[0]
    expect(options.method).toBe('PATCH')
  })

  it('calls the correct cancel URL', async () => {
    global.fetch = mockFetch(200, {})
    await cancelEvent(3)
    const [url] = global.fetch.mock.calls[0]
    expect(url).toBe('/api/events/3/cancel')
  })

  it('throws with the server message on failure', async () => {
    global.fetch = mockFetch(403, { message: 'Forbidden' })
    await expect(cancelEvent(3)).rejects.toThrow('Forbidden')
  })
})
