DROP INDEX public.otp_query_idx;
CREATE INDEX otp_query_idx ON public.otp (user_id,email,valid_after,purpose,otp_status);
