create extension if not exists pg_cron;
create extension if not exists pgcrypto;

create table if not exists public.announcements (
    id uuid primary key default gen_random_uuid(),
    title text not null,
    description text not null,
    media_url text,
    media_path text,
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

alter publication supabase_realtime add table public.announcements;
