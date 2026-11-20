-- Recovery email for SIM-swap protection.
ALTER TABLE users
    ADD COLUMN recovery_email text,
    ADD COLUMN recovery_email_verified_at timestamptz;

-- Devices are now identified by the app's installation id.
ALTER TABLE devices
    ADD COLUMN installation_id uuid NOT NULL,
    ADD COLUMN full_access_from timestamptz NOT NULL DEFAULT now();
CREATE UNIQUE INDEX uq_devices_user_installation ON devices(user_id, installation_id);

-- One row per code that was sent. Codes are stored only as keyed hashes.
CREATE TABLE otp_challenges (
    id               uuid PRIMARY KEY,
    purpose          text NOT NULL CHECK (purpose IN ('SIGN_IN','STEP_UP_EMAIL','VERIFY_EMAIL')),
    target           text NOT NULL,                 -- E.164 phone number or email address
    channel          text NOT NULL CHECK (channel IN ('SMS','WHATSAPP','EMAIL')),
    code_hash        text NOT NULL,
    installation_id  uuid NOT NULL,
    user_id          uuid REFERENCES users(id),
    attempts         int  NOT NULL DEFAULT 0,
    max_attempts     int  NOT NULL DEFAULT 5,
    created_at       timestamptz NOT NULL DEFAULT now(),
    expires_at       timestamptz NOT NULL,
    consumed_at      timestamptz
);
CREATE INDEX idx_otp_target ON otp_challenges(purpose, target, created_at DESC);

-- Refresh tokens rotate. Presenting an already-used token revokes its whole family.
CREATE TABLE refresh_tokens (
    id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     uuid NOT NULL REFERENCES users(id),
    device_id   uuid NOT NULL REFERENCES devices(id),
    family_id   uuid NOT NULL,
    token_hash  text NOT NULL UNIQUE,
    created_at  timestamptz NOT NULL DEFAULT now(),
    expires_at  timestamptz NOT NULL,
    used_at     timestamptz,
    revoked_at  timestamptz
);
CREATE INDEX idx_refresh_device ON refresh_tokens(device_id);
CREATE INDEX idx_refresh_family ON refresh_tokens(family_id);
