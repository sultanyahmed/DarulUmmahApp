package com.example.darulummahapp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import darulummahapp.composeapp.generated.resources.Res
import darulummahapp.composeapp.generated.resources.darul_ummah_logo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

private val SiteBlack = Color(0xFF1F1E1E)
private val SiteCharcoal = Color(0xFF2F2E2E)
private val Green900 = Color(0xFF002A29)
private val Green700 = Color(0xFF047167)
private val Green500 = Color(0xFF009688)
private val Green100 = Color(0xFFE6F2EC)
private val Gold = Color(0xFFFFEB3B)
private val Ink = Color(0xFF1E2A24)
private val Muted = Color(0xFF65746D)
private val Page = SiteBlack
private val Alert = Color(0xFFE2554A)
private val QiblaSurface = Color(0xFF103B38)
private val QiblaSurfaceDeep = Color(0xFF0A2524)
private val QiblaCream = Color(0xFFF7F1DD)
private val QiblaSand = Color(0xFFE6D6AC)
private val QiblaBronze = Color(0xFFB69642)
private val announcementTimeOptions = buildList<String> {
    for (hour in 0..23) {
        for (minute in listOf(0, 15, 30, 45)) {
            add(timeOptionLabel(hour, minute))
        }
    }
}

private fun timeOptionLabel(hour: Int, minute: Int): String {
    val hourText = if (hour < 10) "0$hour" else hour.toString()
    val minuteText = if (minute < 10) "0$minute" else minute.toString()
    return "$hourText:$minuteText"
}

data class PrayerTime(
    val name: String,
    val beginsTime: String,
    val jamaahTime: String,
    val minuteOfDay: Int,
)

data class JumahTime(
    val khutbahTime: String,
    val salaatTime: String,
)

data class PrayerTimetable(
    val dailyPrayerTimes: List<PrayerTime>,
    val jumahTime: JumahTime,
)

data class NotificationPreferences(
    val prayerReminders: Boolean,
    val azanAtSalahStart: Boolean,
    val countdownAlerts: Boolean,
    val classesAndEvents: Boolean,
)

internal data class UpcomingPrayer(
    val name: String,
    val jamaahTime: String,
    val minuteOfDay: Int,
)

private data class MosqueContact(
    val phone: String,
    val email: String,
    val address: String,
)

private data class YouTubeVideo(
    val id: String,
    val title: String,
    val publishedDate: String,
)

data class ClassSession(
    val title: String,
    val day: String,
    val time: String,
    val audience: String,
    val reminderIsoDayOfWeek: Int? = null,
    val reminderMinuteOfDay: Int? = null,
)

data class CalendarPrayerTime(
    val date: String,
    val fajrBegins: String,
    val fajrJamaah: String,
    val sunrise: String,
    val dhuhrBegins: String,
    val dhuhrJamaah: String,
    val asrBegins: String,
    val asrJamaah: String,
    val maghribBegins: String,
    val maghribJamaah: String,
    val ishaBegins: String,
    val ishaJamaah: String,
)

private data class CalendarDate(
    val monthIndex: Int,
    val monthName: String,
    val dayOfMonth: Int,
    val year: Int?,
)

private data class CalendarDay(
    val date: CalendarDate,
    val prayerTime: CalendarPrayerTime,
)

private enum class AppScreen {
    Home,
    Classes,
    YouTube,
    Settings,
    FullCalendar,
}

internal const val DarulUmmahYouTubeChannelId = "UCy7hFfaw0R-z8Mpg4zwMJrA"
private const val DarulUmmahYouTubeChannelUrl = "https://www.youtube.com/@DarulUmmahMosque"

private val fallbackPrayerTimetable = PrayerTimetable(
    dailyPrayerTimes = listOf(
        PrayerTime("Fajr", "04:35", "05:15", 5 * 60 + 15),
        PrayerTime("Zuhr", "13:05", "13:30", 13 * 60 + 30),
        PrayerTime("Asr", "17:49", "18:15", 18 * 60 + 15),
        PrayerTime("Maghrib", "20:03", "20:08", 20 * 60 + 8),
        PrayerTime("Isha", "21:19", "21:40", 21 * 60 + 40),
    ),
    jumahTime = JumahTime("13:30", "13:50"),
)

private val mosqueContact = MosqueContact(
    phone = "0207 790 5166",
    email = "info@darulummah.org.uk",
    address = "56 Bigland St, Shadwell, London E1 2ND",
)

internal val defaultNotificationPreferences = NotificationPreferences(
    prayerReminders = true,
    azanAtSalahStart = false,
    countdownAlerts = false,
    classesAndEvents = true,
)

internal val classSchedule = listOf(
    ClassSession(
        title = "Dars of Sahih Al-Bukhari",
        day = "Tuesday Evenings",
        time = "19:00",
        audience = "Community",
        reminderIsoDayOfWeek = 2,
        reminderMinuteOfDay = 19 * 60,
    ),
    ClassSession(
        title = "Women's Tafsir (Bangla)",
        day = "Wednesday",
        time = "11:00 - 12:30",
        audience = "Sisters",
        reminderIsoDayOfWeek = 3,
        reminderMinuteOfDay = 11 * 60,
    ),
    ClassSession(
        title = "Islamic Studies",
        day = "Wednesday Evenings",
        time = "19:00",
        audience = "Community",
        reminderIsoDayOfWeek = 3,
        reminderMinuteOfDay = 19 * 60,
    ),
    ClassSession(
        title = "Women's Tarteel Class",
        day = "Thursday Evenings",
        time = "19:00",
        audience = "Sisters",
        reminderIsoDayOfWeek = 4,
        reminderMinuteOfDay = 19 * 60,
    ),
    ClassSession(
        title = "Hadeeth Class",
        day = "Thursday Evenings",
        time = "19:00",
        audience = "Community",
        reminderIsoDayOfWeek = 4,
        reminderMinuteOfDay = 19 * 60,
    ),
    ClassSession(
        title = "Tarteel Class",
        day = "Friday Evenings",
        time = "19:00",
        audience = "Community",
        reminderIsoDayOfWeek = 5,
        reminderMinuteOfDay = 19 * 60,
    ),
    ClassSession(
        title = "Bangla Tafseer",
        day = "Sunday Evenings",
        time = "19:00",
        audience = "Community",
        reminderIsoDayOfWeek = 7,
        reminderMinuteOfDay = 19 * 60,
    ),
)

