## Supabase announcement setup

This app expects a public Supabase anon key at build time and an admin password in the Edge Function environment.

### Android

Add these values to `local.properties`:

```properties
supabase.url=https://qcddlnvwpmgioduniuhx.supabase.co
supabase.anonKey=your_supabase_anon_key
```

### iOS

Copy [Secrets.xcconfig.example](/Users/sultanyousufahmed/Downloads/DarulUmmahApp/iosApp/Configuration/Secrets.xcconfig.example:1) to `iosApp/Configuration/Secrets.xcconfig` and set:

```xcconfig
SUPABASE_ANON_KEY=your_supabase_anon_key
```

### Database

Apply every migration in [supabase/migrations](/Users/sultanyousufahmed/Downloads/DarulUmmahApp/supabase/migrations) in filename order.
These migrations create the `announcements` table, start/expiry scheduling columns, and the public storage bucket used for attached images.

### Edge Function

Deploy [index.ts](/Users/sultanyousufahmed/Downloads/DarulUmmahApp/supabase/functions/admin-announcements/index.ts:1) and set:

```bash
ANNOUNCEMENT_ADMIN_PASSWORD=your_admin_password
SUPABASE_URL=https://qcddlnvwpmgioduniuhx.supabase.co
SUPABASE_SERVICE_ROLE_KEY=your_service_role_key
```

`ANNOUNCEMENT_ADMIN_PASSWORD` is required. The admin Edge Function intentionally has no fallback password.

Once the migration and function are live, the app can publish announcements with an on-device image upload and every device will receive them in real time.
Expired announcements are filtered out on reads and are cleaned up together with their uploaded images the next time the admin announcements endpoint is hit.
