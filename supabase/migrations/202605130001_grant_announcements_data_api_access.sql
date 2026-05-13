grant select
on table public.announcements
to anon, authenticated;

grant select, insert, update, delete
on table public.announcements
to service_role;
