create table if not exists premoderation_queue (
    id bigserial primary key not null,
    text varchar(255) not null,
    images text,
    created_dt timestamp with timezone default now()
);