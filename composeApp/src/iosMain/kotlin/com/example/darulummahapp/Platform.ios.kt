package com.example.darulummahapp

import kotlinx.cinterop.BetaInteropApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitHour
import platform.Foundation.NSCalendarUnitMinute
import platform.Foundation.NSCalendarUnitSecond
import platform.Foundation.NSCalendarUnitWeekday
import platform.Foundation.NSData
import platform.Foundation.NSDate
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDefaults
import platform.Foundation.create
import platform.Foundation.stringByAddingPercentEncodingWithAllowedCharacters
import platform.Foundation.URLQueryAllowedCharacterSet
import platform.Foundation.dataWithContentsOfURL
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIAlertAction
import platform.UIKit.UIAlertController
import platform.UIKit.UIAlertControllerStyleActionSheet
import platform.UIKit.UIAlertActionStyleCancel
import platform.UIKit.UIAlertActionStyleDefault
import platform.UIKit.UIApplication
import platform.UIKit.UIDevice
import platform.UIKit.UIViewController
import platform.UIKit.popoverPresentationController
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter

class IOSPlatform : Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

actual fun currentMinuteOfDay(): Int {
    val components = NSCalendar.currentCalendar.components(
        NSCalendarUnitHour or NSCalendarUnitMinute,
        fromDate = NSDate(),
    )
    return components.hour.toInt() * 60 + components.minute.toInt()
}

actual fun currentSecondOfDay(): Int {
    val components = NSCalendar.currentCalendar.components(
        NSCalendarUnitHour or NSCalendarUnitMinute or NSCalendarUnitSecond,
        fromDate = NSDate(),
    )
    return components.hour.toInt() * 60 * 60 +
        components.minute.toInt() * 60 +
        components.second.toInt()
}

actual fun currentIsoDayOfWeek(): Int {
    val components = NSCalendar.currentCalendar.components(
        NSCalendarUnitWeekday,
        fromDate = NSDate(),
    )
    return ((components.weekday.toInt() + 5) % 7) + 1
}

actual fun currentDateTimeComponents(): DateTimeComponents {
    val components = NSCalendar.currentCalendar.components(
        NSCalendarUnitHour or NSCalendarUnitMinute or NSCalendarUnitSecond or
            platform.Foundation.NSCalendarUnitDay or
            platform.Foundation.NSCalendarUnitMonth or
            platform.Foundation.NSCalendarUnitYear,
        fromDate = NSDate(),
    )
    return DateTimeComponents(
        year = components.year.toInt(),
        month = components.month.toInt(),
        day = components.day.toInt(),
        hour = components.hour.toInt(),
        minute = components.minute.toInt(),
        second = components.second.toInt(),
    )
}

actual fun updateNotificationSchedules(
    preferences: NotificationPreferences,
    timetable: PrayerTimetable,
    isoDayOfWeek: Int,
) {
    val notificationCenter = UNUserNotificationCenter.currentNotificationCenter()
    val identifiers = notificationIdentifiers()
    notificationCenter.removePendingNotificationRequestsWithIdentifiers(identifiers)

    if (!preferences.prayerReminders && !preferences.countdownAlerts && !preferences.classesAndEvents) return

    notificationCenter.requestAuthorizationWithOptions(
        options = UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge,
    ) { granted, _ ->
        if (!granted) return@requestAuthorizationWithOptions
        dispatch_async(dispatch_get_main_queue()) {
            scheduleAuthorizedNotifications(
                preferences = preferences,
                timetable = timetable,
                isoDayOfWeek = isoDayOfWeek,
            )
        }
    }
}

private fun scheduleAuthorizedNotifications(
    preferences: NotificationPreferences,
    timetable: PrayerTimetable,
    isoDayOfWeek: Int,
) {
    val prayerEvents = timetable.dailyPrayerTimes.mapIndexed { index, prayer ->
        val isFridayZuhr = isoDayOfWeek == 5 && prayer.name == "Zuhr"
        val name = if (isFridayZuhr) "Jum'ah" else prayer.name
        val time = if (isFridayZuhr) timetable.jumahTime.salaatTime else prayer.jamaahTime
        val minuteOfDay = if (isFridayZuhr) timeToMinuteOfDay(time) else prayer.minuteOfDay
        ScheduledPrayer(index, name, time, minuteOfDay)
    }

    if (preferences.prayerReminders) {
        prayerEvents.forEach { prayer ->
            PRAYER_ALERT_OFFSETS.forEach { offsetMinutes ->
                scheduleNotification(
                    identifier = countdownIdentifier(prayer.index, offsetMinutes),
                    prayerMinuteOfDay = prayer.minuteOfDay,
                    offsetMinutes = offsetMinutes,
                    title = "${prayer.name} in $offsetMinutes minutes",
                    message = "Jama'ah is at ${prayer.time}.",
                )
            }
        }
    }

    if (preferences.classesAndEvents) {
        classSchedule.forEachIndexed { index, session ->
            val day = session.reminderIsoDayOfWeek
            val minute = session.reminderMinuteOfDay
            if (day != null && minute != null) {
                scheduleWeeklyNotification(
                    identifier = classIdentifier(index),
                    isoDayOfWeek = day,
                    minuteOfDay = minute,
                    offsetMinutes = CLASS_ALERT_OFFSET_MINUTES,
                    title = "${session.title} in 1 hour",
                    message = "Starts at ${session.time}.",
                )
            }
        }
    }
}

