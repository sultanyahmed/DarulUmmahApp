package com.example.darulummahapp

import platform.Foundation.NSBundle

actual fun loadSupabaseProjectConfig(): SupabaseProjectConfig {
    val bundle = NSBundle.mainBundle
    val url = bundle.objectForInfoDictionaryKey("SUPABASE_URL") as? String ?: ""
    val anonKey = bundle.objectForInfoDictionaryKey("SUPABASE_ANON_KEY") as? String ?: ""
    return SupabaseProjectConfig(url = url, anonKey = anonKey)
}
