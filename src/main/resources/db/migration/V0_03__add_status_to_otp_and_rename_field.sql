ALTER TABLE public.otp ADD otp_status varchar NOT NULL;

ALTER TABLE public.otp RENAME COLUMN last_access_time TO valid_after;
