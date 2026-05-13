package com.example.darulummahapp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.darkColorScheme
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
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import darulummahapp.composeapp.generated.resources.Res
import darulummahapp.composeapp.generated.resources.darul_ummah_logo
import darulummahapp.composeapp.generated.resources.hall_hire_1
import darulummahapp.composeapp.generated.resources.hall_hire_2
import darulummahapp.composeapp.generated.resources.hall_hire_3
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
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

private data class AppColors(
    val page: Color,
    val gradient: List<Color>,
    val header: Color,
    val card: Color,
    val cardAlt: Color,
    val text: Color,
    val muted: Color,
    val border: Color,
    val selected: Color,
    val nav: Color,
    val navBorder: Color,
    val field: Color,
)

private val LightAppColors = AppColors(
    page = SiteBlack,
    gradient = listOf(SiteBlack, Green900, Green700),
    header = SiteCharcoal,
    card = Color.White,
    cardAlt = Color(0xFFFBFDFB),
    text = Ink,
    muted = Muted,
    border = Color(0xFFE2E8E4),
    selected = Green100,
    nav = Color.White,
    navBorder = Color.White.copy(alpha = 0.55f),
    field = Color.White,
)

private val DarkAppColors = AppColors(
    page = Color(0xFF071514),
    gradient = listOf(Color(0xFF050D0C), Color(0xFF0A2524), Color(0xFF0E4C46)),
    header = Color(0xFF142321),
    card = Color(0xFF10201E),
    cardAlt = Color(0xFF132825),
    text = Color(0xFFF2F7F4),
    muted = Color(0xFFA9BBB3),
    border = Color(0xFF28413D),
    selected = Color(0xFF173F3A),
    nav = Color(0xFF0E1D1B),
    navBorder = Color(0xFF2B4944),
    field = Color(0xFF0B1917),
)

private val LocalAppColors = staticCompositionLocalOf { LightAppColors }
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

private fun mergeCreatedAnnouncement(
    createdAnnouncement: Announcement?,
    refreshedAnnouncements: List<Announcement>,
): List<Announcement> {
    if (createdAnnouncement == null) return refreshedAnnouncements
    return (listOf(createdAnnouncement) + refreshedAnnouncements.filterNot { it.id == createdAnnouncement.id })
        .sortedByDescending { it.createdAt }
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

internal data class YouTubeVideo(
    val id: String,
    val title: String,
    val publishedDate: String,
)

data class ClassSession(
    val title: String,
    val day: String,
    val bstTime: String,
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
    Donate,
    Qibla,
    HallHire,
    Settings,
    FullCalendar,
}

private enum class BottomNavIcon {
    Time,
    Events,
    Donate,
    Live,
    Compass,
    Hall,
}

private data class BottomNavItem(
    val screen: AppScreen,
    val label: String,
    val icon: BottomNavIcon,
    val prominent: Boolean = false,
)

private val bottomNavItems = listOf(
    BottomNavItem(AppScreen.Home, "Times", BottomNavIcon.Time),
    BottomNavItem(AppScreen.Classes, "Events", BottomNavIcon.Events),
    BottomNavItem(AppScreen.YouTube, "Live", BottomNavIcon.Live),
    BottomNavItem(AppScreen.Qibla, "Qibla", BottomNavIcon.Compass),
    BottomNavItem(AppScreen.HallHire, "Hall", BottomNavIcon.Hall),
)
private val donateNavItem = BottomNavItem(AppScreen.Donate, "Donate", BottomNavIcon.Donate, prominent = true)
private val hallHireImages: List<DrawableResource> = listOf(
    Res.drawable.hall_hire_1,
    Res.drawable.hall_hire_2,
    Res.drawable.hall_hire_3,
)

internal const val DarulUmmahYouTubeChannelId = "UCy7hFfaw0R-z8Mpg4zwMJrA"
internal const val DarulUmmahYouTubeChannelUrl = "https://www.youtube.com/@DarulUmmahMosque"
internal const val YouTubeFetchUserAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0 Safari/537.36"
internal const val YouTubeConsentCookie = "CONSENT=YES+; SOCS=CAAaBgiA8vnPBg"
private const val DarulUmmahDonationUrl = "https://pay.sumup.com/b2c/Q3XVB1B0"
private const val PrivacyPolicyUrl = "https://sultanyahmed.github.io/DarulUmmahApp/privacy-policy.html"

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

private fun bundledPrayerTimetableForToday(): PrayerTimetable {
    val today = currentDateTimeComponents()
    return darulUmmahPrayerCalendar2026.prayerTimetableForDate(today) ?: fallbackPrayerTimetable
}

private fun List<CalendarPrayerTime>.prayerTimetableForDate(date: DateTimeComponents): PrayerTimetable? {
    val prayerTime = firstOrNull { calendarPrayerTime ->
        val parsed = parseCalendarDate(calendarPrayerTime.date) ?: return@firstOrNull false
        parsed.year == date.year &&
            parsed.monthIndex == date.month &&
            parsed.dayOfMonth == date.day
    } ?: return null

    return PrayerTimetable(
        dailyPrayerTimes = listOf(
            PrayerTime("Fajr", prayerTime.fajrBegins, prayerTime.fajrJamaah, toMinuteOfDay(prayerTime.fajrJamaah)),
            PrayerTime("Zuhr", prayerTime.dhuhrBegins, prayerTime.dhuhrJamaah, toMinuteOfDay(prayerTime.dhuhrJamaah)),
            PrayerTime("Asr", prayerTime.asrBegins, prayerTime.asrJamaah, toMinuteOfDay(prayerTime.asrJamaah)),
            PrayerTime("Maghrib", prayerTime.maghribBegins, prayerTime.maghribJamaah, toMinuteOfDay(prayerTime.maghribJamaah)),
            PrayerTime("Isha", prayerTime.ishaBegins, prayerTime.ishaJamaah, toMinuteOfDay(prayerTime.ishaJamaah)),
        ),
        jumahTime = fallbackPrayerTimetable.jumahTime,
    )
}

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
        bstTime = "19:00",
        audience = "Community",
        reminderIsoDayOfWeek = 2,
        reminderMinuteOfDay = 19 * 60,
    ),
    ClassSession(
        title = "Women's Tafsir (Bangla)",
        day = "Wednesday",
        bstTime = "11:00 - 12:30",
        audience = "Sisters",
        reminderIsoDayOfWeek = 3,
        reminderMinuteOfDay = 11 * 60,
    ),
    ClassSession(
        title = "Islamic Studies",
        day = "Wednesday Evenings",
        bstTime = "19:00",
        audience = "Community",
        reminderIsoDayOfWeek = 3,
        reminderMinuteOfDay = 19 * 60,
    ),
    ClassSession(
        title = "Women's Tarteel Class",
        day = "Thursday Evenings",
        bstTime = "19:00",
        audience = "Sisters",
        reminderIsoDayOfWeek = 4,
        reminderMinuteOfDay = 19 * 60,
    ),
    ClassSession(
        title = "Hadeeth Class",
        day = "Thursday Evenings",
        bstTime = "19:00",
        audience = "Community",
        reminderIsoDayOfWeek = 4,
        reminderMinuteOfDay = 19 * 60,
    ),
    ClassSession(
        title = "Tarteel Class",
        day = "Friday Evenings",
        bstTime = "19:00",
        audience = "Community",
        reminderIsoDayOfWeek = 5,
        reminderMinuteOfDay = 19 * 60,
    ),
    ClassSession(
        title = "Bangla Tafseer",
        day = "Sunday Evenings",
        bstTime = "19:00",
        audience = "Community",
        reminderIsoDayOfWeek = 7,
        reminderMinuteOfDay = 19 * 60,
    ),
)

