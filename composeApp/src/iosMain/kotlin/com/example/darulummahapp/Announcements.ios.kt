package com.example.darulummahapp

import platform.Foundation.NSBundle

private const val defaultSupabaseUrl = "https://qcddlnvwpmgioduniuhx.supabase.co"
private const val defaultSupabaseAnonKey = "sb_publishable_wM5wDGvFMP7lIso5c934_g_YyMTvaKc"

actual fun loadSupabaseProjectConfig(): SupabaseProjectConfig {
    val bundle = NSBundle.mainBundle
    val url = bundle.supabaseInfoValue("SUPABASE_URL", defaultSupabaseUrl)
    val anonKey = bundle.supabaseInfoValue("SUPABASE_ANON_KEY", defaultSupabaseAnonKey)
    return SupabaseProjectConfig(url = url, anonKey = anonKey)
}

private fun NSBundle.supabaseInfoValue(key: String, defaultValue: String): String {
    val value = (objectForInfoDictionaryKey(key) as? String).orEmpty().trim()
    return if (value.isBlank() || value.startsWith("$(")) defaultValue else value
}
