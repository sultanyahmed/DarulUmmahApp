alter table public.announcements
    add column if not exists start_date text,
    add column if not exists start_time text,
    add column if not exists start_at timestamptz;

update public.announcements
set
    start_date = coalesce(start_date, event_date),
    start_time = coalesce(start_time, event_time),
    start_at = coalesce(start_at, expires_at)
where
    start_date is null
    or start_time is null
    or start_at is null;

alter table public.announcements
    alter column start_date set not null,
    alter column start_time set not null,
    alter column start_at set not null;