@Composable
@Preview
fun App() {
    MaterialTheme {
        val announcementRepository = remember { AnnouncementRepository() }
        var screen by rememberSaveable { mutableStateOf(AppScreen.Home) }
        var minuteOfDay by remember { mutableIntStateOf(currentMinuteOfDay()) }
        var secondOfDay by remember { mutableIntStateOf(currentSecondOfDay()) }
        var isoDayOfWeek by remember { mutableIntStateOf(currentIsoDayOfWeek()) }
        var prayerTimetable by remember { mutableStateOf(fallbackPrayerTimetable) }
        var notificationPreferences by remember { mutableStateOf(loadNotificationPreferences()) }
        var updateStatus by remember { mutableStateOf("Loading today's times from darulummah.org.uk") }
        var refreshKey by remember { mutableIntStateOf(0) }
        var calendarTimetable by remember { mutableStateOf<List<CalendarPrayerTime>>(emptyList()) }
        var calendarStatus by remember { mutableStateOf("Open the full calendar timetable to load yearly prayer times.") }
        var calendarRefreshKey by remember { mutableIntStateOf(0) }
        var announcements by remember { mutableStateOf<List<Announcement>>(emptyList()) }
        var announcementStatus by remember { mutableStateOf("Connecting live announcements...") }
        var announcementSubmitStatus by remember { mutableStateOf<String?>(null) }
        var announcementDeleteStatus by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(Unit) {
            while (true) {
                val now = currentDateTimeComponents()
                minuteOfDay = now.hour * 60 + now.minute
                secondOfDay = now.hour * 60 * 60 + now.minute * 60 + now.second
                isoDayOfWeek = currentIsoDayOfWeek()
                delay(1_000)
            }
        }

        LaunchedEffect(refreshKey) {
            updateStatus = "Loading today's times from darulummah.org.uk"
            runCatching {
                parseDarulUmmahTimetable(fetchDarulUmmahPrayerTimetableHtml())
            }.onSuccess { timetable ->
                prayerTimetable = timetable
                updateStatus = "Updated from darulummah.org.uk"
            }.onFailure {
                updateStatus = "Could not refresh. Showing the last bundled timetable."
            }
        }

        LaunchedEffect(calendarRefreshKey) {
            if (calendarRefreshKey == 0) return@LaunchedEffect
            calendarStatus = "Loading full calendar timetable from darulummah.org.uk"
            runCatching {
                val html = fetchDarulUmmahPrayerTimetableHtml()
                publishedCalendarYear(html) to parseFullCalendarTimetable(html)
            }.onSuccess { (publishedYear, timetable) ->
                calendarTimetable = timetable
                calendarStatus = if (timetable.isEmpty()) {
                    "No timetable rows were found on the Darul Ummah timetable page."
                } else {
                    "Updated from darulummah.org.uk for $publishedYear"
                }
            }.onFailure {
                calendarStatus = "Could not load the full calendar timetable."
            }
        }

        LaunchedEffect(notificationPreferences, prayerTimetable, isoDayOfWeek, announcements) {
            saveNotificationPreferences(notificationPreferences)
            updateNotificationSchedules(
                preferences = notificationPreferences,
                timetable = prayerTimetable,
                isoDayOfWeek = isoDayOfWeek,
                announcements = announcements,
            )
        }

        LaunchedEffect(announcementRepository) {
            announcementRepository.observeAnnouncements().collect { feed ->
                announcements = feed.announcements
                announcementStatus = feed.status
            }
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Page,
        ) {
            Column(
                modifier = Modifier
                    .safeContentPadding()
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(SiteBlack, Green900, Green700),
                        ),
                    )
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Header(onSettingsClick = { screen = AppScreen.Settings })
                NavigationTabs(
                    selected = screen,
                    onSelected = { screen = it },
                )

                when (screen) {
                    AppScreen.Home -> HomeScreen(
                        minuteOfDay = minuteOfDay,
                        secondOfDay = secondOfDay,
                        isoDayOfWeek = isoDayOfWeek,
                        prayerTimetable = prayerTimetable,
                        updateStatus = updateStatus,
                        onRefresh = { refreshKey++ },
                    )
                    AppScreen.Classes -> ClassesAndEventsScreen(
                        announcements = announcements,
                        announcementStatus = announcementStatus,
                        submitStatus = announcementSubmitStatus,
                        onSubmitAnnouncement = { draft, password ->
                            announcementSubmitStatus = "Sending announcement..."
                            try {
                                announcementRepository.submitAnnouncement(draft, password)
                                val refreshedFeed = announcementRepository.fetchAnnouncements()
                                announcements = refreshedFeed.announcements
                                announcementStatus = refreshedFeed.status
                                announcementSubmitStatus = "Announcement sent."
                            } catch (error: Throwable) {
                                announcementSubmitStatus = error.message ?: "Could not send announcement."
                            }
                        },
                        deleteStatus = announcementDeleteStatus,
                        onDeleteAnnouncement = { announcement, password ->
                            announcementDeleteStatus = "Deleting announcement..."
                            try {
                                announcementRepository.deleteAnnouncement(announcement.id, password)
                                val refreshedFeed = announcementRepository.fetchAnnouncements()
                                announcements = refreshedFeed.announcements
                                announcementStatus = refreshedFeed.status
                                announcementDeleteStatus = "Announcement deleted."
                            } catch (error: Throwable) {
                                announcementDeleteStatus = error.message ?: "Could not delete announcement."
                            }
                        },
                    )
                    AppScreen.YouTube -> YouTubeScreen()
                    AppScreen.Settings -> SettingsScreen(
                        notificationPreferences = notificationPreferences,
                        onNotificationPreferencesChanged = { notificationPreferences = it },
                        onFullCalendarClick = {
                            screen = AppScreen.FullCalendar
                            calendarRefreshKey++
                        },
                    )
                    AppScreen.FullCalendar -> FullCalendarTimetableScreen(
                        timetable = calendarTimetable,
                        status = calendarStatus,
                        onRefresh = { calendarRefreshKey++ },
                        onBack = { screen = AppScreen.Settings },
                    )
                }
            }
        }
    }
}

@Composable
private fun Header(
    onSettingsClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(SiteCharcoal)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(Res.drawable.darul_ummah_logo),
            contentDescription = "Darul Ummah logo",
            modifier = Modifier
                .size(58.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .padding(6.dp),
        )
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Darul Ummah Shadwell",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = mosqueContact.address,
                color = Gold,
                fontSize = 13.sp,
            )
        }
        TextButton(onClick = onSettingsClick) {
            Text(
                text = "⚙",
                color = Gold,
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun NavigationTabs(
    selected: AppScreen,
    onSelected: (AppScreen) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(SiteCharcoal)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        TabButton(
            text = "Prayer Times",
            selected = selected == AppScreen.Home,
            onClick = { onSelected(AppScreen.Home) },
            modifier = Modifier.weight(1f),
        )
        TabButton(
            text = "Classes & Events",
            selected = selected == AppScreen.Classes,
            onClick = { onSelected(AppScreen.Classes) },
            modifier = Modifier.weight(1f),
        )
        TabButton(
            text = "YouTube",
            selected = selected == AppScreen.YouTube,
            onClick = { onSelected(AppScreen.YouTube) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun TabButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val container = if (selected) Green900 else Color.Transparent
    val content = if (selected) Gold else Color.White
    TextButton(
        onClick = onClick,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(container),
    ) {
        Text(
            text = text,
            color = content,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun HomeScreen(
    minuteOfDay: Int,
    secondOfDay: Int,
    isoDayOfWeek: Int,
    prayerTimetable: PrayerTimetable,
    updateStatus: String,
    onRefresh: () -> Unit,
) {
    val currentPrayer = currentPrayer(prayerTimetable.dailyPrayerTimes, minuteOfDay)
    val upcomingPrayer = upcomingPrayer(
        timetable = prayerTimetable,
        minuteOfDay = minuteOfDay,
        isoDayOfWeek = isoDayOfWeek,
    )
    val remainingSeconds = secondsUntil(upcomingPrayer.minuteOfDay * 60, secondOfDay)

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        CountdownCard(
            currentPrayer = currentPrayer,
            upcomingPrayer = upcomingPrayer,
            remainingSeconds = remainingSeconds,
        )
        JumahCard(prayerTimetable.jumahTime)
        PrayerTimesList(
            currentPrayer = currentPrayer,
            prayerTimes = prayerTimetable.dailyPrayerTimes,
        )
        ContactCard(mosqueContact)
        RemoteUpdateCard(
            status = updateStatus,
            onRefresh = onRefresh,
        )
    }
}

@Composable
private fun CountdownCard(
    currentPrayer: PrayerTime,
    upcomingPrayer: UpcomingPrayer,
    remainingSeconds: Int,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Green900),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Green900, Green700, Green500),
                    ),
                )
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Upcoming prayer",
                color = Gold,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = upcomingPrayer.name,
                color = Color.White,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Jama'ah at ${upcomingPrayer.jamaahTime}",
                color = Color.White,
                fontSize = 16.sp,
            )
            Text(
                text = formatCountdown(remainingSeconds),
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Current: ${currentPrayer.name}",
                color = Green100,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun JumahCard(jumahTime: JumahTime) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Alert),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "Jum'ah",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Khutbah starts at ${jumahTime.khutbahTime}",
                    color = Color.White,
                    fontSize = 14.sp,
                )
            }
            Text(
                text = jumahTime.salaatTime,
                color = Gold,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun PrayerTimesList(
    currentPrayer: PrayerTime,
    prayerTimes: List<PrayerTime>,
) {
    InfoCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Today's mosque prayer times",
                color = Ink,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
            prayerTimes.forEach { prayer ->
                PrayerRow(
                    prayer = prayer,
                    isCurrent = prayer.name == currentPrayer.name,
                )
            }
        }
    }
}

