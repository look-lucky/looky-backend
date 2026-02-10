-- Enable Event Scheduler (Ensure it's ON)
SET GLOBAL event_scheduler = ON;

-- Drop event if exists to avoid conflicts
DROP EVENT IF EXISTS RESET_EXPIRED_COUPON_ACTIVATION;

-- Create Event
CREATE EVENT RESET_EXPIRED_COUPON_ACTIVATION
ON SCHEDULE EVERY 1 MINUTE
DO
    UPDATE student_coupon
    SET status = 'UNUSED',
        verification_code = NULL,
        activated_at = NULL
    WHERE status = 'ACTIVATED'
      AND activated_at < DATE_SUB(NOW(), INTERVAL 30 MINUTE);
