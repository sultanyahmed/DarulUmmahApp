create extension if not exists pgcrypto;

create table if not exists public.announcements (
    id uuid primary key default gen_random_uuid(),
    title text not null,
    description text not null,
    media_url text,
    media_path text,
    start_date text not null,
    start_time text not null,
    start_at timestamptz not null,
    event_date text not null,
    event_time text not null,
    expires_at timestamptz not null,
    created_at timestamptz not null default timezone('utc', now())
);

alter table public.announcements enable row level security;

insert into storage.buckets (id, name, public)
values ('announcement-media', 'announcement-media', true)
on conflict (id) do update
set public = excluded.public;

drop policy if exists "announcements_select_public" on public.announcements;
create policy "announcements_select_public"
on public.announcements
for select
to anon, authenticated
using (true);

drop policy if exists "announcements_insert_public" on public.announcements;

do $$
begin
    if not exists (
        select 1
        from pg_publication_tables
        where pubname = 'supabase_realtime'
            and schemaname = 'public'
            and tablename = 'announcements'
    ) then
        alter publication supabase_realtime add table public.announcements;
    end if;
end $$;
