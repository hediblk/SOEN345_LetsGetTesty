\set ON_ERROR_STOP on

--------------------------------------------------------
-- TABLES
--------------------------------------------------------

CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL, -- not hashed for now so i can have some dummy values inserted
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(50),
    CONSTRAINT users_exactly_one_contact_chk
        CHECK (num_nonnulls(email, phone) = 1)
);

CREATE TABLE IF NOT EXISTS admins (
    id SERIAL PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL, -- not hashed for now so i can have some dummy values inserted
    email VARCHAR(255),
    phone VARCHAR(50),
    CONSTRAINT admins_exactly_one_contact_chk
        CHECK (num_nonnulls(email, phone) = 1)
);

ALTER TABLE users ALTER COLUMN password_hash SET NOT NULL;
ALTER TABLE admins ALTER COLUMN password_hash SET NOT NULL;
ALTER TABLE users ALTER COLUMN email DROP NOT NULL;
ALTER TABLE admins ALTER COLUMN email DROP NOT NULL;

UPDATE users
SET phone = NULL
WHERE email IS NOT NULL AND phone IS NOT NULL;

UPDATE admins
SET phone = NULL
WHERE email IS NOT NULL AND phone IS NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'users_exactly_one_contact_chk'
    ) THEN
        ALTER TABLE users
        ADD CONSTRAINT users_exactly_one_contact_chk
        CHECK (num_nonnulls(email, phone) = 1);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'admins_exactly_one_contact_chk'
    ) THEN
        ALTER TABLE admins
        ADD CONSTRAINT admins_exactly_one_contact_chk
        CHECK (num_nonnulls(email, phone) = 1);
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS events (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(100) NOT NULL,
    location VARCHAR(255) NOT NULL,
    starts_at TIMESTAMP NOT NULL,
    ends_at TIMESTAMP,
    capacity INTEGER NOT NULL CHECK (capacity >= 0),
    price INTEGER NOT NULL DEFAULT 0 CHECK (price >= 0),
    reserved_count INTEGER NOT NULL CHECK (reserved_count >= 0 AND reserved_count <= capacity),
    is_cancelled BOOLEAN NOT NULL DEFAULT FALSE
);

ALTER TABLE events ADD COLUMN IF NOT EXISTS price INTEGER NOT NULL DEFAULT 0;

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
    (1, 'user1', 'user123', 'user1@example.com', NULL),
    (2, 'user2', 'user456', NULL, '514-666-6666')
ON CONFLICT (id) DO NOTHING;

INSERT INTO admins (id, full_name, password_hash, email, phone) VALUES
    (1, 'Admin One', 'admin123', 'admin1@example.com', NULL)
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
    price,
    reserved_count,
    is_cancelled
) VALUES
    (
        1,
        'Dune: Messiah - World Premiere',
        NULL,
        'MOVIE',
        'Cinema Imperial, Montreal',
        '2026-04-15 18:00:00',
        '2026-04-15 22:00:00',
        280,
        22,
        0,
        FALSE
    ),
    (
        2,
        'Montreal Jazz Night',
        'An evening concert featuring local and guest jazz performers.',
        'CONCERT',
        'Place des Arts, Montreal',
        '2026-04-18 19:30:00',
        '2026-04-18 22:00:00',
        118,
        45,
        2,
        FALSE
    ),
    (
        3,
        'Canadiens vs Maple Leafs',
        NULL,
        'SPORT',
        'Bell Centre, Montreal',
        '2026-04-22 18:00:00',
        '2026-04-22 21:00:00',
        312,
        89,
        0,
        FALSE
    ),
    (
        4,
        'Paris Weekend Getaway',
        NULL,
        'TRAVEL',
        'Departure: Montreal-Trudeau',
        '2026-04-25 18:00:00',
        NULL,
        60,
        699,
        0,
        FALSE
    ),
    (
        5,
        'Arcade Fire - Live',
        NULL,
        'CONCERT',
        'Bell Centre, Montreal',
        '2026-05-10 18:00:00',
        '2026-05-10 22:00:00',
        54,
        120,
        0,
        FALSE
    ),
    (
        6,
        'Blade Runner 2099 Screening',
        NULL,
        'MOVIE',
        'Cineplex Forum, Montreal',
        '2026-05-12 18:00:00',
        '2026-05-12 21:00:00',
        150,
        18,
        0,
        FALSE
    ),
    (
        7,
        'NYC Long Weekend Package',
        NULL,
        'TRAVEL',
        'Departure: Montreal-Trudeau',
        '2026-05-16 18:00:00',
        NULL,
        45,
        399,
        0,
        FALSE
    ),
    (
        8,
        'Grand Prix de Montreal',
        NULL,
        'SPORT',
        'Circuit Gilles Villeneuve',
        '2026-06-07 18:00:00',
        '2026-06-07 21:00:00',
        890,
        200,
        0,
        TRUE
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
    (1, 1, 2, 'CONFIRMED', '2026-03-15 10:30:00', NULL),
    (2, 2, 2, 'CONFIRMED', '2026-03-16 14:45:00', NULL),
    (8, 1, 1, 'CANCELLED', '2026-03-16 14:45:00', '2026-03-16 14:45:00')
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
