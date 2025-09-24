-- Re-apply change as baseline was set to version 1
ALTER TABLE payments
  MODIFY COLUMN booking_id BIGINT NULL;