@Composable
private fun PrayerRow(
    prayer: PrayerTime,
    isCurrent: Boolean,
) {
    val background = if (isCurrent) Green100 else Color.Transparent
    val border = if (isCurrent) BorderStroke(1.dp, Green700) else BorderStroke(1.dp, Color(0xFFE2E8E4))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .border(border, RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = prayer.name,
                color = Ink,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Begins ${prayer.beginsTime}",
                color = Muted,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
            if (isCurrent) {
                Text(
                    text = "Now",
                    color = Green700,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        Spacer(Modifier.width(10.dp))
        Column(
            modifier = Modifier.weight(1.2f),
            horizontalAlignment = Alignment.End,
        ) {
            Text(
                text = "Jama'ah starts at:",
                color = Muted,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.End,
            )
            Text(
                text = prayer.jamaahTime,
                color = if (isCurrent) Green900 else Ink,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
            )
        }
    }
}

@Composable
private fun YouTubeScreen() {
    var recentVideos by remember { mutableStateOf<List<YouTubeVideo>>(emptyList()) }
    var recentVideosStatus by remember { mutableStateOf("Loading recent videos...") }
    var selectedVideoId by remember { mutableStateOf<String?>(null) }
    var refreshKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(refreshKey) {
        recentVideosStatus = "Loading recent videos..."
        runCatching {
            parseYouTubeRecentVideos(fetchDarulUmmahYouTubeFeedXml()).take(4)
        }.onSuccess { videos ->
            recentVideos = videos
            selectedVideoId = videos.firstOrNull()?.id
            recentVideosStatus = if (videos.isEmpty()) {
                "No recent videos were found."
            } else {
                "Latest videos from Darul Ummah TV"
            }
        }.onFailure {
            recentVideosStatus = "Could not load recent videos."
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        InfoCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle("YouTube")
                Text(
                    text = "Live stream from Darul Ummah TV",
                    color = Muted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "The live player shows the channel stream when the mosque is live.",
                    color = Muted,
                    fontSize = 12.sp,
                )
                YouTubeLivePlayer(
                    channelId = DarulUmmahYouTubeChannelId,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(8.dp)),
                )
                Button(
                    onClick = { openExternalUrl(DarulUmmahYouTubeChannelUrl) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Open YouTube channel")
                }
            }
        }
        InfoCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle("Previous videos")
                Text(
                    text = recentVideosStatus,
                    color = if (recentVideos.isEmpty()) Muted else Green700,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                selectedVideoId?.let { videoId ->
                    YouTubeVideoPlayer(
                        videoId = videoId,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(230.dp)
                            .clip(RoundedCornerShape(8.dp)),
                    )
                }
                recentVideos.forEach { video ->
                    YouTubeVideoRow(
                        video = video,
                        selected = video.id == selectedVideoId,
                        onClick = { selectedVideoId = video.id },
                    )
                }
                Button(
                    onClick = { refreshKey++ },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Refresh videos")
                }
            }
        }
    }
}

@Composable
private fun YouTubeVideoRow(
    video: YouTubeVideo,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val background = if (selected) Green100 else Color.Transparent
    val border = if (selected) BorderStroke(1.dp, Green700) else BorderStroke(1.dp, Color(0xFFE2E8E4))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .border(border, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = video.title,
                color = Ink,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
            if (video.publishedDate.isNotBlank()) {
                Text(
                    text = video.publishedDate,
                    color = Muted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        Text(
            text = if (selected) "Playing" else "Play",
            color = if (selected) Green700 else Green900,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun NotificationSettings(
    preferences: NotificationPreferences,
    onPreferencesChanged: (NotificationPreferences) -> Unit,
) {
    InfoCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionTitle("Notifications")
            NotificationRow(
                title = "Prayer reminders",
                detail = "30 and 10 minutes before Jama'ah",
                enabled = preferences.prayerReminders,
                onEnabledChanged = {
                    onPreferencesChanged(preferences.copy(prayerReminders = it))
                },
            )
            NotificationRow(
                title = "Azan at salah start",
                detail = "Play azan when each prayer begins",
                enabled = preferences.azanAtSalahStart,
                onEnabledChanged = {
                    onPreferencesChanged(preferences.copy(azanAtSalahStart = it))
                },
            )
            NotificationRow(
                title = "Classes and events",
                detail = "One alert 1 hour before each class or announcement",
                enabled = preferences.classesAndEvents,
                onEnabledChanged = {
                    onPreferencesChanged(preferences.copy(classesAndEvents = it))
                },
            )
        }
    }
}

@Composable
private fun NotificationRow(
    title: String,
    detail: String,
    enabled: Boolean,
    onEnabledChanged: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Ink, fontWeight = FontWeight.SemiBold)
            Text(detail, color = Muted, fontSize = 13.sp)
        }
        Switch(
            checked = enabled,
            onCheckedChange = onEnabledChanged,
        )
    }
}

@Composable
private fun ContactCard(contact: MosqueContact) {
    InfoCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionTitle("Contact Darul Ummah Shadwell")
            ContactRow(
                label = "Call",
                value = contact.phone,
                actionLabel = "Tap to call",
                onClick = { openPhoneNumber(contact.phone) },
            )
            ContactRow(
                label = "Email",
                value = contact.email,
                actionLabel = "Tap to email",
                onClick = { openEmailAddress(contact.email) },
            )
            ContactRow(
                label = "Visit",
                value = contact.address,
                actionLabel = "Tap for directions",
                onClick = { openMapDirections(contact.address) },
            )
        }
    }
}

