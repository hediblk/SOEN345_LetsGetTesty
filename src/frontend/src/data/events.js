export const CATEGORY_OPTIONS = ['Movies', 'Concerts', 'Travel', 'Sports']

export const CATEGORY_TO_API = {
  Movies: 'MOVIE',
  Concerts: 'CONCERT',
  Sports: 'SPORT',
  Travel: 'TRAVEL',
}

export const CATEGORY_LABEL = {
  MOVIE: 'Movies',
  CONCERT: 'Concerts',
  SPORT: 'Sports',
  TRAVEL: 'Travel',
  THEATRE: 'Theatre',
  ART: 'Art',
  OTHERS: 'Others',
}

export function formatEventDate(iso) {
  if (!iso) return ''
  const s = String(iso).trim()
  const cal = /^(\d{4})-(\d{2})-(\d{2})$/.exec(s)
  if (cal) {
    const y = Number(cal[1])
    const m = Number(cal[2])
    const d = Number(cal[3])
    return new Date(y, m - 1, d).toLocaleDateString('en-CA', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
    })
  }
  return new Date(s).toLocaleDateString('en-CA', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  })
}

export function mapEventFromApi(apiEvent) {
  const startsAt = apiEvent.startsAt || ''
  const date = startsAt.length >= 10 ? startsAt.slice(0, 10) : ''
  const apiCategory = apiEvent.category || ''
  return {
    id: apiEvent.id,
    title: apiEvent.title,
    location: apiEvent.location,
    capacity: apiEvent.capacity,
    price: apiEvent.price ?? 0,
    category: CATEGORY_LABEL[apiCategory] || apiCategory,
    date,
  }
}

export function buildCreateEventPayload(form) {
  const category = CATEGORY_TO_API[form.category]
  if (!category) {
    throw new Error('Invalid category.')
  }
  return {
    title: form.title.trim(),
    description: null,
    category,
    location: form.location.trim(),
    startsAt: `${form.date}T18:00:00`,
    endsAt: null,
    capacity: Number(form.capacity),
    price: Number(form.price),
  }
}
