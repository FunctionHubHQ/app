CREATE SCHEMA IF NOT EXISTS public;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "vector";

CREATE TABLE IF NOT EXISTS public.user (
    id BIGSERIAL NOT NULL primary key,
    uid varchar(255),
    full_name varchar(255),
    first_name varchar(128),
    last_name varchar(128),
    email varchar(255),
    roles varchar(255)[],
    is_premium_user boolean NOT NULL default false,
    updated_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(uid)
);
ALTER TABLE public.user OWNER TO root;
comment on table public.user is 'Basic user information';

CREATE TABLE IF NOT EXISTS public.product (
    id BIGSERIAL NOT NULL primary key,
    product_sku varchar(255) NOT NULL,
    title varchar(255),
    seller varchar(255),
    product_url varchar(255),
    base_url varchar(255),
    image_url varchar,
    description varchar,
    price numeric(8,2),
    currency varchar(8),
    num_reviews bigint default 0,
    has_embedding boolean NOT NULL default false,
    all_reviews_scheduled boolean NOT NULL default false,
    is_ready boolean default false,
    num_questions_answered bigint default 0,
    star_rating numeric(2,1) NOT NULL default 0.0,
    updated_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(product_sku)
);
ALTER TABLE public.product OWNER TO root;

CREATE TABLE IF NOT EXISTS public.review (
    id BIGSERIAL NOT NULL,
    product_id bigint NOT NULL constraint product_id_fk references public.product (id) on delete cascade,
    title varchar(255),
    review_id varchar(255),
    merchant varchar(255),
    country varchar(255),
    verified_purchase boolean,
    upvotes bigint,
    star_rating integer,
    reviewer varchar(255),
    has_embedding boolean NOT NULL default false,
    body varchar,
    submitted_at timestamptz,
    updated_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP
  );
ALTER TABLE public.review OWNER TO root;

CREATE TABLE IF NOT EXISTS public.product_question (
    id BIGSERIAL NOT NULL,
    product_id bigint NOT NULL constraint product_id_fk references public.product (id) on delete cascade,
    question_id varchar(255) NOT NULL,
    question varchar NOT NULL,
    has_embedding boolean NOT NULL default false,
    num_answers bigint,
    upvotes bigint,
    asked_at timestamptz,
    updated_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP
);
ALTER TABLE public.product_question OWNER TO root;

CREATE TABLE IF NOT EXISTS public.product_answer (
    id BIGSERIAL NOT NULL,
    product_question_id bigint NOT NULL,
    product_id bigint NOT NULL,
    answer varchar,
    answer_id varchar(255),
    answered_by varchar(255),
    upvotes bigint default 0,
    downvotes bigint default 0,
    has_embedding boolean NOT NULL default false,
    answered_at timestamptz,
    updated_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP
    );
ALTER TABLE public.product_answer OWNER TO root;

CREATE TABLE IF NOT EXISTS public.embedding (
    id BIGSERIAL NOT NULL,
    review_id bigint NOT NULL default -1,
    question_id bigint NOT NULL default -1,
    answer_id bigint NOT NULL default -1,
    product_sku varchar(255) NOT NULL,
    product_id bigint NOT NULL,
    embedding vector(1536) NOT NULL,
    text varchar NOT NULL,
    updated_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP
);
ALTER TABLE public.embedding OWNER TO root;
comment on table public.embedding is 'Document embeddings';

CREATE TABLE IF NOT EXISTS public.feedback (
     id BIGSERIAL PRIMARY KEY NOT NULL,
     user_id bigint NOT NULL,
     subject varchar(255),
     body varchar,
     updated_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
     created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP
);

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
    product_sku varchar(255) NOT NULL,
    chat_id varchar(16),
    message varchar NOT NULL,
    is_gpt boolean NOT NULL,
    updated_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP
);
ALTER TABLE public.chat_history OWNER TO root;