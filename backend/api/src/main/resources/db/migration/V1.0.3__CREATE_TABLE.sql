CREATE TABLE IF NOT EXISTS public.task (
    id BIGSERIAL NOT NULL,
    product_id bigint NOT NULL constraint product_id_fk references public.product (id) on delete cascade,
    task_id varchar(255),
    question_id varchar(255),
    star_rating varchar(255),
    job_type varchar(255),
    url varchar(255),
    completed boolean NOT NULL default false,
    updated_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP
);
ALTER TABLE public.review OWNER TO root;