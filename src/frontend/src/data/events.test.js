import { describe, it, expect } from 'vitest'
import {
  formatEventDate,
  mapEventFromApi,
  buildCreateEventPayload,
  buildUpdateEventPayload,
  buildEventFormFromEvent,
} from './events'

describe('formatEventDate', () => {
  it('returns empty string for null', () => {
    expect(formatEventDate(null)).toBe('')
  })

  it('returns empty string for empty string', () => {
    expect(formatEventDate('')).toBe('')
  })

  it('formats a calendar date string (YYYY-MM-DD)', () => {
    const result = formatEventDate('2025-06-15')
    expect(result).toMatch(/2025/)
    expect(result).toMatch(/jun/i)
  })

  it('formats an ISO datetime string', () => {
    const result = formatEventDate('2025-06-15T18:00:00')
    expect(result).toMatch(/2025/)
    expect(result).toMatch(/jun/i)
  })

  it('returns a non-empty string for a valid date', () => {
    expect(formatEventDate('2025-01-01')).not.toBe('')
  })
})

describe('mapEventFromApi', () => {
  const baseApi = {
    id: 1,
    title: 'Test Event',
    description: 'A description',
    location: 'Montreal',
    capacity: 100,
    price: 25.0,
    category: 'MOVIE',
    startsAt: '2025-06-15T18:00:00',
    isCancelled: false,
  }

  it('maps all fields correctly', () => {
    expect(mapEventFromApi(baseApi)).toEqual({
      id: 1,
      title: 'Test Event',
      description: 'A description',
      location: 'Montreal',
      capacity: 100,
      price: 25.0,
      category: 'Movies',
      date: '2025-06-15',
      isCancelled: false,
    })
  })

  it('defaults description to empty string when null', () => {
    expect(mapEventFromApi({ ...baseApi, description: null }).description).toBe('')
  })

  it('defaults price to 0 when missing', () => {
    const { price, ...rest } = baseApi
    expect(mapEventFromApi(rest).price).toBe(0)
  })

  it('uses the raw category string when not in CATEGORY_LABEL', () => {
    expect(mapEventFromApi({ ...baseApi, category: 'UNKNOWN' }).category).toBe('UNKNOWN')
  })

  it('maps known API categories to their labels', () => {
    expect(mapEventFromApi({ ...baseApi, category: 'CONCERT' }).category).toBe('Concerts')
    expect(mapEventFromApi({ ...baseApi, category: 'SPORT' }).category).toBe('Sports')
    expect(mapEventFromApi({ ...baseApi, category: 'TRAVEL' }).category).toBe('Travel')
  })

  it('extracts the date portion from startsAt', () => {
    expect(mapEventFromApi(baseApi).date).toBe('2025-06-15')
  })

  it('sets date to empty string when startsAt is missing', () => {
    const { startsAt, ...rest } = baseApi
    expect(mapEventFromApi(rest).date).toBe('')
  })

  it('sets isCancelled to false when not explicitly true', () => {
    expect(mapEventFromApi({ ...baseApi, isCancelled: undefined }).isCancelled).toBe(false)
    expect(mapEventFromApi({ ...baseApi, isCancelled: false }).isCancelled).toBe(false)
  })

  it('sets isCancelled to true when explicitly true', () => {
    expect(mapEventFromApi({ ...baseApi, isCancelled: true }).isCancelled).toBe(true)
  })
})

describe('buildCreateEventPayload', () => {
  const validForm = {
    title: '  My Event  ',
    category: 'Movies',
    location: '  Montreal  ',
    date: '2025-06-15',
    capacity: '100',
    price: '25',
  }

  it('returns a correct payload for a valid form', () => {
    expect(buildCreateEventPayload(validForm)).toEqual({
      title: 'My Event',
      description: null,
      category: 'MOVIE',
      location: 'Montreal',
      startsAt: '2025-06-15T18:00:00',
      endsAt: null,
      capacity: 100,
      price: 25,
    })
  })

  it('trims whitespace from title and location', () => {
    const result = buildCreateEventPayload(validForm)
    expect(result.title).toBe('My Event')
    expect(result.location).toBe('Montreal')
  })

  it('converts capacity and price to numbers', () => {
    const result = buildCreateEventPayload(validForm)
    expect(typeof result.capacity).toBe('number')
    expect(typeof result.price).toBe('number')
  })

  it('throws for an invalid category', () => {
    expect(() => buildCreateEventPayload({ ...validForm, category: 'InvalidCat' })).toThrow('Invalid category.')
  })

  it('maps all valid category options correctly', () => {
    const cases = [
      ['Movies', 'MOVIE'],
      ['Concerts', 'CONCERT'],
      ['Sports', 'SPORT'],
      ['Travel', 'TRAVEL'],
    ]
    for (const [cat, expected] of cases) {
      expect(buildCreateEventPayload({ ...validForm, category: cat }).category).toBe(expected)
    }
  })

  it('formats the startsAt timestamp from the date field', () => {
    const result = buildCreateEventPayload({ ...validForm, date: '2025-12-31' })
    expect(result.startsAt).toBe('2025-12-31T18:00:00')
  })

  it('always sets description and endsAt to null', () => {
    const result = buildCreateEventPayload(validForm)
    expect(result.description).toBeNull()
    expect(result.endsAt).toBeNull()
  })
})

describe('buildUpdateEventPayload', () => {
  const form = { title: 'T', category: 'Concerts', location: 'L', date: '2025-01-01', capacity: '10', price: '0' }

  it('produces the same result as buildCreateEventPayload', () => {
    expect(buildUpdateEventPayload(form)).toEqual(buildCreateEventPayload(form))
  })
})

describe('buildEventFormFromEvent', () => {
  it('maps event fields to form fields', () => {
    const event = { title: 'T', date: '2025-06-15', category: 'Movies', location: 'MTL', price: 10, capacity: 50 }
    expect(buildEventFormFromEvent(event)).toEqual({
      title: 'T',
      date: '2025-06-15',
      category: 'Movies',
      location: 'MTL',
      price: '10',
      capacity: '50',
    })
  })

  it('returns defaults when event is null', () => {
    expect(buildEventFormFromEvent(null)).toEqual({
      title: '',
      date: '',
      category: 'Movies',
      location: '',
      price: '0',
      capacity: '50',
    })
  })

  it('returns defaults when event is undefined', () => {
    const result = buildEventFormFromEvent(undefined)
    expect(result.title).toBe('')
    expect(result.category).toBe('Movies')
    expect(result.price).toBe('0')
    expect(result.capacity).toBe('50')
  })

  it('converts price and capacity to strings', () => {
    const event = { title: 'T', date: '2025-01-01', category: 'Sports', location: 'L', price: 99, capacity: 200 }
    const result = buildEventFormFromEvent(event)
    expect(result.price).toBe('99')
    expect(result.capacity).toBe('200')
  })
})
