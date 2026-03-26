export const STATUS_LABEL = {
  CONFIRMED: 'CONFIRMED',
  CANCELLED: 'CANCELLED',
}

export function mapReservationFromApi(apiReservation) {
  return {
    id: apiReservation.id,
    userId: apiReservation.userId,
    eventId: apiReservation.eventId,
    status: STATUS_LABEL[apiReservation.status] || apiReservation.status || '',
    createdAt: apiReservation.createdAt || '',
    cancelledAt: apiReservation.cancelledAt || null,
    isCancelled: apiReservation.status === 'CANCELLED',
  }
}

export function buildCreateReservationPayload(userId, eventId) {
  return {
    userId,
    eventId,
  }
}