actual fun loadNotificationPreferences(): NotificationPreferences {
    val defaults = NSUserDefaults.standardUserDefaults
    if (!defaults.boolForKey(NOTIFICATION_PREFERENCES_SAVED_KEY)) {
        return defaultNotificationPreferences
    }
    return NotificationPreferences(
        prayerReminders = defaults.boolForKey("prayerReminders"),
        countdownAlerts = defaults.boolForKey("countdownAlerts"),
        classesAndEvents = defaults.boolForKey("classesAndEvents"),
    )
}

actual fun saveNotificationPreferences(preferences: NotificationPreferences) {
    val defaults = NSUserDefaults.standardUserDefaults
    defaults.setBool(true, NOTIFICATION_PREFERENCES_SAVED_KEY)
    defaults.setBool(preferences.prayerReminders, "prayerReminders")
    defaults.setBool(preferences.countdownAlerts, "countdownAlerts")
    defaults.setBool(preferences.classesAndEvents, "classesAndEvents")
}

actual fun openPhoneNumber(phoneNumber: String) {
    openUrl("tel:${phoneNumber.filter { it.isDigit() || it == '+' }}")
}

actual fun openEmailAddress(emailAddress: String) {
    openUrl("mailto:$emailAddress")
}

actual fun openMapDirections(address: String) {
    val encodedAddress = address.stringByAddingPercentEncodingWithAllowedCharacters(URLQueryAllowedCharacterSet)
        ?: return
    val appleMapsUrl = "http://maps.apple.com/?q=$encodedAddress"
    val googleMapsUrl = "comgooglemaps://?q=$encodedAddress&directionsmode=driving"
    val actionSheet = UIAlertController.alertControllerWithTitle(
        title = "Open Directions",
        message = address,
        preferredStyle = UIAlertControllerStyleActionSheet,
    )
    actionSheet.addAction(
        UIAlertAction.actionWithTitle(
            title = "Apple Maps",
            style = UIAlertActionStyleDefault,
        ) { _ ->
            openUrl(appleMapsUrl)
        },
    )
    if (canOpenUrl(googleMapsUrl)) {
        actionSheet.addAction(
            UIAlertAction.actionWithTitle(
                title = "Google Maps",
                style = UIAlertActionStyleDefault,
            ) { _ ->
                openUrl(googleMapsUrl)
            },
        )
    }
    actionSheet.addAction(
        UIAlertAction.actionWithTitle(
            title = "Cancel",
            style = UIAlertActionStyleCancel,
            handler = null,
        ),
    )
    topViewController()?.presentViewController(actionSheet, animated = true, completion = null)
}

@OptIn(BetaInteropApi::class)
actual suspend fun fetchDarulUmmahHomeHtml(): String {
    return fetchUrlString("http://www.darulummah.org.uk/")
}

@OptIn(BetaInteropApi::class)
actual suspend fun fetchDarulUmmahPrayerTimetableHtml(): String {
    return fetchUrlString("http://www.darulummah.org.uk/mosque/prayer-timetable")
}

@OptIn(BetaInteropApi::class)
private suspend fun fetchUrlString(urlString: String): String {
    val url = NSURL.URLWithString(urlString)
        ?: error("Invalid Darul Ummah URL")
    val data = withContext(Dispatchers.Default) {
        NSData.dataWithContentsOfURL(url)
    } ?: error("Could not load Darul Ummah timetable")
    return NSString.create(data = data, encoding = NSUTF8StringEncoding)?.toString()
        ?: error("Could not decode Darul Ummah timetable")
}

private fun openUrl(urlString: String) {
    val url = NSURL.URLWithString(urlString) ?: return
    UIApplication.sharedApplication.openURL(url)
}