@Composable
private fun ContactRow(
    label: String,
    value: String,
    actionLabel: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .background(Color(0xFFF7FAF8))
            .border(1.dp, Color(0xFFE1E8E3), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column {
            Text(
                text = label,
                color = Green900,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = actionLabel,
                color = Green700,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Text(
            text = value,
            modifier = Modifier.weight(1f).padding(start = 14.dp),
            color = Ink,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun RemoteUpdateCard(
    status: String,
    onRefresh: () -> Unit,
) {
    InfoCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                SectionTitle("Live updates")
                Text(
                    text = status,
                    color = Muted,
                    fontSize = 13.sp,
                )
            }
            Button(onClick = onRefresh) {
                Text("Sync")
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    notificationPreferences: NotificationPreferences,
    onNotificationPreferencesChanged: (NotificationPreferences) -> Unit,
    onFullCalendarClick: () -> Unit,
) {
    val qiblaCompassController = remember { createQiblaCompassController() }
    val qiblaState by qiblaCompassController.state.collectAsState()

    DisposableEffect(qiblaCompassController) {
        qiblaCompassController.start()
        onDispose {
            qiblaCompassController.stop()
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        NotificationSettings(
            preferences = notificationPreferences,
            onPreferencesChanged = onNotificationPreferencesChanged,
        )
        QiblaCompassCard(state = qiblaState)
        InfoCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle("Prayer calendar")
                Text(
                    text = "Load the Darul Ummah timetable year currently published on the website. When the site switches to a new year, the app follows it.",
                    color = Muted,
                    fontSize = 14.sp,
                )
                Button(onClick = onFullCalendarClick) {
                    Text("Full calendar timetable")
                }
            }
        }
    }
}

@Composable
private fun QiblaCompassCard(state: QiblaCompassState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, Color(0xFF315956)),
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(QiblaSurface, QiblaSurfaceDeep),
                    ),
                )
                .padding(18.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Qibla Compass",
                    color = QiblaCream,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                )
                Text(
                    text = "Use your device heading and current location to align with the direction of prayer.",
                    color = QiblaSand.copy(alpha = 0.86f),
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Green900.copy(alpha = 0.95f),
                                    lerp(Green700, QiblaBronze, 0.35f),
                                    Green900.copy(alpha = 0.98f),
                                ),
                            ),
                        )
                        .border(1.dp, QiblaSand.copy(alpha = 0.35f), RoundedCornerShape(22.dp))
                        .padding(horizontal = 14.dp, vertical = 18.dp),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        QiblaCompassDial(
                            headingDegrees = state.headingDegrees,
                            turnDegrees = state.turnDegrees,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(290.dp),
                        )
                        Text(
                            text = state.headingDegrees?.let { "Facing ${it.toInt()}°" } ?: "Facing --",
                            color = QiblaCream,
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Light,
                        )
                        Text(
                            text = "Qibla ${state.qiblaBearingDegrees?.let { "${it.toInt()}°" } ?: "--"}",
                            color = QiblaSand.copy(alpha = 0.98f),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = state.turnDegrees?.let { turnLabel(it) } ?: "Compass permission needed",
                            color = QiblaSand.copy(alpha = 0.92f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                Text(
                    text = state.status,
                    color = QiblaSand.copy(alpha = 0.82f),
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    CompassFact(
                        label = "Facing",
                        value = state.headingDegrees?.let { "${it.toInt()}°" } ?: "--",
                        modifier = Modifier.weight(1f),
                    )
                    CompassFact(
                        label = "Qibla",
                        value = state.qiblaBearingDegrees?.let { "${it.toInt()}°" } ?: "--",
                        modifier = Modifier.weight(1f),
                    )
                    CompassFact(
                        label = "Turn",
                        value = state.turnDegrees?.let { turnLabel(it) } ?: "--",
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun QiblaCompassDial(
    headingDegrees: Double?,
    turnDegrees: Double?,
    modifier: Modifier = Modifier,
) {
    val compassRotation = -(headingDegrees ?: 0.0).toFloat()
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(250.dp)
                .shadow(18.dp, CircleShape, ambientColor = Color.Black.copy(alpha = 0.3f))
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(QiblaCream, Color(0xFFE9DEC2), Color(0xFFB9A16A)),
                    ),
                ),
        )
        Canvas(modifier = Modifier.size(236.dp)) {
            val radius = size.minDimension / 2f * 0.97f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(QiblaCream, Color(0xFFF1E7CB), Color(0xFFD4BF8B)),
                ),
                radius = radius,
            )
            drawCircle(
                color = QiblaSand.copy(alpha = 0.9f),
                radius = radius,
                style = Stroke(width = 7.dp.toPx()),
            )
            drawCircle(
                color = QiblaBronze.copy(alpha = 0.18f),
                radius = radius * 0.64f,
            )
            drawCircle(
                color = Color(0xFFE0D3AF),
                radius = radius * 0.64f,
                style = Stroke(width = 1.dp.toPx()),
            )
            rotate(degrees = compassRotation) {
                repeat(72) { index ->
                    val angle = index * 5f
                    val tickLength = when {
                        index % 18 == 0 -> 18.dp.toPx()
                        index % 9 == 0 -> 12.dp.toPx()
                        else -> 5.dp.toPx()
                    }
                    rotate(degrees = angle) {
                        drawLine(
                            color = if (index % 9 == 0) QiblaBronze else QiblaBronze.copy(alpha = 0.5f),
                            start = center.copy(y = center.y - radius + 14.dp.toPx()),
                            end = center.copy(y = center.y - radius + 14.dp.toPx() + tickLength),
                            strokeWidth = if (index % 18 == 0) 3.dp.toPx() else 1.5.dp.toPx(),
                            cap = StrokeCap.Round,
                        )
                    }
                }
                repeat(4) { index ->
                    val angle = index * 90f
                    rotate(degrees = angle) {
                        drawLine(
                            color = QiblaBronze.copy(alpha = 0.6f),
                            start = center.copy(y = center.y - radius + 28.dp.toPx()),
                            end = center.copy(y = center.y - radius + 52.dp.toPx()),
                            strokeWidth = 5.dp.toPx(),
                            cap = StrokeCap.Round,
                        )
                    }
                }
            }
            if (turnDegrees != null) {
                rotate(degrees = turnDegrees.toFloat()) {
                    drawLine(
                        color = Color.Black.copy(alpha = 0.25f),
                        start = center.copy(x = center.x + 3.dp.toPx(), y = center.y + 6.dp.toPx()),
                        end = center.copy(x = center.x + 3.dp.toPx(), y = center.y - radius + 34.dp.toPx()),
                        strokeWidth = 8.dp.toPx(),
                        cap = StrokeCap.Round,
                    )
                    drawLine(
                        color = Gold,
                        start = center.copy(y = center.y + 4.dp.toPx()),
                        end = center.copy(y = center.y - radius + 30.dp.toPx()),
                        strokeWidth = 7.dp.toPx(),
                        cap = StrokeCap.Round,
                    )
                    drawCircle(
                        color = Gold,
                        radius = 8.dp.toPx(),
                        center = center.copy(y = center.y - radius + 30.dp.toPx()),
                    )
                    drawKaabaMarker(
                        centerX = center.x,
                        centerY = center.y - radius + 4.dp.toPx(),
                        width = 26.dp.toPx(),
                        height = 24.dp.toPx(),
                    )
                }
            }
            drawLine(
                color = Color(0xFF8C1214),
                start = center.copy(y = center.y + 10.dp.toPx()),
                end = center.copy(y = center.y - radius + 34.dp.toPx()),
                strokeWidth = 7.dp.toPx(),
                cap = StrokeCap.Round,
            )
            drawLine(
                color = Color.White.copy(alpha = 0.9f),
                start = center.copy(y = center.y - 10.dp.toPx()),
                end = center.copy(y = center.y + radius - 30.dp.toPx()),
                strokeWidth = 6.dp.toPx(),
                cap = StrokeCap.Round,
            )
            drawCircle(
                color = QiblaSurfaceDeep,
                radius = 8.dp.toPx(),
            )
            drawCircle(
                color = QiblaCream,
                radius = 3.dp.toPx(),
            )
        }
        Column(
            modifier = Modifier
                .size(236.dp)
                .graphicsLayer { rotationZ = compassRotation },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("N", color = QiblaBronze, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("W", color = QiblaBronze, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text("E", color = QiblaBronze, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.weight(1f))
            Text("S", color = QiblaBronze, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawKaabaMarker(
    centerX: Float,
    centerY: Float,
    width: Float,
    height: Float,
) {
    val left = centerX - width / 2f
    val top = centerY - height / 2f
    val right = centerX + width / 2f
    val bottom = centerY + height / 2f
    val sideInset = width * 0.16f
    val face = Path().apply {
        moveTo(left + sideInset, top)
        lineTo(right, top + height * 0.1f)
        lineTo(right, bottom)
        lineTo(left + sideInset, bottom - height * 0.1f)
        close()
    }
    val side = Path().apply {
        moveTo(left, top + height * 0.12f)
        lineTo(left + sideInset, top)
        lineTo(left + sideInset, bottom - height * 0.1f)
        lineTo(left, bottom)
        close()
    }
    val bandTop = top + height * 0.22f
    val bandBottom = top + height * 0.35f
    val faceBand = Path().apply {
        moveTo(left + sideInset, bandTop)
        lineTo(right, bandTop + height * 0.04f)
        lineTo(right, bandBottom + height * 0.04f)
        lineTo(left + sideInset, bandBottom)
        close()
    }
    val sideBand = Path().apply {
        moveTo(left, bandTop + height * 0.04f)
        lineTo(left + sideInset, bandTop)
        lineTo(left + sideInset, bandBottom)
        lineTo(left, bandBottom + height * 0.04f)
        close()
    }
    drawPath(path = side, color = Color(0xFF28211A))
    drawPath(path = face, color = Color(0xFF3A3126))
    drawPath(path = sideBand, color = Gold.copy(alpha = 0.9f))
    drawPath(path = faceBand, color = Gold)
}

@Composable
private fun CompassFact(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(18.dp))
            .padding(horizontal = 10.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            color = QiblaSand.copy(alpha = 0.82f),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = value,
            color = QiblaCream,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
    }
}

private fun turnLabel(turnDegrees: Double): String {
    val normalized = normalizeDegrees(turnDegrees)
    if (kotlin.math.abs(normalizeSignedDegrees(normalized)) <= QIBLA_ALIGNMENT_THRESHOLD_DEGREES) {
        return "Aligned"
    }
    val displayDegrees = if (normalized > 180.0) 360.0 - normalized else normalized
    val rounded = (displayDegrees + 0.5).toInt()
    return if (normalized > 180.0) "$rounded° left" else "$rounded° right"
}

@Composable
private fun FullCalendarTimetableScreen(
    timetable: List<CalendarPrayerTime>,
    status: String,
    onRefresh: () -> Unit,
    onBack: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        InfoCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SectionTitle("Full calendar timetable")
                    TextButton(onClick = onBack) {
                        Text("Back")
                    }
                }
                Text(
                    text = status,
                    color = Muted,
                    fontSize = 13.sp,
                )
                Button(onClick = onRefresh) {
                    Text("Refresh")
                }
            }
        }
        if (timetable.isNotEmpty()) {
            val today = currentDateTimeComponents()
            val allCalendarDays = timetable
                .mapNotNull { prayerTime ->
                    parseCalendarDate(prayerTime.date)?.let { date ->
                        CalendarDay(date = date, prayerTime = prayerTime)
                    }
                }
                .sortedWith(
                    compareBy<CalendarDay> { it.date.year ?: Int.MAX_VALUE }
                        .thenBy { it.date.monthIndex }
                        .thenBy { it.date.dayOfMonth },
                )
            val monthIndexes = allCalendarDays.map { it.date.monthIndex }.distinct()
            val selectedToday = allCalendarDays.firstOrNull { day ->
                day.date.year == today.year &&
                    day.date.monthIndex == today.month &&
                    day.date.dayOfMonth == today.day
            }
            var visibleMonthIndex by remember(timetable) {
                mutableIntStateOf(selectedToday?.date?.monthIndex ?: monthIndexes.firstOrNull() ?: 1)
            }
            var selectedDay by remember(timetable) {
                mutableStateOf(selectedToday ?: allCalendarDays.firstOrNull())
            }
            val visibleMonthDays = allCalendarDays.filter { it.date.monthIndex == visibleMonthIndex }

            if (allCalendarDays.isEmpty()) {
                InfoCard {
                    Text(
                        text = "The timetable loaded, but the dates could not be arranged into a calendar.",
                        color = Muted,
                        fontSize = 14.sp,
                    )
                }
            } else {
                CalendarMonthSection(
                    days = visibleMonthDays,
                    selectedDay = selectedDay,
                    onDaySelected = { selectedDay = it },
                    onPreviousMonth = {
                        val currentIndex = monthIndexes.indexOf(visibleMonthIndex)
                        val previousIndex = if (currentIndex <= 0) monthIndexes.lastIndex else currentIndex - 1
                        visibleMonthIndex = monthIndexes[previousIndex]
                        selectedDay = allCalendarDays.first { it.date.monthIndex == visibleMonthIndex }
                    },
                    onNextMonth = {
                        val currentIndex = monthIndexes.indexOf(visibleMonthIndex)
                        val nextIndex = if (currentIndex == monthIndexes.lastIndex) 0 else currentIndex + 1
                        visibleMonthIndex = monthIndexes[nextIndex]
                        selectedDay = allCalendarDays.first { it.date.monthIndex == visibleMonthIndex }
                    },
                )
                selectedDay?.let { CalendarSelectedDayTimes(it) }
            }
        }
    }
}

