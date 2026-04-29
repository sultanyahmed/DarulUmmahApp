package com.example.darulummahapp

import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

data class SupabaseProjectConfig(
    val url: String,
    val anonKey: String,
) {
    val isConfigured: Boolean
        get() = url.isNotBlank() && anonKey.isNotBlank()

    val functionsUrl: String?
        get() {
            val host = url.removePrefix("https://").removePrefix("http://").substringBefore('/')
            val projectRef = host.substringBefore(".supabase.co")
            return if (projectRef.isBlank() || projectRef == host) null else "https://${projectRef}.functions.supabase.co"
        }
}

@Serializable
data class Announcement(
    val id: String,
    val title: String,
    val description: String,
    @SerialName("media_url") val mediaUrl: String? = null,
    @SerialName("start_date") val startDate: String = "",
    @SerialName("start_time") val startTime: String = "",
    @SerialName("event_date") val eventDate: String = "",
    @SerialName("event_time") val eventTime: String = "",
    @SerialName("created_at") val createdAt: String,
)

@Serializable
data class AnnouncementDraft(
    val title: String,
    val description: String,
    val startDate: String,
    val startTime: String,
    val eventDate: String,
    val eventTime: String,
    val mediaBase64: String? = null,
    val mediaMimeType: String? = null,
    val mediaFileName: String? = null,
)

data class AnnouncementFeed(
    val announcements: List<Announcement>,
    val status: String,
)

expect fun loadSupabaseProjectConfig(): SupabaseProjectConfig
expect fun platformDecodeBase64(value: ByteArray): ByteArray

@Serializable
private data class AnnouncementFunctionRequest(
    val title: String,
    val description: String,
    val startDate: String,
    val startTime: String,
    val eventDate: String,
    val eventTime: String,
    val mediaBase64: String? = null,
    val mediaMimeType: String? = null,
    val mediaFileName: String? = null,
)

@Serializable
private data class AnnouncementDeleteRequest(
    val announcementId: String,
)

@Serializable
private data class FunctionErrorResponse(
    val error: String? = null,
    val message: String? = null,
)

private const val announcementPollIntervalMillis = 30_000L

private val announcementsJson = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
}

private val announcementHttpClient = HttpClient()

class AnnouncementRepository(
    private val config: SupabaseProjectConfig = loadSupabaseProjectConfig(),
) {
    val isConfigured: Boolean
        get() = config.isConfigured

    fun observeAnnouncements(): Flow<AnnouncementFeed> {
        if (!config.isConfigured) {
            return flow {
                emit(
                    AnnouncementFeed(
                        announcements = emptyList(),
                        status = "Supabase is not configured.",
                    ),
                )
            }
        }

        return flow {
            while (true) {
                emit(loadAnnouncements(config))
                delay(announcementPollIntervalMillis)
            }
        }.catch {
            emit(
                AnnouncementFeed(
                    announcements = emptyList(),
                    status = it.message ?: "Could not load announcements from the announcements service.",
                ),
            )
        }
    }

    suspend fun fetchAnnouncements(): AnnouncementFeed {
        if (!config.isConfigured) {
            return AnnouncementFeed(
                announcements = emptyList(),
                status = "Supabase is not configured.",
            )
        }
        return loadAnnouncements(config)
    }

    suspend fun submitAnnouncement(
        draft: AnnouncementDraft,
        password: String,
    ) {
        require(config.isConfigured) { "Supabase configuration is missing." }
        val functionsUrl = requireNotNull(config.functionsUrl) {
            "Supabase URL must point to a hosted Supabase project."
        }
        val response = announcementHttpClient.post("${functionsUrl}/admin-announcements") {
            header("Content-Type", "application/json")
            header("apikey", config.anonKey)
            header("Authorization", "Bearer ${config.anonKey}")
            header("x-announcement-password", password)
            setBody(
                announcementsJson.encodeToString(
                    AnnouncementFunctionRequest(
                        title = draft.title,
                        description = draft.description,
                        startDate = draft.startDate,
                        startTime = draft.startTime,
                        eventDate = draft.eventDate,
                        eventTime = draft.eventTime,
                        mediaBase64 = draft.mediaBase64,
                        mediaMimeType = draft.mediaMimeType,
                        mediaFileName = draft.mediaFileName,
                    ),
                ),
            )
        }
        val responseBody = response.bodyAsText()

        when (response.status.value) {
            in 200..299 -> return
            404 -> error(missingBackendMessage())
            else -> error(parseFunctionError(responseBody) ?: "Could not send announcement.")
        }
    }

    suspend fun deleteAnnouncement(
        announcementId: String,
        password: String,
    ) {
        require(config.isConfigured) { "Supabase configuration is missing." }
        val functionsUrl = requireNotNull(config.functionsUrl) {
            "Supabase URL must point to a hosted Supabase project."
        }
        val response = announcementHttpClient.delete("${functionsUrl}/admin-announcements") {
            header("Content-Type", "application/json")
            header("apikey", config.anonKey)
            header("Authorization", "Bearer ${config.anonKey}")
            header("x-announcement-password", password)
            setBody(
                announcementsJson.encodeToString(
                    AnnouncementDeleteRequest(announcementId = announcementId),
                ),
            )
        }
        val responseBody = response.bodyAsText()

        when (response.status.value) {
            in 200..299 -> return
            404 -> error(missingBackendMessage())
            else -> error(parseFunctionError(responseBody) ?: "Could not delete announcement.")
        }
    }
}

private suspend fun loadAnnouncements(config: SupabaseProjectConfig): AnnouncementFeed {
    val functionsUrl = config.functionsUrl
        ?: return AnnouncementFeed(
            announcements = emptyList(),
            status = "Supabase URL must point to a hosted Supabase project.",
        )
    val response = announcementHttpClient.get("${functionsUrl}/public-announcements") {
        header("apikey", config.anonKey)
        header("Authorization", "Bearer ${config.anonKey}")
    }
    val responseBody = response.bodyAsText()

    return when (response.status.value) {
        in 200..299 -> {
            val rows = announcementsJson.decodeFromString<List<Announcement>>(responseBody)
            AnnouncementFeed(
                announcements = rows.sortedByDescending { it.createdAt },
                status = "",
            )
        }
        404 -> AnnouncementFeed(
            announcements = emptyList(),
            status = missingBackendMessage(),
        )
        else -> AnnouncementFeed(
            announcements = emptyList(),
            status = parseFunctionError(responseBody)
                ?: "Could not load announcements from the announcements service.",
        )
    }
}

private fun parseFunctionError(responseBody: String): String? {
    return runCatching {
        announcementsJson.decodeFromString<FunctionErrorResponse>(responseBody)
    }.getOrNull()?.let { error ->
        error.error ?: error.message
    }
}

private fun missingBackendMessage(): String {
    return "Announcements backend is not deployed. Apply the Supabase migration and deploy the Edge Functions."
}
