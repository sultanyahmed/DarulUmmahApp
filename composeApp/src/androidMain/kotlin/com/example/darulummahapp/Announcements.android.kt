package com.example.darulummahapp

actual fun loadSupabaseProjectConfig(): SupabaseProjectConfig {
    return SupabaseProjectConfig(
        url = BuildConfig.SUPABASE_URL,
        anonKey = BuildConfig.SUPABASE_ANON_KEY,
    )
}