@Composable
private fun CalendarMonthSection(
    days: List<CalendarDay>,
    selectedDay: CalendarDay?,
    onDaySelected: (CalendarDay) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
) {
    val firstDay = days.first().date
    val leadingBlankDays = firstDay.year?.let {
        sundayFirstDayOfWeek(
            year = it,
            month = firstDay.monthIndex,
            day = firstDay.dayOfMonth,
        )
    } ?: 0
    val cells = buildList<CalendarDay?> {
        repeat(leadingBlankDays) { add(null) }
        addAll(days)
    }.chunked(7)

    InfoCard {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onPreviousMonth) {
                    Text("‹", color = Green700, fontSize = 28.sp)
                }
                Text(
                    text = firstDay.year?.let { "${firstDay.monthName} $it" } ?: firstDay.monthName,
                    color = Ink,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                TextButton(onClick = onNextMonth) {
                    Text("›", color = Green700, fontSize = 28.sp)
                }
            }
            CalendarWeekdayHeader()
            cells.forEach { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    repeat(7) { index ->
                        val day = week.getOrNull(index)
                        CalendarDayCell(
                            day = day,
                            selected = day?.date == selectedDay?.date,
                            onClick = { clickedDay ->
                                onDaySelected(clickedDay)
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarWeekdayHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                color = Muted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: CalendarDay?,
    selected: Boolean,
    onClick: (CalendarDay) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .then(if (day == null) Modifier else Modifier.clickable { onClick(day) })
            .padding(vertical = 5.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (day == null) return@Box
        val background = if (selected) Green100 else Color.Transparent
        Text(
            text = day.date.dayOfMonth.toString(),
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(17.dp))
                .background(background)
                .padding(top = 7.dp),
            color = if (selected) Green900 else Ink,
            fontSize = 15.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun CalendarSelectedDayTimes(day: CalendarDay) {
    InfoCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = day.prayerTime.date,
                color = Ink,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Prayer",
                    color = Muted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(22.dp)) {
                    Text(
                        text = "Starts",
                        color = Muted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Jama'ah",
                        color = Muted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            CalendarSelectedTimeRow("Fajr", day.prayerTime.fajrBegins, day.prayerTime.fajrJamaah)
            CalendarSelectedTimeRow("Sunrise", day.prayerTime.sunrise, "-")
            CalendarSelectedTimeRow("Dhuhr", day.prayerTime.dhuhrBegins, day.prayerTime.dhuhrJamaah)
            CalendarSelectedTimeRow("Asr", day.prayerTime.asrBegins, day.prayerTime.asrJamaah)
            CalendarSelectedTimeRow("Maghrib", day.prayerTime.maghribBegins, day.prayerTime.maghribJamaah)
            CalendarSelectedTimeRow("Isha", day.prayerTime.ishaBegins, day.prayerTime.ishaJamaah)
        }
    }
}

@Composable
private fun CalendarSelectedTimeRow(
    label: String,
    starts: String,
    jamaah: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE2E8E4), RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = Ink,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Text(
                text = starts,
                color = Green900,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
            )
            Text(
                text = jamaah,
                color = Green900,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
            )
        }
    }
}

private fun parseCalendarDate(value: String): CalendarDate? {
    val text = value
        .replace(Regex("(\\d{1,2})(st|nd|rd|th)\\b", RegexOption.IGNORE_CASE), "$1")
        .replace(",", " ")
    val namedMonth = monthNameRegex.find(text)
    if (namedMonth != null) {
        val monthName = namedMonth.value
        val monthIndex = monthIndex(monthName) ?: return null
        val day = Regex("\\b\\d{1,2}\\b").find(text)?.value?.toIntOrNull() ?: return null
        val year = Regex("\\b\\d{4}\\b").find(text)?.value?.toIntOrNull()
        return CalendarDate(monthIndex, fullMonthName(monthIndex), day, year)
    }
    val numeric = Regex("\\b(\\d{1,2})[/-](\\d{1,2})(?:[/-](\\d{2,4}))?\\b").find(text) ?: return null
    val day = numeric.groupValues[1].toIntOrNull() ?: return null
    val monthIndex = numeric.groupValues[2].toIntOrNull() ?: return null
    if (monthIndex !in 1..12) return null
    val year = numeric.groupValues.getOrNull(3)
        ?.takeIf { it.isNotBlank() }
        ?.toIntOrNull()
        ?.let { if (it < 100) 2000 + it else it }
    return CalendarDate(monthIndex, fullMonthName(monthIndex), day, year)
}

private fun sundayFirstDayOfWeek(
    year: Int,
    month: Int,
    day: Int,
): Int {
    val offsets = intArrayOf(0, 3, 2, 5, 0, 3, 5, 1, 4, 6, 2, 4)
    var adjustedYear = year
    if (month < 3) adjustedYear--
    val sundayFirst = (
        adjustedYear +
            adjustedYear / 4 -
            adjustedYear / 100 +
            adjustedYear / 400 +
            offsets[month - 1] +
            day
        ) % 7
    return sundayFirst
}

private fun monthIndex(value: String): Int? {
    val normalized = value.lowercase().take(3)
    return monthNames.indexOfFirst { it.lowercase().startsWith(normalized) }
        .takeIf { it >= 0 }
        ?.plus(1)
}

private fun fullMonthName(monthIndex: Int): String {
    return monthNames.getOrElse(monthIndex - 1) { "Month $monthIndex" }
}

@Composable
private fun ClassesAndEventsScreen(
    announcements: List<Announcement>,
    announcementStatus: String,
    submitStatus: String?,
    onSubmitAnnouncement: suspend (AnnouncementDraft, String) -> Unit,
    deleteStatus: String?,
    onDeleteAnnouncement: suspend (Announcement, String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        InfoCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle("Class schedule")
                classSchedule.forEach { session ->
                    ScheduleRow(
                        title = session.title,
                        meta = "${session.day} - ${session.time}",
                        detail = session.audience,
                    )
                }
            }
        }
        InfoCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle("Announcements")
                if (announcementStatus.isNotBlank()) {
                    Text(
                        text = announcementStatus,
                        color = Alert,
                        fontSize = 13.sp,
                    )
                }
                if (announcements.isEmpty()) {
                    Text(
                        text = "No announcements at the moment.",
                        color = Muted,
                        fontSize = 14.sp,
                    )
                } else {
                    DeleteAnnouncementControls(
                        announcements = announcements,
                        deleteStatus = deleteStatus,
                        onDeleteAnnouncement = onDeleteAnnouncement,
                    )
                }
            }
        }
        AddAnnouncementCard(
            submitStatus = submitStatus,
            onSubmitAnnouncement = onSubmitAnnouncement,
        )
        InfoCard {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionTitle("Custom alerts")
                Text(
                    text = "Class reminders are set for 1 hour before each listed class.",
                    color = Muted,
                    fontSize = 14.sp,
                )
            }
        }
    }
}

