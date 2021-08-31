CREATE SEQUENCE public.user_id_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 9223372036854775807
	START 1
	CACHE 1
	NO CYCLE;

CREATE TABLE public."user" (
	id bigint NOT NULL DEFAULT (cast( date_part( 'epoch', now() ) as INT8 ) << 23) | (nextval('user_id_seq'::regclass) % 8388608),
	first_name varchar NOT NULL,
	last_name varchar NOT NULL,
	email varchar NOT NULL,
	"password" varchar NOT NULL,
	email_verified bool NOT NULL,
	account_status varchar NOT NULL,
	meta jsonb NULL,
	deleted bool NOT NULL DEFAULT false,
	time_created timestamptz(0) NOT NULL,
	time_last_modified timestamptz(0) NOT NULL,
	CONSTRAINT user_pk PRIMARY KEY (id),
	CONSTRAINT user_email_un UNIQUE (email)
);


CREATE SEQUENCE public.otp_id_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 9223372036854775807
	START 1
	CACHE 1
	NO CYCLE;

CREATE TABLE public.otp (
	id int8 NOT NULL DEFAULT (date_part('epoch'::text, now())::bigint << 23) | (nextval('otp_id_seq'::regclass) % 8388608::bigint),
	user_id int8 NOT NULL,
	email varchar NOT NULL,
	otp_hash varchar NOT NULL,
	count int4 NOT NULL,
	last_access_time timestamptz(0) NOT NULL,
	purpose varchar NOT NULL,
	deleted bool NOT NULL DEFAULT false,
	time_created timestamptz(0) NOT NULL,
	time_last_modified timestamptz(0) NOT NULL
);
CREATE INDEX otp_query_idx ON public.otp USING btree (user_id, email, last_access_time);

ALTER TABLE public.otp ADD CONSTRAINT otp_fk FOREIGN KEY (user_id) REFERENCES "user"(id);


CREATE SEQUENCE public.role_id_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 9223372036854775807
	START 1
	CACHE 1
	NO CYCLE;

CREATE TABLE public."role" (
	id int8 NOT NULL DEFAULT (date_part('epoch'::text, now())::bigint << 23) | (nextval('role_id_seq'::regclass) % 8388608::bigint),
	user_id int8 NOT NULL,
	"role" varchar NOT NULL,
	deleted bool NOT NULL DEFAULT false,
	time_created timestamptz(0) NOT NULL,
	time_last_modified timestamptz(0) NOT NULL,
	CONSTRAINT role_un_user_id_role UNIQUE (user_id, role)
);

ALTER TABLE public."role" ADD CONSTRAINT role_fk FOREIGN KEY (user_id) REFERENCES "user"(id);
