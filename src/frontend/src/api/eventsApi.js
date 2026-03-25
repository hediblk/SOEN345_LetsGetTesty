const EVENTS_URL = '/api/events'

async function readErrorMessage(response) {
  try {
    const body = await response.json()
    if (body && typeof body.message === 'string') {
      return body.message
    }
  } catch {
    // ignore
  }
  return `Request failed (${response.status})`
}

export async function fetchEvents() {
  const res = await fetch(EVENTS_URL)
  if (!res.ok) {
    throw new Error(await readErrorMessage(res))
  }
  return res.json()
}

export async function createEvent(payload) {
  const res = await fetch(EVENTS_URL, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  })
  if (!res.ok) {
    throw new Error(await readErrorMessage(res))
  }
  return res.json()
}