internal fun classSessionDisplayTime(
    session: ClassSession,
    date: DateTimeComponents = currentDateTimeComponents(),
): String {
    return shiftClassTimeForLondonSeason(session.bstTime, date)
}

internal fun classSessionReminderMinuteOfDay(
    session: ClassSession,
    date: DateTimeComponents = currentDateTimeComponents(),
): Int? {
    return session.reminderMinuteOfDay?.let { shiftClassMinuteForLondonSeason(it, date) }
}

internal fun isBritishSummerTime(date: DateTimeComponents): Boolean {
    return when (date.month) {
        in 4..9 -> true
        in listOf(1, 2, 11, 12) -> false
        3 -> date.day >= lastSundayOfMonth(date.year, 3)
        10 -> date.day < lastSundayOfMonth(date.year, 10)
        else -> false
    }
}

private fun shiftClassTimeForLondonSeason(
    bstTime: String,
    date: DateTimeComponents,
): String {
    if (isBritishSummerTime(date)) return bstTime
    return Regex("""\b\d{1,2}:\d{2}\b""").replace(bstTime) { match ->
        minuteOfDayToTime(shiftClassMinuteForLondonSeason(toMinuteOfDay(match.value), date))
    }
}

private fun shiftClassMinuteForLondonSeason(
    bstMinuteOfDay: Int,
    date: DateTimeComponents,
): Int {
    return if (isBritishSummerTime(date)) {
        bstMinuteOfDay
    } else {
        ((bstMinuteOfDay - 60) + 24 * 60) % (24 * 60)
    }
}

private fun lastSundayOfMonth(year: Int, month: Int): Int {
    val lastDay = daysInMonth(year, month)
    return lastDay - sundayFirstDayOfWeek(year, month, lastDay)
}

private fun daysInMonth(year: Int, month: Int): Int {
    return when (month) {
        4, 6, 9, 11 -> 30
        2 -> if (isLeapYear(year)) 29 else 28
        else -> 31
    }
}

private fun minuteOfDayToTime(minuteOfDay: Int): String {
    val hour = minuteOfDay / 60
    val minute = minuteOfDay % 60
    return "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
}

@Composable
@Preview
fun App() {
    val systemDarkMode = isSystemInDarkTheme()
    var darkModeEnabled by rememberSaveable {
        mutableStateOf(loadDarkModePreference() ?: systemDarkMode)
    }
    val appColors = if (darkModeEnabled) DarkAppColors else LightAppColors
    val materialColors = if (darkModeEnabled) {
        darkColorScheme(
            primary = Green500,
            secondary = Gold,
            background = appColors.page,
            surface = appColors.card,
            onSurface = appColors.text,
        )
    } else {
        lightColorScheme(
            primary = Green700,
            secondary = Gold,
            background = appColors.page,
            surface = appColors.card,
            onSurface = appColors.text,
        )
    }

    MaterialTheme(colorScheme = materialColors) {
        CompositionLocalProvider(LocalAppColors provides appColors) {
        val announcementRepository = remember { AnnouncementRepository() }
        var screen by rememberSaveable { mutableStateOf(AppScreen.Home) }
        var minuteOfDay by remember { mutableIntStateOf(currentMinuteOfDay()) }
        var secondOfDay by remember { mutableIntStateOf(currentSecondOfDay()) }
        var isoDayOfWeek by remember { mutableIntStateOf(currentIsoDayOfWeek()) }
        var prayerTimetable by remember { mutableStateOf(bundledPrayerTimetableForToday()) }
        var notificationPreferences by remember { mutableStateOf(loadNotificationPreferences()) }
        var updateStatus by remember {
            mutableStateOf("Using the bundled Darul Ummah $DarulUmmahPrayerCalendarYear calendar.")
        }
        var refreshKey by remember { mutableIntStateOf(0) }
        var announcements by remember { mutableStateOf<List<Announcement>>(emptyList()) }
        var announcementStatus by remember { mutableStateOf("Connecting live announcements...") }
        var announcementSubmitStatus by remember { mutableStateOf<String?>(null) }
        var announcementDeleteStatus by remember { mutableStateOf<String?>(null) }
        var expandedAnnouncementMediaUrl by remember { mutableStateOf<String?>(null) }
        var expandedHallImageIndex by remember { mutableStateOf<Int?>(null) }

        LaunchedEffect(darkModeEnabled) {
            saveDarkModePreference(darkModeEnabled)
        }

        LaunchedEffect(Unit) {
            while (true) {
                val now = currentDateTimeComponents()
                minuteOfDay = now.hour * 60 + now.minute
                secondOfDay = now.hour * 60 * 60 + now.minute * 60 + now.second
                isoDayOfWeek = currentIsoDayOfWeek()
                val currentTimetable = darulUmmahPrayerCalendar2026.prayerTimetableForDate(now)
                if (currentTimetable != null && currentTimetable != prayerTimetable) {
                    prayerTimetable = currentTimetable
                }
                delay(1_000)
            }
        }

        LaunchedEffect(refreshKey) {
            if (refreshKey == 0) return@LaunchedEffect
            prayerTimetable = bundledPrayerTimetableForToday()
            updateStatus = "Reloaded from the bundled Darul Ummah $DarulUmmahPrayerCalendarYear calendar."
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
            color = appColors.page,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = appColors.gradient,
                        ),
                    ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeContentPadding()
                        .verticalScroll(rememberScrollState())
                        .padding(start = 18.dp, top = 16.dp, end = 18.dp, bottom = 128.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Header(onSettingsClick = { screen = AppScreen.Settings })

                    when (screen) {
                        AppScreen.Home -> HomeScreen(
                            minuteOfDay = minuteOfDay,
                            secondOfDay = secondOfDay,
                            isoDayOfWeek = isoDayOfWeek,
                            prayerTimetable = prayerTimetable,
                            updateStatus = updateStatus,
                            onRefresh = { refreshKey++ },
                            onFullCalendarClick = { screen = AppScreen.FullCalendar },
                        )
                        AppScreen.Classes -> ClassesAndEventsScreen(
                            announcements = announcements,
                            announcementStatus = announcementStatus,
                            deleteStatus = announcementDeleteStatus,
                            onAnnouncementMediaClick = { expandedAnnouncementMediaUrl = it },
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
                        AppScreen.Donate -> DonateScreen()
                        AppScreen.Qibla -> QiblaCompassScreen()
                        AppScreen.HallHire -> HallHireScreen(
                            onImageClick = { expandedHallImageIndex = it },
                        )
                        AppScreen.Settings -> SettingsScreen(
                            darkModeEnabled = darkModeEnabled,
                            onDarkModeEnabledChanged = { darkModeEnabled = it },
                            notificationPreferences = notificationPreferences,
                            onNotificationPreferencesChanged = { notificationPreferences = it },
                            submitStatus = announcementSubmitStatus,
                            onSubmitAnnouncement = { draft, password ->
                                announcementSubmitStatus = "Sending announcement..."
                                try {
                                    val createdAnnouncement = announcementRepository.submitAnnouncement(draft, password)
                                    val refreshedFeed = announcementRepository.fetchAnnouncements()
                                    announcements = mergeCreatedAnnouncement(
                                        createdAnnouncement = createdAnnouncement,
                                        refreshedAnnouncements = refreshedFeed.announcements,
                                    )
                                    announcementStatus = refreshedFeed.status
                                    announcementSubmitStatus = "Announcement sent."
                                } catch (error: Throwable) {
                                    announcementSubmitStatus = error.message ?: "Could not send announcement."
                                }
                            },
                        )
                        AppScreen.FullCalendar -> FullCalendarTimetableScreen(
                            timetable = darulUmmahPrayerCalendar2026,
                            status = "Showing the Darul Ummah $DarulUmmahPrayerCalendarYear PDF calendar.",
                            onBack = { screen = AppScreen.Home },
                        )
                    }
                }
                BottomNavigationBar(
                    selected = screen,
                    onSelected = { screen = it },
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
                expandedAnnouncementMediaUrl?.let { mediaUrl ->
                    AnnouncementImageViewer(
                        mediaUrl = mediaUrl,
                        onBack = { expandedAnnouncementMediaUrl = null },
                    )
                }
                expandedHallImageIndex?.let { imageIndex ->
                    HallHireImageViewer(
                        selectedImageIndex = imageIndex,
                        onSelectedImageIndexChange = { expandedHallImageIndex = it },
                        onBack = { expandedHallImageIndex = null },
                    )
                }
            }
        }
        }
    }
}

