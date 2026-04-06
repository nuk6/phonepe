-- version column for optimistic locking
ALTER TABLE sips ADD COLUMN version INT NOT NULL DEFAULT 0;

-- audit timestamps on all tables
ALTER TABLE users ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE users ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT now();

ALTER TABLE mutual_funds ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE mutual_funds ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT now();

ALTER TABLE sips ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE sips ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT now();

ALTER TABLE sip_installments ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT now();

-- idempotency key on installments to prevent double-charge
ALTER TABLE sip_installments ADD COLUMN idempotency_key VARCHAR(128);
CREATE UNIQUE INDEX idx_installment_idempotency ON sip_installments(idempotency_key);

