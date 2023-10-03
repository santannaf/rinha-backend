CREATE EXTENSION pg_trgm;

CREATE OR REPLACE FUNCTION ARRAY_TO_STRING_IMMUTABLE(
    arr TEXT[],
    sep TEXT
) RETURNS TEXT
    IMMUTABLE PARALLEL SAFE
    LANGUAGE SQL AS
$$
SELECT ARRAY_TO_STRING(arr, sep)
$$;

CREATE TABLE pessoas
(
    id         uuid                   not null,
    apelido    character varying(32)  not null,
    nome       character varying(100) not null,
    nascimento date                   not null,
    stack      varchar(32)[],
    busca      TEXT GENERATED ALWAYS AS (nome || ' ' || apelido || ' ' ||
                                         COALESCE(ARRAY_TO_STRING_IMMUTABLE(stack, ' '), '')) STORED,
    CONSTRAINT unique_apelido UNIQUE (apelido)
);

CREATE INDEX index_pessoas_on_id ON pessoas (id);
CREATE INDEX index_pessoas_on_apelido ON pessoas USING btree (apelido);
CREATE INDEX index_pessoas_on_search ON pessoas USING gist (busca gist_trgm_ops);
