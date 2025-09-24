-- Allow NULL booking_id so event-only payments can be created
ALTER TABLE payments
  MODIFY COLUMN booking_id BIGINT NULL;