@Composable
private fun Header(
    onSettingsClick: () -> Unit,
) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(colors.header)
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
private fun BottomNavigationBar(
    selected: AppScreen,
    onSelected: (AppScreen) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    val leftItems = bottomNavItems.take(2)
    val rightItems = bottomNavItems.drop(2)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(112.dp)
            .padding(top = 8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(86.dp)
                .align(Alignment.BottomCenter)
                .shadow(
                    elevation = 18.dp,
                    shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
                    ambientColor = Color.Black.copy(alpha = 0.24f),
                )
                .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                .background(colors.nav)
                .border(
                    width = 1.dp,
                    color = colors.navBorder,
                    shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
                )
                .padding(start = 8.dp, top = 6.dp, end = 8.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                leftItems.forEach { item ->
                    BottomNavigationItem(
                        item = item,
                        selected = selected == item.screen,
                        onClick = { onSelected(item.screen) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Spacer(Modifier.width(82.dp))
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                rightItems.forEach { item ->
                    BottomNavigationItem(
                        item = item,
                        selected = selected == item.screen,
                        onClick = { onSelected(item.screen) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        BottomNavigationItem(
            item = donateNavItem,
            selected = selected == donateNavItem.screen,
            onClick = { onSelected(donateNavItem.screen) },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .width(82.dp),
        )
    }
}

@Composable
private fun BottomNavigationItem(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    val activeColor = if (colors == DarkAppColors) Color.White else Green900
    val inactiveColor = colors.muted.copy(alpha = 0.78f)
    val contentColor = if (selected) activeColor else inactiveColor
    TextButton(
        onClick = onClick,
        modifier = modifier.height(if (item.prominent) 100.dp else 74.dp),
        contentPadding = PaddingValues(0.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = if (item.prominent) {
                    Modifier
                        .size(62.dp)
                        .shadow(14.dp, CircleShape, ambientColor = Green900.copy(alpha = 0.38f))
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Green700, Green900),
                            ),
                        )
                        .border(2.dp, Gold.copy(alpha = 0.72f), CircleShape)
                } else {
                    Modifier
                        .size(if (selected) 40.dp else 36.dp)
                        .clip(CircleShape)
                        .background(if (selected) colors.selected else Color.Transparent)
                },
                contentAlignment = Alignment.Center,
            ) {
                BottomNavIcon(
                    icon = item.icon,
                    color = if (item.prominent) Color.White else contentColor,
                    modifier = Modifier.size(if (item.prominent) 32.dp else 24.dp),
                )
            }
            Text(
                text = item.label,
                color = if (item.prominent) {
                    if (selected && colors != DarkAppColors) Green900 else activeColor
                } else {
                    contentColor
                },
                fontSize = if (item.prominent) 12.sp else 11.sp,
                fontWeight = if (selected || item.prominent) FontWeight.Bold else FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun BottomNavIcon(
    icon: BottomNavIcon,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 2.2.dp.toPx()
        val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        when (icon) {
            BottomNavIcon.Time -> {
                drawCircle(color = color, radius = size.minDimension * 0.42f, style = stroke)
                drawLine(color, center, Offset(center.x, center.y - size.height * 0.24f), strokeWidth, StrokeCap.Round)
                drawLine(color, center, Offset(center.x + size.width * 0.2f, center.y + size.height * 0.08f), strokeWidth, StrokeCap.Round)
            }
            BottomNavIcon.Events -> {
                val left = size.width * 0.24f
                val right = size.width * 0.76f
                drawLine(color, Offset(left, size.height * 0.22f), Offset(right, size.height * 0.22f), strokeWidth, StrokeCap.Round)
                drawLine(color, Offset(left, size.height * 0.4f), Offset(right, size.height * 0.4f), strokeWidth, StrokeCap.Round)
                drawLine(color, Offset(left, size.height * 0.58f), Offset(right * 0.88f, size.height * 0.58f), strokeWidth, StrokeCap.Round)
                drawCircle(color = color, radius = 2.4.dp.toPx(), center = Offset(size.width * 0.17f, size.height * 0.22f))
                drawCircle(color = color, radius = 2.4.dp.toPx(), center = Offset(size.width * 0.17f, size.height * 0.4f))
                drawCircle(color = color, radius = 2.4.dp.toPx(), center = Offset(size.width * 0.17f, size.height * 0.58f))
            }
            BottomNavIcon.Donate -> {
                drawLine(color, Offset(size.width * 0.18f, size.height * 0.6f), Offset(size.width * 0.42f, size.height * 0.76f), 4.dp.toPx(), StrokeCap.Round)
                drawLine(color, Offset(size.width * 0.42f, size.height * 0.76f), Offset(size.width * 0.78f, size.height * 0.62f), 4.dp.toPx(), StrokeCap.Round)
                drawLine(color, Offset(size.width * 0.34f, size.height * 0.52f), Offset(size.width * 0.64f, size.height * 0.52f), 4.dp.toPx(), StrokeCap.Round)
                drawCircle(color = color, radius = size.minDimension * 0.18f, center = Offset(size.width * 0.5f, size.height * 0.24f), style = stroke)
            }
            BottomNavIcon.Live -> {
                drawCircle(color = color, radius = size.minDimension * 0.1f, center = center)
                drawCircle(color = color.copy(alpha = 0.4f), radius = size.minDimension * 0.26f, style = stroke)
                drawCircle(color = color.copy(alpha = 0.26f), radius = size.minDimension * 0.42f, style = stroke)
            }
            BottomNavIcon.Compass -> {
                drawCircle(color = color, radius = size.minDimension * 0.4f, style = stroke)
                drawLine(color, Offset(center.x, size.height * 0.18f), Offset(center.x, size.height * 0.08f), 2.dp.toPx(), StrokeCap.Round)
                drawLine(color, Offset(center.x, size.height * 0.82f), Offset(center.x, size.height * 0.92f), 2.dp.toPx(), StrokeCap.Round)
                drawLine(color, Offset(size.width * 0.18f, center.y), Offset(size.width * 0.08f, center.y), 2.dp.toPx(), StrokeCap.Round)
                drawLine(color, Offset(size.width * 0.82f, center.y), Offset(size.width * 0.92f, center.y), 2.dp.toPx(), StrokeCap.Round)
                drawLine(color, center, Offset(center.x + size.width * 0.14f, center.y - size.height * 0.22f), 2.4.dp.toPx(), StrokeCap.Round)
            }
            BottomNavIcon.Hall -> {
                val roofTop = Offset(center.x, size.height * 0.16f)
                val roofLeft = Offset(size.width * 0.18f, size.height * 0.42f)
                val roofRight = Offset(size.width * 0.82f, size.height * 0.42f)
                drawLine(color, roofLeft, roofTop, strokeWidth, StrokeCap.Round)
                drawLine(color, roofTop, roofRight, strokeWidth, StrokeCap.Round)
                drawLine(color, Offset(size.width * 0.24f, size.height * 0.42f), Offset(size.width * 0.24f, size.height * 0.82f), strokeWidth, StrokeCap.Round)
                drawLine(color, Offset(size.width * 0.76f, size.height * 0.42f), Offset(size.width * 0.76f, size.height * 0.82f), strokeWidth, StrokeCap.Round)
                drawLine(color, Offset(size.width * 0.16f, size.height * 0.82f), Offset(size.width * 0.84f, size.height * 0.82f), strokeWidth, StrokeCap.Round)
                drawLine(color, Offset(size.width * 0.42f, size.height * 0.82f), Offset(size.width * 0.42f, size.height * 0.58f), strokeWidth, StrokeCap.Round)
                drawLine(color, Offset(size.width * 0.58f, size.height * 0.82f), Offset(size.width * 0.58f, size.height * 0.58f), strokeWidth, StrokeCap.Round)
                drawLine(color, Offset(size.width * 0.42f, size.height * 0.58f), Offset(size.width * 0.58f, size.height * 0.58f), strokeWidth, StrokeCap.Round)
            }
        }
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
    onFullCalendarClick: () -> Unit,
) {
    val currentPrayer = currentPrayer(prayerTimetable.dailyPrayerTimes, minuteOfDay)
    val upcomingPrayer = upcomingPrayer(
        timetable = prayerTimetable,
        secondOfDay = secondOfDay,
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
        HomeCalendarCard(onFullCalendarClick)
        ContactCard(mosqueContact)
        RemoteUpdateCard(
            status = updateStatus,
            onRefresh = onRefresh,
        )
    }
}

@Composable
private fun HomeCalendarCard(
    onFullCalendarClick: () -> Unit,
) {
    val colors = LocalAppColors.current
    InfoCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionTitle("2026 PDF calendar")
            Text(
                text = "The home times and full calendar use the Darul Ummah PDF calendar. Asr begins uses Mithl 2.",
                color = colors.muted,
                fontSize = 13.sp,
            )
            Button(onClick = onFullCalendarClick) {
                Text("Full calendar timetable")
            }
        }
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
                text = "Jama'ah at ${formatPrayerDisplayTime(upcomingPrayer.jamaahTime)}",
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
                    text = "Khutbah starts at ${formatPrayerDisplayTime(jumahTime.khutbahTime)}",
                    color = Color.White,
                    fontSize = 14.sp,
                )
            }
            Text(
                text = formatPrayerDisplayTime(jumahTime.salaatTime),
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
    val colors = LocalAppColors.current
    InfoCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Today's mosque prayer times",
                color = colors.text,
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
    val colors = LocalAppColors.current
    val background = if (isCurrent) colors.selected else Color.Transparent
    val border = if (isCurrent) BorderStroke(1.dp, Green700) else BorderStroke(1.dp, colors.border)

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
                color = colors.text,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Begins ${formatPrayerDisplayTime(prayer.beginsTime)}",
                color = colors.muted,
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
                color = colors.muted,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.End,
            )
            Text(
                text = formatPrayerDisplayTime(prayer.jamaahTime),
                color = if (isCurrent && colors != DarkAppColors) Green900 else colors.text,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
            )
        }
    }
}

@Composable
private fun YouTubeScreen() {
    val colors = LocalAppColors.current
    var recentVideos by remember { mutableStateOf<List<YouTubeVideo>>(emptyList()) }
    var recentVideosStatus by remember { mutableStateOf("Loading recent videos...") }
    var selectedVideoId by remember { mutableStateOf<String?>(null) }
    var liveVideoId by remember { mutableStateOf<String?>(null) }
    var liveStatus by remember { mutableStateOf("Checking live stream...") }
    var refreshKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(refreshKey) {
        liveStatus = "Checking live stream..."
        liveVideoId = runCatching {
            parseYouTubeLiveVideoId(fetchDarulUmmahYouTubeLivePageHtml())
        }.getOrNull()
        liveStatus = if (liveVideoId != null) {
            "Darul Ummah TV is live now."
        } else {
            "The live player shows the channel stream when the mosque is live."
        }
    }

    LaunchedEffect(refreshKey) {
        recentVideosStatus = "Loading recent videos..."
        runCatching {
            fetchDarulUmmahRecentVideos().take(4)
        }.onSuccess { videos ->
            recentVideos = videos
            selectedVideoId = videos.firstOrNull()?.id
            recentVideosStatus = if (videos.isEmpty()) {
                "No recent videos were found."
            } else {
                "Latest videos from Darul Ummah TV"
            }
        }.onFailure {
            recentVideos = emptyList()
            selectedVideoId = null
            recentVideosStatus = "Could not load recent videos."
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        InfoCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle("YouTube")
                Text(
                    text = "Live stream from Darul Ummah TV",
                    color = colors.muted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = liveStatus,
                    color = colors.muted,
                    fontSize = 12.sp,
                )
                val livePlayerModifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(8.dp))
                liveVideoId?.let { videoId ->
                    YouTubeVideoPlayer(
                        videoId = videoId,
                        modifier = livePlayerModifier,
                    )
                } ?: YouTubeLivePlayer(
                    channelId = DarulUmmahYouTubeChannelId,
                    modifier = livePlayerModifier,
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
                    color = if (recentVideos.isEmpty()) colors.muted else Green700,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                selectedVideoId?.let { videoId ->
                    key(videoId) {
                        YouTubeVideoPlayer(
                            videoId = videoId,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(230.dp)
                                .clip(RoundedCornerShape(8.dp)),
                        )
                    }
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
private fun DonateScreen() {
    val colors = LocalAppColors.current
    InfoCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionTitle("Donate")
            Text(
                text = "Support Darul Ummah Shadwell with a one-off or regular donation.",
                color = colors.muted,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Complete your donation securely on SumUp's payment page.",
                color = colors.muted,
                fontSize = 12.sp,
            )
            Button(
                onClick = { openExternalUrl(DarulUmmahDonationUrl) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Donate securely with SumUp")
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
    val colors = LocalAppColors.current
    val background = if (selected) colors.selected else Color.Transparent
    val border = if (selected) BorderStroke(1.dp, Green700) else BorderStroke(1.dp, colors.border)
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
                color = colors.text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
            if (video.publishedDate.isNotBlank()) {
                Text(
                    text = video.publishedDate,
                    color = colors.muted,
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
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = colors.text, fontWeight = FontWeight.SemiBold)
            Text(detail, color = colors.muted, fontSize = 13.sp)
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
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .background(colors.cardAlt)
            .border(1.dp, colors.border, RoundedCornerShape(10.dp))
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
            color = colors.text,
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
    val colors = LocalAppColors.current
    InfoCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                SectionTitle("Calendar source")
                Text(
                    text = status,
                    color = colors.muted,
                    fontSize = 13.sp,
                )
            }
            Button(onClick = onRefresh) {
                Text("Reload")
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    darkModeEnabled: Boolean,
    onDarkModeEnabledChanged: (Boolean) -> Unit,
    notificationPreferences: NotificationPreferences,
    onNotificationPreferencesChanged: (NotificationPreferences) -> Unit,
    submitStatus: String?,
    onSubmitAnnouncement: suspend (AnnouncementDraft, String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AppearanceSettings(
            darkModeEnabled = darkModeEnabled,
            onDarkModeEnabledChanged = onDarkModeEnabledChanged,
        )
        NotificationSettings(
            preferences = notificationPreferences,
            onPreferencesChanged = onNotificationPreferencesChanged,
        )
        PrivacyPolicySettings()
        AddAnnouncementCard(
            submitStatus = submitStatus,
            onSubmitAnnouncement = onSubmitAnnouncement,
        )
    }
}

@Composable
private fun PrivacyPolicySettings() {
    InfoCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionTitle("Privacy")
            Text(
                text = "Read how Darul Ummah Shadwell uses app data, location, notifications, and announcement media.",
                color = LocalAppColors.current.muted,
                fontSize = 13.sp,
            )
            TextButton(onClick = { openExternalUrl(PrivacyPolicyUrl) }) {
                Text("Open privacy policy")
            }
        }
    }
}

@Composable
private fun AppearanceSettings(
    darkModeEnabled: Boolean,
    onDarkModeEnabledChanged: (Boolean) -> Unit,
) {
    val colors = LocalAppColors.current
    InfoCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionTitle("Appearance")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.cardAlt)
                    .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Dark mode", color = colors.text, fontWeight = FontWeight.SemiBold)
                    Text(
                        text = "Uses your phone setting on first launch, then remembers this switch.",
                        color = colors.muted,
                        fontSize = 13.sp,
                    )
                }
                Switch(
                    checked = darkModeEnabled,
                    onCheckedChange = onDarkModeEnabledChanged,
                )
            }
        }
    }
}

@Composable
private fun HallHireScreen(
    onImageClick: (Int) -> Unit,
) {
    val colors = LocalAppColors.current
    var selectedImageIndex by rememberSaveable { mutableIntStateOf(0) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Green900, Green700),
                        ),
                    )
                    .padding(16.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Hall Hire",
                        color = Color.White,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black,
                    )
                    Text(
                        text = "A flexible community hall for birthdays, meetings, conferences, mehndi and more.",
                        color = Color.White.copy(alpha = 0.88f),
                        fontSize = 15.sp,
                        lineHeight = 21.sp,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        HallHireSummaryPill("110", "capacity", Modifier.weight(1f))
                        HallHireSummaryPill("Call", "for timings", Modifier.weight(1f))
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = colors.card),
            border = BorderStroke(1.dp, colors.border),
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = "Inside the hall",
                    color = colors.text,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 3f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.cardAlt)
                        .border(1.dp, colors.border, RoundedCornerShape(14.dp))
                        .clickable { onImageClick(selectedImageIndex) },
                ) {
                    Image(
                        painter = painterResource(hallHireImages[selectedImageIndex]),
                        contentDescription = "Inside Darul Ummah hall",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                    Text(
                        text = "Tap to enlarge",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.58f))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                    Text(
                        text = "${selectedImageIndex + 1} / ${hallHireImages.size}",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.58f))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    hallHireImages.forEachIndexed { index, image ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1.35f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.cardAlt)
                                .border(
                                    width = if (index == selectedImageIndex) 3.dp else 1.dp,
                                    color = if (index == selectedImageIndex) Green700 else colors.border,
                                    shape = RoundedCornerShape(10.dp),
                                )
                                .clickable { selectedImageIndex = index },
                        ) {
                            Image(
                                painter = painterResource(image),
                                contentDescription = "Hall photo ${index + 1}",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                            )
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = colors.card),
            border = BorderStroke(1.dp, colors.border),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Booking details",
                    color = colors.text,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
                HallHireDetail(label = "Capacity", value = "110 people")
                HallHireDetail(label = "Suitable for", value = "Birthdays, meetings, conferences, mehndi and more")
                HallHireDetail(label = "Dates and timings", value = "Call Brother Talha Noor")
                Text(
                    text = "07886663213",
                    color = if (colors == DarkAppColors) Green500 else Green700,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                )
            }
        }
    }
}

