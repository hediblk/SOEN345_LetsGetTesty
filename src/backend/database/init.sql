\set ON_ERROR_STOP on

--------------------------------------------------------
-- TABLES
--------------------------------------------------------

CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL, -- not hashed for now so i can have some dummy values inserted
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS admins (
    id SERIAL PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL, -- not hashed for now so i can have some dummy values inserted
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS events (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(100) NOT NULL,
    location VARCHAR(255) NOT NULL,
    starts_at TIMESTAMP NOT NULL,
    ends_at TIMESTAMP,
    capacity INTEGER NOT NULL CHECK (capacity >= 0),
    reserved_count INTEGER NOT NULL CHECK (reserved_count >= 0 AND reserved_count <= capacity),
    is_cancelled BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS reservations (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    event_id INTEGER NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    cancelled_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS notifications (
    id SERIAL PRIMARY KEY,
    reservation_id INTEGER NOT NULL REFERENCES reservations(id) ON DELETE CASCADE,
    channel VARCHAR(50) NOT NULL,
    destination VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    sent_at TIMESTAMP
);

--------------------------------------------------------
-- SAMPLE DATA
--------------------------------------------------------

INSERT INTO users (id, full_name, password_hash, email, phone) VALUES
    (1, 'user1', 'user123', 'user1@example.com', '514-555-5555'),
    (2, 'user2', 'user456', 'user2@example.com', '514-666-6666')
ON CONFLICT (id) DO NOTHING;

INSERT INTO admins (id, full_name, password_hash, email, phone) VALUES
    (1, 'Admin One', 'admin123', 'admin1@example.com', '514-777-7777')
ON CONFLICT (id) DO NOTHING;

INSERT INTO events (
    id,
    title,
    description,
    category,
    location,
    starts_at,
    ends_at,
    capacity,
    reserved_count,
    is_cancelled
) VALUES
    (
        1,
        'Montreal Jazz Night',
        'An evening concert featuring local and guest jazz performers.',
        'Concert',
        'Place des Arts, Montreal',
        '2026-04-18 19:30:00',
        '2026-04-18 22:00:00',
        120,
        2,
        FALSE
    )
ON CONFLICT (id) DO NOTHING;

INSERT INTO reservations (
    id,
    user_id,
    event_id,
    status,
    created_at,
    cancelled_at
) VALUES
    (1, 1, 1, 'CONFIRMED', '2026-03-15 10:30:00', NULL),
    (2, 2, 1, 'CONFIRMED', '2026-03-16 14:45:00', NULL)
ON CONFLICT (id) DO NOTHING;

INSERT INTO notifications (
    id,
    reservation_id,
    channel,
    destination,
    message,
    sent_at
) VALUES
    (
        1,
        1,
        'email',
        'user1@example.com',
        'Your reservation for Montreal Jazz Night has been confirmed.',
        '2026-03-15 10:31:00'
    ),
    (
        2,
        2,
        'sms',
        '514-666-6666',
        'Reservation confirmed for Montreal Jazz Night.',
        '2026-03-16 14:46:00'
    )
ON CONFLICT (id) DO NOTHING;

SELECT setval(pg_get_serial_sequence('users', 'id'), COALESCE((SELECT MAX(id) FROM users), 1), TRUE);
SELECT setval(pg_get_serial_sequence('admins', 'id'), COALESCE((SELECT MAX(id) FROM admins), 1), TRUE);
SELECT setval(pg_get_serial_sequence('events', 'id'), COALESCE((SELECT MAX(id) FROM events), 1), TRUE);
SELECT setval(pg_get_serial_sequence('reservations', 'id'), COALESCE((SELECT MAX(id) FROM reservations), 1), TRUE);
SELECT setval(pg_get_serial_sequence('notifications', 'id'), COALESCE((SELECT MAX(id) FROM notifications), 1), TRUE);

SELECT 'lets_get_testy tables checked and sample data inserted without dropping existing data.' AS status;