@Composable
private fun DeleteAnnouncementControls(
    announcements: List<Announcement>,
    deleteStatus: String?,
    onDeleteAnnouncement: suspend (Announcement, String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var adminPassword by remember { mutableStateOf("") }
    var pendingDeleteId by remember { mutableStateOf<String?>(null) }
    var localError by remember { mutableStateOf<String?>(null) }

    Text(
        text = "Enter the admin password below to enable manual deletion before expiry.",
        color = Muted,
        fontSize = 13.sp,
    )
    OutlinedTextField(
        value = adminPassword,
        onValueChange = {
            adminPassword = it
            if (it.isNotBlank()) {
                localError = null
            }
        },
        label = { Text("Delete password") },
        modifier = Modifier.fillMaxWidth(),
        colors = announcementFieldColors(),
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
    )
    localError?.let {
        Text(
            text = it,
            color = Alert,
            fontSize = 13.sp,
        )
    }
    deleteStatus?.let {
        Text(
            text = it,
            color = if (it.contains("deleted", ignoreCase = true)) Green700 else Muted,
            fontSize = 13.sp,
        )
    }
    announcements.forEach { announcement ->
        AnnouncementRow(
            announcement = announcement,
            isDeleting = pendingDeleteId == announcement.id,
            onDelete = {
                if (adminPassword.isBlank()) {
                    localError = "Enter the admin password to delete an announcement."
                } else {
                    localError = null
                    pendingDeleteId = announcement.id
                    scope.launch {
                        try {
                            onDeleteAnnouncement(announcement, adminPassword)
                        } finally {
                            pendingDeleteId = null
                        }
                    }
                }
            },
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun AddAnnouncementCard(
    submitStatus: String?,
    onSubmitAnnouncement: suspend (AnnouncementDraft, String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedImage by remember { mutableStateOf<PickedAnnouncementImage?>(null) }
    var startDate by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf(announcementTimeOptions.first()) }
    var eventDate by remember { mutableStateOf("") }
    var eventTime by remember { mutableStateOf(announcementTimeOptions.first()) }
    var password by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    var startTimeMenuExpanded by remember { mutableStateOf(false) }
    var timeMenuExpanded by remember { mutableStateOf(false) }

    InfoCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionTitle("Add announcement")
            Text(
                text = "Enter the admin password to publish a live announcement. Add a start date and time for the 1 hour reminder, then choose when the announcement expires.",
                color = Muted,
                fontSize = 13.sp,
            )
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                colors = announcementFieldColors(),
                singleLine = true,
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                colors = announcementFieldColors(),
                minLines = 3,
            )
            OutlinedTextField(
                value = selectedImage?.fileName ?: "",
                onValueChange = {},
                label = { Text("Selected photo") },
                modifier = Modifier.fillMaxWidth(),
                colors = announcementFieldColors(),
                singleLine = true,
                readOnly = true,
                enabled = false,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            runCatching { pickAnnouncementImage() }
                                .onSuccess {
                                    selectedImage = it
                                    if (it != null) {
                                        localError = null
                                    }
                                }
                                .onFailure {
                                    localError = it.message ?: "Could not open the image picker."
                                }
                        }
                    },
                    enabled = !isSubmitting,
                ) {
                    Text(if (selectedImage == null) "Choose image" else "Change image")
                }
                if (selectedImage != null) {
                    TextButton(
                        onClick = { selectedImage = null },
                        enabled = !isSubmitting,
                    ) {
                        Text("Remove")
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = startDate,
                    onValueChange = { startDate = it },
                    label = { Text("Start date DD/MM/YYYY") },
                    modifier = Modifier.weight(1f),
                    colors = announcementFieldColors(),
                    singleLine = true,
                )
                ExposedDropdownMenuBox(
                    expanded = startTimeMenuExpanded,
                    onExpandedChange = { startTimeMenuExpanded = it },
                    modifier = Modifier.weight(1f),
                ) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Start time") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = startTimeMenuExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor(
                                type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                                enabled = true,
                            )
                            .fillMaxWidth(),
                        colors = announcementFieldColors(),
                        singleLine = true,
                    )
                    ExposedDropdownMenu(
                        expanded = startTimeMenuExpanded,
                        onDismissRequest = { startTimeMenuExpanded = false },
                    ) {
                        announcementTimeOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    startTime = option
                                    startTimeMenuExpanded = false
                                },
                            )
                        }
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = eventDate,
                    onValueChange = { eventDate = it },
                    label = { Text("Expiry date DD/MM/YYYY") },
                    modifier = Modifier.weight(1f),
                    colors = announcementFieldColors(),
                    singleLine = true,
                )
                ExposedDropdownMenuBox(
                    expanded = timeMenuExpanded,
                    onExpandedChange = { timeMenuExpanded = it },
                    modifier = Modifier.weight(1f),
                ) {
                    OutlinedTextField(
                        value = eventTime,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Expiry time") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = timeMenuExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor(
                                type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                                enabled = true,
                            )
                            .fillMaxWidth(),
                        colors = announcementFieldColors(),
                        singleLine = true,
                    )
                    ExposedDropdownMenu(
                        expanded = timeMenuExpanded,
                        onDismissRequest = { timeMenuExpanded = false },
                    ) {
                        announcementTimeOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    eventTime = option
                                    timeMenuExpanded = false
                                },
                            )
                        }
                    }
                }
            }
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Admin password") },
                modifier = Modifier.fillMaxWidth(),
                colors = announcementFieldColors(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
            )
            localError?.let {
                Text(
                    text = it,
                    color = Alert,
                    fontSize = 13.sp,
                )
            }
            submitStatus?.let {
                Text(
                    text = it,
                    color = if (it.contains("sent", ignoreCase = true)) Green700 else Muted,
                    fontSize = 13.sp,
                )
            }
            Button(
                onClick = {
                    val draft = buildAnnouncementDraft(
                        title = title,
                        description = description,
                        startDate = startDate,
                        startTime = startTime,
                        eventDate = eventDate,
                        eventTime = eventTime,
                        selectedImage = selectedImage,
                    )
                    if (draft == null) {
                        localError = "Fill in title, description, start date/time, and expiry date/time using the requested formats."
                        return@Button
                    }
                    if (password.isBlank()) {
                        localError = "Enter the admin password to publish the announcement."
                        return@Button
                    }
                    localError = null
                    isSubmitting = true
                    scope.launch {
                        try {
                            onSubmitAnnouncement(draft, password)
                            title = ""
                            description = ""
                            selectedImage = null
                            startDate = ""
                            startTime = announcementTimeOptions.first()
                            eventDate = ""
                            eventTime = announcementTimeOptions.first()
                            password = ""
                        } finally {
                            isSubmitting = false
                        }
                    }
                },
                enabled = !isSubmitting,
            ) {
                Text(if (isSubmitting) "Sending..." else "Publish announcement")
            }
        }
    }
}

@Composable
private fun AnnouncementRow(
    announcement: Announcement,
    isDeleting: Boolean = false,
    onDelete: (() -> Unit)? = null,
) {
    ScheduleRow(
        title = announcement.title,
        meta = "Starts ${announcement.startDate} at ${announcement.startTime}",
        detail = announcement.description,
        footer = buildList {
            add("Expires ${announcement.eventDate} at ${announcement.eventTime}")
            if (!announcement.mediaUrl.isNullOrBlank()) add("Photo attached")
        }.joinToString(" • "),
        action = onDelete?.let { deleteAction ->
            {
                TextButton(
                    onClick = deleteAction,
                    enabled = !isDeleting,
                ) {
                    Text(if (isDeleting) "Deleting..." else "Delete")
                }
            }
        },
    )
}

@Composable
private fun ScheduleRow(
    title: String,
    meta: String,
    detail: String,
    footer: String? = null,
    action: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFFBFDFB))
            .border(1.dp, Color(0xFFE2E8E4), RoundedCornerShape(8.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = title,
            color = Ink,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = meta,
            color = Alert,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = detail,
            color = Muted,
            fontSize = 13.sp,
        )
        footer?.let {
            Text(
                text = it,
                color = Green700,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        action?.invoke()
    }
}

@Composable
private fun announcementFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Green700,
    unfocusedBorderColor = Color(0xFFD2DDD8),
    focusedLabelColor = Green700,
    unfocusedLabelColor = Muted,
    cursorColor = Green700,
)

private fun buildAnnouncementDraft(
    title: String,
    description: String,
    startDate: String,
    startTime: String,
    eventDate: String,
    eventTime: String,
    selectedImage: PickedAnnouncementImage?,
): AnnouncementDraft? {
    val trimmedTitle = title.trim()
    val trimmedDescription = description.trim()
    val trimmedStartDate = startDate.trim()
    val trimmedStartTime = startTime.trim()
    val trimmedDate = eventDate.trim()
    val trimmedTime = eventTime.trim()
    val validStartDate = Regex("""\d{2}/\d{2}/\d{4}""").matches(trimmedStartDate)
    val validStartTime = Regex("""\d{2}:\d{2}""").matches(trimmedStartTime)
    val validDate = Regex("""\d{2}/\d{2}/\d{4}""").matches(trimmedDate)
    val validTime = Regex("""\d{2}:\d{2}""").matches(trimmedTime)
    if (
        trimmedTitle.isBlank() ||
        trimmedDescription.isBlank() ||
        !validStartDate ||
        !validStartTime ||
        !validDate ||
        !validTime
    ) return null
    return AnnouncementDraft(
        title = trimmedTitle,
        description = trimmedDescription,
        startDate = trimmedStartDate,
        startTime = trimmedStartTime,
        eventDate = trimmedDate,
        eventTime = trimmedTime,
        mediaBase64 = selectedImage?.let { encodeAnnouncementImageBase64(it.bytes) },
        mediaMimeType = selectedImage?.mimeType,
        mediaFileName = selectedImage?.fileName,
    )
}


