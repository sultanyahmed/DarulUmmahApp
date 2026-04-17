package com.example.darulummahapp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
)

data class EventAnnouncement(
    val title: String,
    val date: String,
    val detail: String,
)

private enum class AppScreen {
    Home,
    Classes,
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
    classesAndEvents = false,
)

private val classSchedule = listOf(
    ClassSession("Quran Class", "Monday and Wednesday", "18:30", "Children"),
    ClassSession("Tajweed Circle", "Friday", "19:15", "Adults"),
    ClassSession("Sisters Study", "Saturday", "11:00", "Sisters"),
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
                parseDarulUmmahTimetable(fetchDarulUmmahHomeHtml())
            }.onSuccess { timetable ->
                prayerTimetable = timetable
                updateStatus = "Updated from darulummah.org.uk"
            }.onFailure {
                updateStatus = "Could not refresh. Showing the last bundled timetable."
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
                Header()
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
                        notificationPreferences = notificationPreferences,
                        onNotificationPreferencesChanged = { notificationPreferences = it },
                        updateStatus = updateStatus,
                        onRefresh = { refreshKey++ },
                    )
                    AppScreen.Classes -> ClassesAndEventsScreen()
                }
            }
        }
    }
}

@Composable
private fun Header() {
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
        Column {
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
    notificationPreferences: NotificationPreferences,
    onNotificationPreferencesChanged: (NotificationPreferences) -> Unit,
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
        NotificationSettings(
            preferences = notificationPreferences,
            onPreferencesChanged = onNotificationPreferencesChanged,
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
                detail = "Class updates and future announcements",
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
                    text = "Quran class reminders are set for 30 minutes before the class starts.",
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

internal fun parseDarulUmmahTimetable(html: String): PrayerTimetable {
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