private fun canOpenUrl(urlString: String): Boolean {
    val url = NSURL.URLWithString(urlString) ?: return false
    return UIApplication.sharedApplication.canOpenURL(url)
}

private fun topViewController(): UIViewController? {
    var controller = UIApplication.sharedApplication.keyWindow?.rootViewController
    while (controller?.presentedViewController != null) {
        controller = controller.presentedViewController
    }
    return controller
}

private data class ScheduledPrayer(
    val index: Int,
    val name: String,
    val time: String,
    val minuteOfDay: Int,
)

private fun scheduleNotification(
    identifier: String,
    prayerMinuteOfDay: Int,
    offsetMinutes: Int,
    title: String,
    message: String,
) {
    val content = UNMutableNotificationContent().apply {
        setTitle(title)
        setBody(message)
        setSound(UNNotificationSound.defaultSound)
    }
    val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(
        timeInterval = secondsUntilReminder(prayerMinuteOfDay, offsetMinutes).toDouble(),
        repeats = false,
    )
    val request = UNNotificationRequest.requestWithIdentifier(
        identifier = identifier,
        content = content,
        trigger = trigger,
    )
    UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(request) { }
}

private fun scheduleWeeklyNotification(
    identifier: String,
    isoDayOfWeek: Int,
    minuteOfDay: Int,
    offsetMinutes: Int,
    title: String,
    message: String,
) {
    val content = UNMutableNotificationContent().apply {
        setTitle(title)
        setBody(message)
        setSound(UNNotificationSound.defaultSound)
    }
    val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(
        timeInterval = secondsUntilWeeklyReminder(
            isoDayOfWeek = isoDayOfWeek,
            minuteOfDay = minuteOfDay,
            offsetMinutes = offsetMinutes,
        ).toDouble(),
        repeats = false,
    )
    val request = UNNotificationRequest.requestWithIdentifier(
        identifier = identifier,
        content = content,
        trigger = trigger,
    )
    UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(request) { }
}

private fun notificationIdentifiers(): List<String> {
    return buildList {
        repeat(5) { index ->
            add("prayer-$index")
            PRAYER_ALERT_OFFSETS.forEach { offsetMinutes ->
                add(countdownIdentifier(index, offsetMinutes))
            }
        }
        repeat(classSchedule.size) { index ->
            add(classIdentifier(index))
        }
    }
}

private fun timeToMinuteOfDay(time: String): Int {
    val parts = time.split(":")
    return parts[0].toInt() * 60 + parts[1].toInt()
}

private fun secondsUntilReminder(
    prayerMinuteOfDay: Int,
    offsetMinutes: Int,
): Int {
    val reminderSecondOfDay = normalizedMinuteOfDay(prayerMinuteOfDay - offsetMinutes) * 60
    val currentSecond = currentSecondOfDay()
    return if (reminderSecondOfDay > currentSecond) {
        reminderSecondOfDay - currentSecond
    } else {
        (24 * 60 * 60 - currentSecond) + reminderSecondOfDay
    }
}

private fun secondsUntilWeeklyReminder(
    isoDayOfWeek: Int,
    minuteOfDay: Int,
    offsetMinutes: Int,
): Int {
    val reminderSecondOfDay = normalizedMinuteOfDay(minuteOfDay - offsetMinutes) * 60
    val currentSecond = currentSecondOfDay()
    val todayIsoDay = currentIsoDayOfWeek()
    val dayDelta = when {
        isoDayOfWeek > todayIsoDay -> isoDayOfWeek - todayIsoDay
        isoDayOfWeek < todayIsoDay -> 7 - (todayIsoDay - isoDayOfWeek)
        reminderSecondOfDay > currentSecond -> 0
        else -> 7
    }
    return dayDelta * 24 * 60 * 60 + (reminderSecondOfDay - currentSecond).let { delta ->
        if (delta >= 0) delta else delta + 24 * 60 * 60
    }
}

private fun normalizedMinuteOfDay(minuteOfDay: Int): Int {
    return ((minuteOfDay % (24 * 60)) + (24 * 60)) % (24 * 60)
}

private fun countdownIdentifier(
    prayerIndex: Int,
    offsetMinutes: Int,
): String {
    return "countdown-$prayerIndex-$offsetMinutes"
}

private fun classIdentifier(classIndex: Int): String {
    return "class-$classIndex"
}

private const val NOTIFICATION_PREFERENCES_SAVED_KEY = "notificationPreferencesSaved"
private val PRAYER_ALERT_OFFSETS = listOf(30, 10)
private const val CLASS_ALERT_OFFSET_MINUTES = 60