@Composable
private fun HallHireSummaryPill(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.13f))
            .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Text(
            text = value,
            color = Gold,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
        )
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.88f),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun HallHireDetail(
    label: String,
    value: String,
) {
    val colors = LocalAppColors.current
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            text = label,
            color = colors.muted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = value,
            color = colors.text,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 21.sp,
        )
    }
}

@Composable
private fun QiblaCompassScreen() {
    val qiblaCompassController = remember { createQiblaCompassController() }
    val qiblaState by qiblaCompassController.state.collectAsState()

    DisposableEffect(qiblaCompassController) {
        qiblaCompassController.start()
        onDispose {
            qiblaCompassController.stop()
        }
    }

    QiblaCompassCard(state = qiblaState)
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
                            qiblaBearingDegrees = state.qiblaBearingDegrees,
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
    qiblaBearingDegrees: Double?,
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
            rotate(degrees = compassRotation) {
                qiblaBearingDegrees?.let { bearing ->
                    rotate(degrees = bearing.toFloat()) {
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
private fun HallHireImageViewer(
    selectedImageIndex: Int,
    onSelectedImageIndexChange: (Int) -> Unit,
    onBack: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.94f))
            .safeContentPadding()
            .padding(16.dp),
    ) {
        TextButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.14f)),
        ) {
            Text(
                text = "Back",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Text(
            text = "${selectedImageIndex + 1} / ${hallHireImages.size}",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.14f))
                .padding(horizontal = 12.dp, vertical = 9.dp),
        )
        Image(
            painter = painterResource(hallHireImages[selectedImageIndex]),
            contentDescription = "Expanded hall photo",
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 58.dp, bottom = 78.dp),
            contentScale = ContentScale.Fit,
        )
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TextButton(
                onClick = {
                    onSelectedImageIndexChange(
                        if (selectedImageIndex == 0) hallHireImages.lastIndex else selectedImageIndex - 1,
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.14f)),
            ) {
                Text("Previous", color = Color.White, fontWeight = FontWeight.Bold)
            }
            TextButton(
                onClick = {
                    onSelectedImageIndexChange(
                        if (selectedImageIndex == hallHireImages.lastIndex) 0 else selectedImageIndex + 1,
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.14f)),
            ) {
                Text("Next", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun FullCalendarTimetableScreen(
    timetable: List<CalendarPrayerTime>,
    status: String,
    onBack: () -> Unit,
) {
    val colors = LocalAppColors.current
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
                    color = colors.muted,
                    fontSize = 13.sp,
                )
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
                        color = colors.muted,
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
    val colors = LocalAppColors.current
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
                    color = colors.text,
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
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                color = colors.muted,
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
    val colors = LocalAppColors.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .then(if (day == null) Modifier else Modifier.clickable { onClick(day) })
            .padding(vertical = 5.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (day == null) return@Box
        val background = if (selected) colors.selected else Color.Transparent
        Text(
            text = day.date.dayOfMonth.toString(),
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(17.dp))
                .background(background)
                .padding(top = 7.dp),
            color = if (selected && colors != DarkAppColors) Green900 else colors.text,
            fontSize = 15.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun CalendarSelectedDayTimes(day: CalendarDay) {
    val colors = LocalAppColors.current
    InfoCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = day.prayerTime.date,
                color = colors.text,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Prayer",
                    color = colors.muted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(22.dp)) {
                    Text(
                        text = "Starts",
                        color = colors.muted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Jama'ah",
                        color = colors.muted,
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
    val colors = LocalAppColors.current
    val timeColor = if (colors == DarkAppColors) Green500 else Green900
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colors.border, RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = colors.text,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Text(
                text = starts,
                color = timeColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
            )
            Text(
                text = jamaah,
                color = timeColor,
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
    deleteStatus: String?,
    onAnnouncementMediaClick: (String) -> Unit,
    onDeleteAnnouncement: suspend (Announcement, String) -> Unit,
) {
    val colors = LocalAppColors.current
    val today = currentDateTimeComponents()
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        InfoCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle("Class schedule")
                classSchedule.forEach { session ->
                    ScheduleRow(
                        title = session.title,
                        meta = "${session.day} - ${classSessionDisplayTime(session, today)}",
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
                        color = colors.muted,
                        fontSize = 14.sp,
                    )
                } else {
                    DeleteAnnouncementControls(
                        announcements = announcements,
                        deleteStatus = deleteStatus,
                        onAnnouncementMediaClick = onAnnouncementMediaClick,
                        onDeleteAnnouncement = onDeleteAnnouncement,
                    )
                }
            }
        }
        InfoCard {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionTitle("Custom alerts")
                Text(
                    text = "Class reminders are set for 1 hour before each listed class.",
                    color = colors.muted,
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
    onAnnouncementMediaClick: (String) -> Unit,
    onDeleteAnnouncement: suspend (Announcement, String) -> Unit,
) {
    val colors = LocalAppColors.current
    val scope = rememberCoroutineScope()
    var adminPassword by remember { mutableStateOf("") }
    var pendingDeleteId by remember { mutableStateOf<String?>(null) }
    var localError by remember { mutableStateOf<String?>(null) }

    Text(
        text = "Enter the admin password below to enable manual deletion before expiry.",
        color = colors.muted,
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
            color = if (it.contains("deleted", ignoreCase = true)) Green700 else colors.muted,
            fontSize = 13.sp,
        )
    }
    announcements.forEach { announcement ->
        AnnouncementRow(
            announcement = announcement,
            isDeleting = pendingDeleteId == announcement.id,
            onMediaClick = onAnnouncementMediaClick,
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
    val colors = LocalAppColors.current
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
                color = colors.muted,
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
                        localError = "Fill in title, description, valid start date/time, and valid expiry date/time."
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
    onMediaClick: (String) -> Unit,
    onDelete: (() -> Unit)? = null,
) {
    val mediaUrl = announcement.mediaUrl?.takeIf { it.isNotBlank() }
    ScheduleRow(
        title = announcement.title,
        meta = "Starts ${announcement.startDate} at ${announcement.startTime}",
        detail = announcement.description,
        mediaUrl = mediaUrl,
        onMediaClick = onMediaClick,
        footer = buildList {
            add("Expires ${announcement.eventDate} at ${announcement.eventTime}")
            if (mediaUrl != null) add("Photo attached")
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
    mediaUrl: String? = null,
    onMediaClick: ((String) -> Unit)? = null,
    footer: String? = null,
    action: (@Composable () -> Unit)? = null,
) {
    val colors = LocalAppColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(colors.cardAlt)
            .border(1.dp, colors.border, RoundedCornerShape(8.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = title,
            color = colors.text,
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
            color = colors.muted,
            fontSize = 13.sp,
        )
        mediaUrl?.let {
            AnnouncementPoster(
                mediaUrl = it,
                onClick = onMediaClick?.let { click -> { click(it) } },
                modifier = Modifier.padding(top = 6.dp),
            )
        }
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
private fun AnnouncementPoster(
    mediaUrl: String,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    var imageBitmap by remember(mediaUrl) { mutableStateOf<ImageBitmap?>(null) }
    var loadFailed by remember(mediaUrl) { mutableStateOf(false) }

    LaunchedEffect(mediaUrl) {
        imageBitmap = null
        loadFailed = false
        runCatching {
            decodeAnnouncementImageBitmap(loadAnnouncementMedia(mediaUrl))
        }.onSuccess { decodedImage ->
            if (decodedImage == null) {
                loadFailed = true
            } else {
                imageBitmap = decodedImage
            }
        }.onFailure {
            loadFailed = true
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(4f / 3f)
            .clip(RoundedCornerShape(8.dp))
            .background(LocalAppColors.current.selected)
            .border(1.dp, LocalAppColors.current.border, RoundedCornerShape(8.dp))
            .then(if (onClick == null) Modifier else Modifier.clickable(onClick = onClick)),
        contentAlignment = Alignment.Center,
    ) {
        when {
            imageBitmap != null -> Image(
                bitmap = imageBitmap!!,
                contentDescription = "Announcement poster",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            loadFailed -> Text(
                text = "Poster unavailable",
                color = LocalAppColors.current.muted,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
            else -> Text(
                text = "Loading poster...",
                color = LocalAppColors.current.muted,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun AnnouncementImageViewer(
    mediaUrl: String,
    onBack: () -> Unit,
) {
    var imageBitmap by remember(mediaUrl) { mutableStateOf<ImageBitmap?>(null) }
    var loadFailed by remember(mediaUrl) { mutableStateOf(false) }

    LaunchedEffect(mediaUrl) {
        imageBitmap = null
        loadFailed = false
        runCatching {
            decodeAnnouncementImageBitmap(loadAnnouncementMedia(mediaUrl))
        }.onSuccess { decodedImage ->
            if (decodedImage == null) {
                loadFailed = true
            } else {
                imageBitmap = decodedImage
            }
        }.onFailure {
            loadFailed = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.92f))
            .safeContentPadding()
            .padding(16.dp),
    ) {
        TextButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.14f)),
        ) {
            Text(
                text = "Back",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 54.dp),
            contentAlignment = Alignment.Center,
        ) {
            when {
                imageBitmap != null -> Image(
                    bitmap = imageBitmap!!,
                    contentDescription = "Expanded announcement poster",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
                loadFailed -> Text(
                    text = "Poster unavailable",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                else -> Text(
                    text = "Loading poster...",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun announcementFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Green700,
    unfocusedBorderColor = LocalAppColors.current.border,
    focusedLabelColor = Green700,
    unfocusedLabelColor = LocalAppColors.current.muted,
    cursorColor = Green700,
    focusedTextColor = LocalAppColors.current.text,
    unfocusedTextColor = LocalAppColors.current.text,
    focusedContainerColor = LocalAppColors.current.field,
    unfocusedContainerColor = LocalAppColors.current.field,
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
    val normalizedStartDate = normalizeAnnouncementDate(startDate)
    val normalizedStartTime = normalizeAnnouncementTime(startTime)
    val normalizedExpiryDate = normalizeAnnouncementDate(eventDate)
    val normalizedExpiryTime = normalizeAnnouncementTime(eventTime)
    if (
        trimmedTitle.isBlank() ||
        trimmedDescription.isBlank() ||
        normalizedStartDate == null ||
        normalizedStartTime == null ||
        normalizedExpiryDate == null ||
        normalizedExpiryTime == null
    ) return null
    return AnnouncementDraft(
        title = trimmedTitle,
        description = trimmedDescription,
        startDate = normalizedStartDate,
        startTime = normalizedStartTime,
        eventDate = normalizedExpiryDate,
        eventTime = normalizedExpiryTime,
        mediaBase64 = selectedImage?.let { encodeAnnouncementImageBase64(it.bytes) },
        mediaMimeType = selectedImage?.mimeType,
        mediaFileName = selectedImage?.fileName,
    )
}

private fun normalizeAnnouncementDate(value: String): String? {
    val trimmed = value.trim()
    val slashOrDashMatch = Regex("""^(\d{1,2})[/-](\d{1,2})[/-](\d{4})$""").matchEntire(trimmed)
    val isoMatch = Regex("""^(\d{4})-(\d{1,2})-(\d{1,2})$""").matchEntire(trimmed)
    val (day, month, year) = when {
        slashOrDashMatch != null -> {
            val (_, dayText, monthText, yearText) = slashOrDashMatch.groupValues
            Triple(
                dayText.toIntOrNull() ?: return null,
                monthText.toIntOrNull() ?: return null,
                yearText.toIntOrNull() ?: return null,
            )
        }
        isoMatch != null -> {
            val (_, yearText, monthText, dayText) = isoMatch.groupValues
            Triple(
                dayText.toIntOrNull() ?: return null,
                monthText.toIntOrNull() ?: return null,
                yearText.toIntOrNull() ?: return null,
            )
        }
        else -> return null
    }

    if (!isValidAnnouncementDate(day, month, year)) return null
    return "${day.toString().padStart(2, '0')}/${month.toString().padStart(2, '0')}/${
        year.toString().padStart(4, '0')
    }"
}

private fun normalizeAnnouncementTime(value: String): String? {
    val match = Regex("""^(\d{1,2}):(\d{2})$""").matchEntire(value.trim()) ?: return null
    val hour = match.groupValues[1].toIntOrNull() ?: return null
    val minute = match.groupValues[2].toIntOrNull() ?: return null
    if (hour !in 0..23 || minute !in 0..59) return null
    return "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
}

private fun isValidAnnouncementDate(day: Int, month: Int, year: Int): Boolean {
    if (year !in 2000..2099 || month !in 1..12) return false
    val daysInMonth = when (month) {
        4, 6, 9, 11 -> 30
        2 -> if (isLeapYear(year)) 29 else 28
        else -> 31
    }
    return day in 1..daysInMonth
}

private fun isLeapYear(year: Int): Boolean {
    return year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)
}


@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = LocalAppColors.current.text,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun InfoCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = LocalAppColors.current.card),
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
    secondOfDay: Int,
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
    return candidates.firstOrNull { secondOfDay < it.minuteOfDay * 60 } ?: candidates.first()
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
    if (times.size < 6) return null

    val prayerTimes = when {
        times.size >= 11 -> CalendarPrayerValues(
            fajrBegins = normalizeClockTime(times[0], ExpectedClockPeriod.Morning),
            sunrise = normalizeClockTime(times[1], ExpectedClockPeriod.Morning),
            fajrJamaah = normalizeClockTime(times[2], ExpectedClockPeriod.Morning),
            dhuhrBegins = normalizeClockTime(times[3], ExpectedClockPeriod.Afternoon),
            dhuhrJamaah = normalizeClockTime(times[4], ExpectedClockPeriod.Afternoon),
            asrBegins = normalizeClockTime(times[5], ExpectedClockPeriod.Afternoon),
            asrJamaah = normalizeClockTime(times[6], ExpectedClockPeriod.Afternoon),
            maghribBegins = normalizeClockTime(times[7], ExpectedClockPeriod.Afternoon),
            maghribJamaah = normalizeClockTime(times[8], ExpectedClockPeriod.Afternoon),
            ishaBegins = normalizeClockTime(times[9], ExpectedClockPeriod.Afternoon),
            ishaJamaah = normalizeClockTime(times[10], ExpectedClockPeriod.Afternoon),
        )
        else -> CalendarPrayerValues(
            fajrBegins = normalizeClockTime(times[0], ExpectedClockPeriod.Morning),
            fajrJamaah = normalizeClockTime(times[0], ExpectedClockPeriod.Morning),
            sunrise = normalizeClockTime(times[1], ExpectedClockPeriod.Morning),
            dhuhrBegins = normalizeClockTime(times[2], ExpectedClockPeriod.Afternoon),
            dhuhrJamaah = normalizeClockTime(times[2], ExpectedClockPeriod.Afternoon),
            asrBegins = normalizeClockTime(times[3], ExpectedClockPeriod.Afternoon),
            asrJamaah = normalizeClockTime(times[3], ExpectedClockPeriod.Afternoon),
            maghribBegins = normalizeClockTime(times[4], ExpectedClockPeriod.Afternoon),
            maghribJamaah = normalizeClockTime(times[4], ExpectedClockPeriod.Afternoon),
            ishaBegins = normalizeClockTime(times[5], ExpectedClockPeriod.Afternoon),
            ishaJamaah = normalizeClockTime(times[5], ExpectedClockPeriod.Afternoon),
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
    val rowPeriods = listOf(
        ExpectedClockPeriod.Morning,
        ExpectedClockPeriod.Afternoon,
        ExpectedClockPeriod.Afternoon,
        ExpectedClockPeriod.Afternoon,
        ExpectedClockPeriod.Afternoon,
        ExpectedClockPeriod.Afternoon,
    )
    val beginsTimes = parseTimetableRow(rows.firstOrNull { it.contains("BEGINS") }, rowPeriods)
    val jamaahTimes = parseTimetableRow(rows.firstOrNull { it.contains("JAMA") }, rowPeriods)
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

    val fajrBegins = parseMeridiemTime(pairs["FAJR BEGINS"] ?: return null, ExpectedClockPeriod.Morning)
    val fajrJamaah = parseMeridiemTime(pairs["FAJR JAMA'AH"] ?: return null, ExpectedClockPeriod.Morning)
    val zuhrBegins = parseMeridiemTime(pairs["ZUHR BEGINS"] ?: return null, ExpectedClockPeriod.Afternoon)
    val zuhrJamaah = parseMeridiemTime(pairs["ZUHR JAMA'AH"] ?: return null, ExpectedClockPeriod.Afternoon)
    val asrBegins = parseMeridiemTime(pairs["ASR BEGINS"] ?: return null, ExpectedClockPeriod.Afternoon)
    val asrJamaah = parseMeridiemTime(pairs["ASR JAMA'AH"] ?: return null, ExpectedClockPeriod.Afternoon)
    val maghribBegins = parseMeridiemTime(pairs["MAGHRIB BEGINS"] ?: return null, ExpectedClockPeriod.Afternoon)
    val maghribJamaah = parseMeridiemTime(pairs["MAGHRIB JAMA'AH"] ?: return null, ExpectedClockPeriod.Afternoon)
    val ishaBegins = parseMeridiemTime(pairs["ISHA BEGINS"] ?: return null, ExpectedClockPeriod.Afternoon)
    val ishaJamaah = parseMeridiemTime(pairs["ISHA JAMA'AH"] ?: return null, ExpectedClockPeriod.Afternoon)

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

private enum class ExpectedClockPeriod {
    Morning,
    Afternoon,
}

private fun parseTimetableRow(
    row: String?,
    expectedPeriods: List<ExpectedClockPeriod?> = emptyList(),
): List<ParsedTime> {
    require(!row.isNullOrBlank()) { "Darul Ummah timetable row was not found" }
    return Regex("(\\d{1,2}:\\d{2})<span[^>]*>\\s*(AM|PM)\\s*</span>", RegexOption.IGNORE_CASE)
        .findAll(row)
        .mapIndexed { index, match ->
            val time = match.groupValues[1]
            val period = match.groupValues[2]
            val minuteOfDay = normalizeMinuteOfDay(
                minuteOfDay = toMinuteOfDay(time, period),
                expectedPeriod = expectedPeriods.getOrNull(index),
            )
            ParsedTime(formatTime(minuteOfDay), minuteOfDay)
        }
        .toList()
}

private fun parseMeridiemTime(
    value: String,
    expectedPeriod: ExpectedClockPeriod? = null,
): ParsedTime {
    val match = meridiemTimeRegex.find(value)
        ?: error("Could not parse time: $value")
    val minuteOfDay = normalizeMinuteOfDay(
        minuteOfDay = toMinuteOfDay(match.groupValues[1], match.groupValues[2]),
        expectedPeriod = expectedPeriod,
    )
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

private suspend fun fetchDarulUmmahRecentVideos(): List<YouTubeVideo> {
    repeat(2) { attempt ->
        runCatching {
            parseYouTubeRecentVideos(fetchDarulUmmahYouTubeFeedXml())
        }.getOrNull()?.takeIf { it.isNotEmpty() }?.let { return it }

        runCatching {
            parseYouTubeRecentVideosPage(fetchDarulUmmahYouTubeVideosPageHtml())
        }.getOrNull()?.takeIf { it.isNotEmpty() }?.let { return it }

        if (attempt == 0) delay(750)
    }

    return parseYouTubeRecentVideosPage(fetchDarulUmmahYouTubeVideosPageHtml())
}

internal fun parseYouTubeLiveVideoId(html: String): String? {
    Regex("<link rel=[\"']canonical[\"'] href=[\"']https://www\\.youtube\\.com/watch\\?v=([^\"'&]+)[\"']")
        .find(html)
        ?.groupValues
        ?.getOrNull(1)
        ?.let { return it }

    val liveVideoDetails = Regex(
        "\"videoDetails\":\\{\"videoId\":\"([^\"]+)\"[\\s\\S]*?\"isLiveContent\":true",
    ).find(html)

    return liveVideoDetails
        ?.groupValues
        ?.getOrNull(1)
        ?: liveVideoIdFromRenderer(html)
}

private fun liveVideoIdFromRenderer(html: String): String? {
    val rendererRegex = Regex(
        "\"videoRenderer\":\\{([\\s\\S]*?)(?=\"videoRenderer\":|\"playlistRenderer\":|\"channelRenderer\":|</script>)",
    )
    return rendererRegex.findAll(html)
        .firstNotNullOfOrNull { match ->
            val renderer = match.groupValues[1]
            val hasLiveBadge = renderer.contains("\"style\":\"LIVE\"") ||
                renderer.contains("\"isLiveNow\":true") ||
                renderer.contains("\"text\":\"LIVE\"")
            if (!hasLiveBadge) return@firstNotNullOfOrNull null
            Regex("\"videoId\":\"([^\"]+)\"")
                .find(renderer)
                ?.groupValues
                ?.getOrNull(1)
        }
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

internal fun parseYouTubeRecentVideosPage(html: String): List<YouTubeVideo> {
    val videos = mutableListOf<YouTubeVideo>()
    val seenVideoIds = mutableSetOf<String>()
    val itemRegex = Regex(
        "\"richItemRenderer\":\\{([\\s\\S]*?)(?=\"richItemRenderer\":|\"continuationItemRenderer\":|</script>)",
    )

    itemRegex.findAll(html).forEach { match ->
        val item = match.groupValues[1]
        val videoId = Regex("\"videoId\":\"([^\"]+)\"")
            .find(item)
            ?.groupValues
            ?.getOrNull(1)
            ?: Regex("/vi/([^/]+)/")
                .find(item)
                ?.groupValues
                ?.getOrNull(1)
            ?: return@forEach
        if (!seenVideoIds.add(videoId)) return@forEach

        val title = Regex("\"lockupMetadataViewModel\":\\{\"title\":\\{\"content\":\"([^\"]+)\"")
            .find(item)
            ?.groupValues
            ?.getOrNull(1)
            ?.let(::decodeJsonText)
            ?.let(::decodeXmlText)
            ?.takeIf { it.isNotBlank() }
            ?: return@forEach
        val publishedDate = Regex("\"content\":\"([^\"]+ ago)\"")
            .find(item)
            ?.groupValues
            ?.getOrNull(1)
            ?.let(::decodeJsonText)
            .orEmpty()

        videos += YouTubeVideo(
            id = videoId,
            title = title,
            publishedDate = publishedDate,
        )
    }

    return videos
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

private fun decodeJsonText(value: String): String {
    return value
        .replace("\\u0026", "&")
        .replace("\\\"", "\"")
        .replace("\\/", "/")
        .replace("\\n", " ")
        .replace(Regex("\\s+"), " ")
        .trim()
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

private fun normalizeClockTime(
    value: String,
    expectedPeriod: ExpectedClockPeriod? = null,
): String {
    val parts = value.split(":")
    val minuteOfDay = normalizeMinuteOfDay(
        minuteOfDay = parts[0].toInt() * 60 + parts[1].toInt(),
        expectedPeriod = expectedPeriod,
    )
    return formatTime(minuteOfDay)
}

private fun normalizeMinuteOfDay(
    minuteOfDay: Int,
    expectedPeriod: ExpectedClockPeriod?,
): Int {
    return when {
        expectedPeriod == ExpectedClockPeriod.Afternoon && minuteOfDay < 12 * 60 -> minuteOfDay + 12 * 60
        else -> minuteOfDay
    }
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

internal fun formatPrayerDisplayTime(time: String): String {
    val parts = time.split(":")
    if (parts.size != 2) return time

    val hour = parts[0].toIntOrNull() ?: return time
    val minute = parts[1].toIntOrNull() ?: return time
    if (hour !in 0..23 || minute !in 0..59) return time

    val displayHour = when (val hourInPeriod = hour % 12) {
        0 -> 12
        else -> hourInPeriod
    }
    val period = if (hour < 12) "AM" else "PM"
    return "$displayHour:${minute.toString().padStart(2, '0')} $period"
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
