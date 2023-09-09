CREATE SCHEMA IF NOT EXISTS public;
CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE TABLE IF NOT EXISTS public.user (
    id varchar(255) NOT NULL primary key,
    username varchar(255),
    full_name varchar(255),
    email varchar(255),
    roles varchar(255)[],
    avatar_url varchar,
    anonymous boolean NOT NULL default false,
    is_premium_user boolean NOT NULL default false,
    updated_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(id)
);
ALTER TABLE public.user OWNER TO root;
comment on table public.user is 'Basic user information';

CREATE TABLE IF NOT EXISTS public.api_key (
    id varchar(255) NOT NULL primary key,
    user_id varchar(255) NOT NULL,
    api_key varchar(64) NOT NULL,
    provider varchar(255) NOT NULL,
    created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(api_key, user_id)
);
ALTER TABLE public.api_key OWNER TO root;
comment on table public.api_key is 'User API Keys';

CREATE TABLE IF NOT EXISTS public.env_variable (
    id varchar(255) NOT NULL primary key,
    user_id varchar(255) NOT NULL,
    env_variable_json text,
    created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id)
);
ALTER TABLE public.env_variable OWNER TO root;
comment on table public.env_variable is 'User environment variables';

CREATE TABLE IF NOT EXISTS public.code_cell (
    id varchar(255) NOT NULL primary key,
    parent_id varchar(255),
    user_id varchar(255) NOT NULL, 
    function_name varchar(255) NOT NULL default '',
    summary varchar(255) NOT NULL default '',
    description varchar(255) NOT NULL default '',
    code text,
    interfaces text,
    json_schema text,
    full_openapi_schema text,
    version varchar(32),
    deployed_version varchar(32),
    slug varchar(256),
    tags varchar NOT NULL default '',
    fork_count bigint NOT NULL default 0,
    reason_not_deployable varchar(255),
    is_deployable boolean NOT NULL default true,
    deployed boolean NOT NULL default false,
    is_active boolean NOT NULL default false,
    is_public boolean NOT NULL default false,
    updated_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(slug, user_id),
    search_doc tsvector GENERATED ALWAYS AS (to_tsvector('english', function_name || ' ' || summary || ' ' || description || ' ' || tags)) stored
);
ALTER TABLE public.code_cell OWNER TO root;
CREATE INDEX code_cell_search_idx ON public.code_cell
    USING GIN(search_doc);

CREATE OR REPLACE FUNCTION search_docs(query text)
    RETURNS TABLE(
    codeid varchar(255),
    ownerid text,
    ownerusername text,
    owneravatar text,
    ispublic boolean,
    slug text,
    name text,
    forkcount bigint,
    summary text,
    description text,
    tags text,
    createdat timestamptz,
    updatedat timestamptz,
    rank text
) AS
$$

SELECT cc.id as codeid,
       u.id AS ownerid,
       u.username as ownerusername,
       u.avatar_url as owneravatar,
       cc.is_public AS ispublic,
       cc.slug as slug,
       cc.function_name AS name,
       cc.fork_count AS forkcount,
       cc.summary as summary,
       cc.description as description,
       cc.tags as tags,
       cc.created_at AS createdat,
       cc.updated_at AS updatedat,
       ts_rank(search_doc, websearch_to_tsquery('english', query)) AS rank
FROM public.code_cell cc JOIN public.user u on cc.user_id = u.id
WHERE search_doc @@ websearch_to_tsquery('english', query) AND
      cc.is_public = true
ORDER BY rank DESC;
$$ LANGUAGE SQL;


CREATE TABLE IF NOT EXISTS public.entitlement (
    id varchar(255) NOT NULL primary key,
    user_id varchar(255) NOT NULL,

--     Max wall clock time
--     ✅ Enforced in runtime
    max_execution_time bigint NOT NULL,

--     Max cpu time in milliseconds per execution
--     ✅ Enforced in runtime
    max_cpu_time bigint NOT NULL,

--     Max memory usage in bytes per execution
--     ✅ Enforced in runtime
    max_memory_usage bigint NOT NULL,

--     Max data transfer in bytes per request (individual HTTP calls and
--     GPT calls)
--     Enforced in API and Proxy
    max_data_transfer bigint NOT NULL,

--     Max number of http calls per execution
--     Enforced in Proxy
    max_http_calls bigint NOT NULL,

--     Max invocations per minute
--     ✅ Enforced in API
    max_invocations bigint NOT NULL,

--     Max number of functions per project
--     Enforced in API
    max_functions bigint NOT NULL,

--     Max number of projects per account
--     Enforced in API
    max_projects bigint NOT NULL,

    updated_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP
);
ALTER TABLE public.entitlement OWNER TO root;

CREATE TABLE IF NOT EXISTS public.project (
    id varchar(255) NOT NULL primary key,
    user_id varchar(255) NOT NULL,
    project_name varchar(255) NOT NULL,
    description varchar(255) NOT NULL,
    updated_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(id, user_id)
);
ALTER TABLE public.project OWNER TO root;

CREATE TABLE IF NOT EXISTS public.project_item (
    id varchar(255) NOT NULL primary key,
    project_id varchar(255) NOT NULL,
    code_id varchar(255) NOT NULL,
    created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(id, code_id, project_id)
);
ALTER TABLE public.project_item OWNER TO root;

CREATE TABLE IF NOT EXISTS public.commit_history (
    id varchar(255) NOT NULL primary key,
    code_cell_id varchar(255) NOT NULL,
    user_id varchar(255) NOT NULL,
    version varchar(32),
    deployed boolean NOT NULL default false,
    code text,
    json_schema text,
    full_openapi_schema text,
    created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP
);
ALTER TABLE public.commit_history OWNER TO root;

CREATE TABLE IF NOT EXISTS public.request_history (
    id varchar(255) NOT NULL primary key,
    user_id varchar(255),
    http_method varchar(255),
    url text,
    error_message text,
    request_started_at timestamptz,
    request_ended_at timestamptz,
    request_duration bigint,
    http_status_code bigint,
    execution_id varchar(255),
    request_content_length bigint,
    response_content_length bigint,
    created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP
);
ALTER TABLE public.request_history OWNER TO root;