@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = Ink,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun InfoCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

private fun currentPrayer(
    prayerTimes: List<PrayerTime>,
    minuteOfDay: Int,
): PrayerTime {
    return prayerTimes.lastOrNull { minuteOfDay >= it.minuteOfDay } ?: prayerTimes.last()
}

internal fun upcomingPrayer(
    timetable: PrayerTimetable,
    minuteOfDay: Int,
    isoDayOfWeek: Int,
): UpcomingPrayer {
    val candidates = timetable.dailyPrayerTimes.map { prayer ->
        if (isoDayOfWeek == FRIDAY && prayer.name == "Zuhr") {
            UpcomingPrayer(
                name = "Jum'ah",
                jamaahTime = timetable.jumahTime.salaatTime,
                minuteOfDay = toMinuteOfDay(timetable.jumahTime.salaatTime),
            )
        } else {
            UpcomingPrayer(
                name = prayer.name,
                jamaahTime = prayer.jamaahTime,
                minuteOfDay = prayer.minuteOfDay,
            )
        }
    }
    return candidates.firstOrNull { minuteOfDay < it.minuteOfDay } ?: candidates.first()
}

private const val FRIDAY = 5
private val monthNames = listOf(
    "January",
    "February",
    "March",
    "April",
    "May",
    "June",
    "July",
    "August",
    "September",
    "October",
    "November",
    "December",
)

internal fun parseFullCalendarTimetable(html: String): List<CalendarPrayerTime> {
    val calendarYear = publishedCalendarYear(html)
    return Regex("<tr[\\s\\S]*?</tr>", RegexOption.IGNORE_CASE)
        .findAll(html)
        .mapNotNull { match -> parseCalendarPrayerRow(match.value, calendarYear) }
        .distinctBy { it.date }
        .toList()
}

private fun publishedCalendarYear(html: String): Int {
    return extractCalendarYear(html) ?: currentDateTimeComponents().year
}

private fun parseCalendarPrayerRow(
    row: String,
    calendarYear: Int?,
): CalendarPrayerTime? {
    val cells = Regex("<t[dh][^>]*>([\\s\\S]*?)</t[dh]>", RegexOption.IGNORE_CASE)
        .findAll(row)
        .map { match -> cleanHtmlText(match.groupValues[1]) }
        .filter { it.isNotBlank() }
        .toList()
    if (cells.size < 7) return null

    val rawDate = cells.firstOrNull { cell ->
        monthNameRegex.containsMatchIn(cell) || numericDateRegex.containsMatchIn(cell)
    } ?: return null
    val date = if (calendarYear != null && !rawDate.contains(Regex("\\b\\d{4}\\b"))) {
        "$rawDate $calendarYear"
    } else {
        rawDate
    }

    val times = cells
        .drop(1)
        .flatMap { cell -> clockTimeRegex.findAll(cell).map { it.value }.toList() }
        .map(::normalizeClockTime)
    if (times.size < 6) return null

    val prayerTimes = when {
        times.size >= 11 -> CalendarPrayerValues(
            fajrBegins = times[0],
            sunrise = times[1],
            fajrJamaah = times[2],
            dhuhrBegins = times[3],
            dhuhrJamaah = times[4],
            asrBegins = times[5],
            asrJamaah = times[6],
            maghribBegins = times[7],
            maghribJamaah = times[8],
            ishaBegins = times[9],
            ishaJamaah = times[10],
        )
        else -> CalendarPrayerValues(
            fajrBegins = times[0],
            fajrJamaah = times[0],
            sunrise = times[1],
            dhuhrBegins = times[2],
            dhuhrJamaah = times[2],
            asrBegins = times[3],
            asrJamaah = times[3],
            maghribBegins = times[4],
            maghribJamaah = times[4],
            ishaBegins = times[5],
            ishaJamaah = times[5],
        )
    }

    return CalendarPrayerTime(
        date = date,
        fajrBegins = prayerTimes.fajrBegins,
        fajrJamaah = prayerTimes.fajrJamaah,
        sunrise = prayerTimes.sunrise,
        dhuhrBegins = prayerTimes.dhuhrBegins,
        dhuhrJamaah = prayerTimes.dhuhrJamaah,
        asrBegins = prayerTimes.asrBegins,
        asrJamaah = prayerTimes.asrJamaah,
        maghribBegins = prayerTimes.maghribBegins,
        maghribJamaah = prayerTimes.maghribJamaah,
        ishaBegins = prayerTimes.ishaBegins,
        ishaJamaah = prayerTimes.ishaJamaah,
    )
}

private data class CalendarPrayerValues(
    val fajrBegins: String,
    val fajrJamaah: String,
    val sunrise: String,
    val dhuhrBegins: String,
    val dhuhrJamaah: String,
    val asrBegins: String,
    val asrJamaah: String,
    val maghribBegins: String,
    val maghribJamaah: String,
    val ishaBegins: String,
    val ishaJamaah: String,
)

internal fun parseDarulUmmahTimetable(html: String): PrayerTimetable {
    parseCurrentPrayerGrid(html)?.let { return it }
    parseTodayPrayerTimetableFromCalendar(html)?.let { return it }

    val timetableBody = html
        .substringAfter("<tbody class=\"timetable-font\"", missingDelimiterValue = "")
        .substringBefore("</tbody>")
    require(timetableBody.isNotBlank()) { "Darul Ummah timetable body was not found" }

    val rows = Regex("<tr[\\s\\S]*?</tr>").findAll(timetableBody).map { it.value }.toList()
    val beginsTimes = parseTimetableRow(rows.firstOrNull { it.contains("BEGINS") })
    val jamaahTimes = parseTimetableRow(rows.firstOrNull { it.contains("JAMA") })
    require(beginsTimes.size >= 6 && jamaahTimes.size >= 6) {
        "Darul Ummah timetable rows were incomplete"
    }

    val names = listOf("Fajr", "Zuhr", "Asr", "Maghrib", "Isha")
    return PrayerTimetable(
        dailyPrayerTimes = names.mapIndexed { index, name ->
            PrayerTime(
                name = name,
                beginsTime = beginsTimes[index].displayTime,
                jamaahTime = jamaahTimes[index].displayTime,
                minuteOfDay = jamaahTimes[index].minuteOfDay,
            )
        },
        jumahTime = JumahTime(
            khutbahTime = beginsTimes[5].displayTime,
            salaatTime = jamaahTimes[5].displayTime,
        ),
    )
}

private fun parseTodayPrayerTimetableFromCalendar(html: String): PrayerTimetable? {
    val currentDate = extractDisplayedDate(html) ?: return null
    val currentDayRow = parseFullCalendarTimetable(html)
        .firstOrNull { prayerTime ->
            val parsed = parseCalendarDate(prayerTime.date) ?: return@firstOrNull false
            parsed.year == currentDate.year &&
                parsed.monthIndex == currentDate.monthIndex &&
                parsed.dayOfMonth == currentDate.dayOfMonth
        }
        ?: return null

    return PrayerTimetable(
        dailyPrayerTimes = listOf(
            PrayerTime("Fajr", currentDayRow.fajrBegins, currentDayRow.fajrJamaah, toMinuteOfDay(currentDayRow.fajrJamaah)),
            PrayerTime("Zuhr", currentDayRow.dhuhrBegins, currentDayRow.dhuhrJamaah, toMinuteOfDay(currentDayRow.dhuhrJamaah)),
            PrayerTime("Asr", currentDayRow.asrBegins, currentDayRow.asrJamaah, toMinuteOfDay(currentDayRow.asrJamaah)),
            PrayerTime("Maghrib", currentDayRow.maghribBegins, currentDayRow.maghribJamaah, toMinuteOfDay(currentDayRow.maghribJamaah)),
            PrayerTime("Isha", currentDayRow.ishaBegins, currentDayRow.ishaJamaah, toMinuteOfDay(currentDayRow.ishaJamaah)),
        ),
        jumahTime = fallbackPrayerTimetable.jumahTime,
    )
}

