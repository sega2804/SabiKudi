CREATE TABLE users (
    id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    phone         text NOT NULL UNIQUE CHECK (phone ~ '^\+[1-9][0-9]{7,14}$'),
    display_name  text,
    created_at    timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE devices (
    id                    uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id               uuid NOT NULL REFERENCES users(id),
    model                 text,
    fcm_token             text,
    integrity_status      text NOT NULL DEFAULT 'UNKNOWN' CHECK (integrity_status IN ('VERIFIED', 'UNVERIFIED', 'UNKNOWN')),
    integrity_checked_at  timestamptz,
    last_seen_at          timestamptz NOT NULL DEFAULT now(),
    created_at            timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE workspaces (
    id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    name          text NOT NULL,
    mode          text NOT NULL CHECK (mode IN ('BUSINESS', 'SHARED')),
    invite_code   text NOT NULL UNIQUE CHECK (length(invite_code) >= 6),
    created_at    timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE memberships (
    id                        uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    workspace_id              uuid NOT NULL REFERENCES workspaces(id),
    user_id                   uuid NOT NULL REFERENCES users(id),
    device_id                 uuid REFERENCES devices(id),
    role                      text NOT NULL CHECK (role IN ('OWNER', 'ORGANISER', 'STAFF', 'MEMBER')),
    perms                     jsonb NOT NULL DEFAULT '[]'::jsonb,
    status                    text NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('INVITED', 'PENDING', 'ACTIVE', 'SUSPENDED', 'LOCKED', 'REMOVED')),
    auto_approve_limit_kobo   bigint NOT NULL DEFAULT 0 CHECK (auto_approve_limit_kobo >= 0),
    created_at                timestamptz NOT NULL DEFAULT now(),
    UNIQUE (workspace_id, user_id)
);

CREATE TABLE audit_events (
    id            bigserial PRIMARY KEY,
    workspace_id  uuid REFERENCES workspaces(id),
    user_id       uuid REFERENCES users(id),
    device_id     uuid REFERENCES devices(id),
    action        text NOT NULL,
    payload       jsonb NOT NULL DEFAULT '{}'::jsonb,
    created_at    timestamptz NOT NULL DEFAULT now()
);

CREATE OR REPLACE FUNCTION forbid_mutation() RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'audit_events is append-only';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_audit_events_immutable
    BEFORE UPDATE OR DELETE ON audit_events
    FOR EACH ROW EXECUTE FUNCTION forbid_mutation();
