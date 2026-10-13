-- BloodLink v2 schema for a NEW Neon database (PostgreSQL).
-- Optional: the app creates these tables itself (ddl-auto=update). Run this in the Neon SQL Editor if you prefer.

CREATE TABLE IF NOT EXISTS users (
  id                  BIGSERIAL PRIMARY KEY,
  name                VARCHAR(120) NOT NULL,
  email               VARCHAR(160) NOT NULL UNIQUE,
  password_hash       VARCHAR(100) NOT NULL,
  phone               VARCHAR(30)  NOT NULL,
  blood_group         VARCHAR(5)   NOT NULL,
  city                VARCHAR(80)  NOT NULL,
  address             VARCHAR(255),
  available           BOOLEAN      NOT NULL DEFAULT TRUE,
  last_donation_date  DATE,
  created_at          TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_users_match ON users (blood_group, lower(city)) WHERE available;

CREATE TABLE IF NOT EXISTS blood_requests (
  id            BIGSERIAL PRIMARY KEY,
  requester_id  BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  patient_name  VARCHAR(120) NOT NULL,
  blood_group   VARCHAR(5)   NOT NULL,
  units         INT          NOT NULL CHECK (units BETWEEN 1 AND 10),
  hospital      VARCHAR(160) NOT NULL,
  city          VARCHAR(80)  NOT NULL,
  message       VARCHAR(500),
  urgency       VARCHAR(20)  NOT NULL DEFAULT 'NORMAL',  -- NORMAL | URGENT | CRITICAL
  status        VARCHAR(20)  NOT NULL DEFAULT 'OPEN',    -- OPEN | CLOSED
  created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_requests_requester ON blood_requests (requester_id, created_at DESC);

-- One row per donor that was e-mailed about a request, tracks their answer.
CREATE TABLE IF NOT EXISTS donor_responses (
  id            BIGSERIAL PRIMARY KEY,
  request_id    BIGINT      NOT NULL REFERENCES blood_requests(id) ON DELETE CASCADE,
  donor_id      BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  status        VARCHAR(20) NOT NULL DEFAULT 'NOTIFIED', -- NOTIFIED | ACCEPTED | DECLINED
  notified_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
  responded_at  TIMESTAMPTZ,
  UNIQUE (request_id, donor_id)
);
CREATE INDEX IF NOT EXISTS idx_responses_donor ON donor_responses (donor_id, notified_at DESC);

CREATE TABLE IF NOT EXISTS notifications (
  id          BIGSERIAL PRIMARY KEY,
  user_id     BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  title       VARCHAR(160) NOT NULL,
  message     VARCHAR(500) NOT NULL,
  link        VARCHAR(200),
  is_read     BOOLEAN      NOT NULL DEFAULT FALSE,
  created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_notifications_user ON notifications (user_id, created_at DESC);