private fun parseCurrentPrayerGrid(html: String): PrayerTimetable? {
    val dailySection = html
        .substringAfter("<div class=\"col-md-4\">", missingDelimiterValue = "")
        .substringBefore("<div class=\"col-md-8\"", missingDelimiterValue = "")
    if (dailySection.isBlank()) return null

    val values = Regex("<div class=\"Demo\">([\\s\\S]*?)</div>", RegexOption.IGNORE_CASE)
        .findAll(dailySection)
        .map { cleanHtmlText(it.groupValues[1]) }
        .filter { it.isNotBlank() }
        .toList()
    if (values.size < 20) return null

    val pairs = values.chunked(2)
        .mapNotNull { chunk ->
            val label = chunk.getOrNull(0) ?: return@mapNotNull null
            val value = chunk.getOrNull(1) ?: return@mapNotNull null
            label.uppercase() to value
        }
        .toMap()

    val fajrBegins = parseMeridiemTime(pairs["FAJR BEGINS"] ?: return null)
    val fajrJamaah = parseMeridiemTime(pairs["FAJR JAMA'AH"] ?: return null)
    val zuhrBegins = parseMeridiemTime(pairs["ZUHR BEGINS"] ?: return null)
    val zuhrJamaah = parseMeridiemTime(pairs["ZUHR JAMA'AH"] ?: return null)
    val asrBegins = parseMeridiemTime(pairs["ASR BEGINS"] ?: return null)
    val asrJamaah = parseMeridiemTime(pairs["ASR JAMA'AH"] ?: return null)
    val maghribBegins = parseMeridiemTime(pairs["MAGHRIB BEGINS"] ?: return null)
    val maghribJamaah = parseMeridiemTime(pairs["MAGHRIB JAMA'AH"] ?: return null)
    val ishaBegins = parseMeridiemTime(pairs["ISHA BEGINS"] ?: return null)
    val ishaJamaah = parseMeridiemTime(pairs["ISHA JAMA'AH"] ?: return null)

    return PrayerTimetable(
        dailyPrayerTimes = listOf(
            PrayerTime("Fajr", fajrBegins.displayTime, fajrJamaah.displayTime, fajrJamaah.minuteOfDay),
            PrayerTime("Zuhr", zuhrBegins.displayTime, zuhrJamaah.displayTime, zuhrJamaah.minuteOfDay),
            PrayerTime("Asr", asrBegins.displayTime, asrJamaah.displayTime, asrJamaah.minuteOfDay),
            PrayerTime("Maghrib", maghribBegins.displayTime, maghribJamaah.displayTime, maghribJamaah.minuteOfDay),
            PrayerTime("Isha", ishaBegins.displayTime, ishaJamaah.displayTime, ishaJamaah.minuteOfDay),
        ),
        jumahTime = fallbackPrayerTimetable.jumahTime,
    )
}

private data class ParsedTime(
    val displayTime: String,
    val minuteOfDay: Int,
)

private fun parseTimetableRow(row: String?): List<ParsedTime> {
    require(!row.isNullOrBlank()) { "Darul Ummah timetable row was not found" }
    return Regex("(\\d{1,2}:\\d{2})<span[^>]*>\\s*(AM|PM)\\s*</span>", RegexOption.IGNORE_CASE)
        .findAll(row)
        .map { match ->
            val time = match.groupValues[1]
            val period = match.groupValues[2]
            val minuteOfDay = toMinuteOfDay(time, period)
            ParsedTime(formatTime(minuteOfDay), minuteOfDay)
        }
        .toList()
}

private fun parseMeridiemTime(value: String): ParsedTime {
    val match = meridiemTimeRegex.find(value)
        ?: error("Could not parse time: $value")
    val minuteOfDay = toMinuteOfDay(match.groupValues[1], match.groupValues[2])
    return ParsedTime(
        displayTime = formatTime(minuteOfDay),
        minuteOfDay = minuteOfDay,
    )
}

private fun extractCalendarYear(html: String): Int? {
    return Regex("FULL\\s+TIMETABLE\\s*(\\d{4})", RegexOption.IGNORE_CASE)
        .find(html)
        ?.groupValues
        ?.getOrNull(1)
        ?.toIntOrNull()
        ?: Regex("Prayer\\s+Timetable\\s*(\\d{4})", RegexOption.IGNORE_CASE)
            .find(html)
            ?.groupValues
            ?.getOrNull(1)
            ?.toIntOrNull()
}

private fun extractDisplayedDate(html: String): CalendarDate? {
    val value = Regex(
        "\\b(?:Mon|Tue|Wed|Thu|Fri|Sat|Sun)[a-z]*,\\s+\\d{1,2}(?:st|nd|rd|th)?\\s+[A-Za-z]+\\s+\\d{4}\\b",
        RegexOption.IGNORE_CASE,
    ).find(html)?.value ?: return null
    return parseCalendarDate(value)
}

private fun parseYouTubeRecentVideos(feedXml: String): List<YouTubeVideo> {
    return Regex("<entry>([\\s\\S]*?)</entry>", RegexOption.IGNORE_CASE)
        .findAll(feedXml)
        .mapNotNull { entry ->
            val entryXml = entry.groupValues[1]
            val videoId = xmlTagValue(entryXml, "yt:videoId") ?: return@mapNotNull null
            val title = xmlTagValue(entryXml, "title")?.let(::decodeXmlText) ?: return@mapNotNull null
            val publishedDate = xmlTagValue(entryXml, "published")
                ?.let(::formatYouTubePublishedDate)
                .orEmpty()
            YouTubeVideo(
                id = videoId,
                title = title,
                publishedDate = publishedDate,
            )
        }
        .toList()
}

private fun xmlTagValue(
    xml: String,
    tagName: String,
): String? {
    return Regex("<$tagName>([\\s\\S]*?)</$tagName>", RegexOption.IGNORE_CASE)
        .find(xml)
        ?.groupValues
        ?.getOrNull(1)
        ?.trim()
}

private fun decodeXmlText(value: String): String {
    return value
        .replace("&amp;", "&")
        .replace("&quot;", "\"")
        .replace("&apos;", "'")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&#8217;", "'")
        .replace("&#x27;", "'")
        .replace(Regex("\\s+"), " ")
        .trim()
}

private fun formatYouTubePublishedDate(value: String): String {
    val date = Regex("""^(\d{4})-(\d{2})-(\d{2})""")
        .find(value)
        ?.groupValues
        ?: return ""
    val year = date.getOrNull(1) ?: return ""
    val month = date.getOrNull(2)?.toIntOrNull() ?: return ""
    val day = date.getOrNull(3)?.toIntOrNull() ?: return ""
    return "${day.toString().padStart(2, '0')} ${fullMonthName(month)} $year"
}

private val monthNameRegex = Regex(
    "\\b(January|February|March|April|May|June|July|August|September|October|November|December|" +
        "Jan|Feb|Mar|Apr|Jun|Jul|Aug|Sep|Sept|Oct|Nov|Dec)\\b",
    RegexOption.IGNORE_CASE,
)
private val numericDateRegex = Regex("\\b\\d{1,2}[/-]\\d{1,2}([/-]\\d{2,4})?\\b")
private val clockTimeRegex = Regex("\\b\\d{1,2}:\\d{2}\\b")
private val meridiemTimeRegex = Regex("(\\d{1,2}:\\d{2})\\s*(AM|PM)", RegexOption.IGNORE_CASE)

private fun cleanHtmlText(value: String): String {
    return value
        .replace(Regex("<[^>]+>"), " ")
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace("&#8211;", "-")
        .replace("&#8217;", "'")
        .replace(Regex("\\s+"), " ")
        .trim()
}

private fun normalizeClockTime(value: String): String {
    val parts = value.split(":")
    return "${parts[0].padStart(2, '0')}:${parts[1]}"
}

private fun toMinuteOfDay(
    time: String,
    period: String,
): Int {
    val parts = time.split(":")
    val rawHour = parts[0].toInt()
    val minute = parts[1].toInt()
    val hour = when {
        period.equals("AM", ignoreCase = true) && rawHour == 12 -> 0
        period.equals("PM", ignoreCase = true) && rawHour != 12 -> rawHour + 12
        else -> rawHour
    }
    return hour * 60 + minute
}

private fun formatTime(minuteOfDay: Int): String {
    val hour = minuteOfDay / 60
    val minute = minuteOfDay % 60
    return "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
}

private fun toMinuteOfDay(time: String): Int {
    val parts = time.split(":")
    return parts[0].toInt() * 60 + parts[1].toInt()
}

private fun secondsUntil(targetSecond: Int, currentSecond: Int): Int {
    return if (targetSecond > currentSecond) {
        targetSecond - currentSecond
    } else {
        (24 * 60 * 60 - currentSecond) + targetSecond
    }
}

internal fun formatCountdown(seconds: Int): String {
    val hoursPart = seconds / 3_600
    val minutesPart = (seconds % 3_600) / 60
    val secondsPart = seconds % 60
    return "${hoursPart}h ${minutesPart.toString().padStart(2, '0')}m " +
        "${secondsPart.toString().padStart(2, '0')}s remaining"
}
