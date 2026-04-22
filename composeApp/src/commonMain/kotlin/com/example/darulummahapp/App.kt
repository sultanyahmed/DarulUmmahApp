package com.example.darulummahapp

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import darulummahapp.composeapp.generated.resources.Res
import darulummahapp.composeapp.generated.resources.darul_ummah_logo
import kotlinx.coroutines.delay
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

data class ClassSession(
    val title: String,
    val day: String,
    val time: String,
    val audience: String,
    val reminderIsoDayOfWeek: Int? = null,
    val reminderMinuteOfDay: Int? = null,
)

data class EventAnnouncement(
    val title: String,
    val date: String,
    val detail: String,
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
    Settings,
    FullCalendar,
}

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

private val eventAnnouncements = emptyList<EventAnnouncement>()

@Composable
@Preview
fun App() {
    MaterialTheme {
        var screen by remember { mutableStateOf(AppScreen.Home) }
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

        LaunchedEffect(Unit) {
            while (true) {
                minuteOfDay = currentMinuteOfDay()
                secondOfDay = currentSecondOfDay()
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
                parseFullCalendarTimetable(fetchDarulUmmahPrayerTimetableHtml())
            }.onSuccess { timetable ->
                calendarTimetable = timetable
                calendarStatus = if (timetable.isEmpty()) {
                    "No timetable rows were found on the Darul Ummah timetable page."
                } else {
                    "Updated from darulummah.org.uk"
                }
            }.onFailure {
                calendarStatus = "Could not load the full calendar timetable."
            }
        }

        LaunchedEffect(notificationPreferences, prayerTimetable, isoDayOfWeek) {
            saveNotificationPreferences(notificationPreferences)
            updateNotificationSchedules(
                preferences = notificationPreferences,
                timetable = prayerTimetable,
                isoDayOfWeek = isoDayOfWeek,
            )
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
                    AppScreen.Classes -> ClassesAndEventsScreen()
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
        Column {
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
        Text(
            text = prayer.jamaahTime,
            color = if (isCurrent) Green900 else Ink,
            fontSize = 18.sp,
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
            ContactRow("Call", contact.phone)
            ContactRow("Email", contact.email)
            ContactRow("Visit", contact.address)
        }
    }
}

@Composable
private fun ContactRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            color = Green900,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )
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
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        NotificationSettings(
            preferences = notificationPreferences,
            onPreferencesChanged = onNotificationPreferencesChanged,
        )
        InfoCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle("Prayer calendar")
                Text(
                    text = "Load the full Darul Ummah prayer timetable for the calendar year.",
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
            var visibleMonthIndex by remember(timetable) {
                mutableIntStateOf(monthIndexes.firstOrNull() ?: 1)
            }
            var selectedDay by remember(timetable) {
                mutableStateOf(allCalendarDays.firstOrNull())
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
private fun ClassesAndEventsScreen() {
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
                if (eventAnnouncements.isEmpty()) {
                    Text(
                        text = "No announcements at the moment.",
                        color = Muted,
                        fontSize = 14.sp,
                    )
                } else {
                    eventAnnouncements.forEach { event ->
                        ScheduleRow(
                            title = event.title,
                            meta = event.date,
                            detail = event.detail,
                        )
                    }
                }
            }
        }
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
private fun ScheduleRow(
    title: String,
    meta: String,
    detail: String,
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
    }
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
    val calendarYear = extractCalendarYear(html)
    return Regex("<tr[\\s\\S]*?</tr>", RegexOption.IGNORE_CASE)
        .findAll(html)
        .mapNotNull { match -> parseCalendarPrayerRow(match.value, calendarYear) }
        .distinctBy { it.date }
        .toList()
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
    parseTodayPrayerTimetableFromCalendar(html)?.let { return it }
    parseCurrentPrayerGrid(html)?.let { return it }

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
