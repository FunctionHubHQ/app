CREATE SCHEMA IF NOT EXISTS public;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS public.user (
    id BIGSERIAL NOT NULL primary key,
    uid varchar(255),
    handle varchar(255),
    full_name varchar(255),
    email varchar(255),
    roles varchar(255)[],
    is_premium_user boolean NOT NULL default false,
    updated_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(uid)
);
ALTER TABLE public.user OWNER TO root;
comment on table public.user is 'Basic user information';

CREATE TABLE IF NOT EXISTS public.code_cell (
    uid uuid NOT NULL primary key,
    parent_id uuid,
    user_id varchar(255) NOT NULL,
    function_name varchar(64),
    description varchar(255),
    code varchar,
    request_dto varchar,
    response_dto varchar,
    version varchar(32),
    slug varchar(32),
    is_active boolean NOT NULL default false,
    is_public boolean NOT NULL default false,
    updated_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(slug)
);
ALTER TABLE public.code_cell OWNER TO root;

CREATE TABLE IF NOT EXISTS public.commit_history (
    uid uuid NOT NULL primary key,
    code_cell_id uuid NOT NULL,
    version varchar(32),
    message varchar(255),
    created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP
);
ALTER TABLE public.commit_history OWNER TO root;

CREATE TABLE IF NOT EXISTS public.fcm_token (
    id BIGSERIAL PRIMARY KEY NOT NULL,
    user_id bigint,
    fcm_token varchar(255) NOT NULL,
    updated_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS public.chat_history (
    id BIGSERIAL PRIMARY KEY NOT NULL,
    fcm_token_id bigint NOT NULL,
    code_cell_uid uuid NOT NULL,
    chat_id varchar(16),
    message varchar NOT NULL,
    is_gpt boolean NOT NULL,
    updated_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP
);
ALTER TABLE public.chat_history OWNER TO root;