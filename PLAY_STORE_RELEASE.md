# Google Play Release Checklist

## App Identity

- App name: `Darul Ummah Shadwell`
- Android package name: `com.sultanyahmed.darulummahapp`
- Current version: `1.0.0`
- Current version code: `1`
- Minimum Android version: API 24
- Target Android version: API 36

The package name is permanent after the first Play Console upload. Do not change it after publishing.

## Build The Play Store Bundle

Google Play requires new apps to be published as Android App Bundles.

Create an upload keystore once and keep it private:

```bash
keytool -genkeypair \
  -v \
  -keystore upload-keystore.jks \
  -alias upload \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

Add the signing values to `local.properties` on your machine. Do not commit these values:

```properties
play.storeFile=upload-keystore.jks
play.storePassword=YOUR_KEYSTORE_PASSWORD
play.keyAlias=upload
play.keyPassword=YOUR_KEY_PASSWORD
```

Then build the uploadable bundle:

```bash
./gradlew :composeApp:bundleRelease
```

The file to upload is:

```text
composeApp/build/outputs/bundle/release/composeApp-release.aab
```

For each future Play Store update, increase `versionCode` in `composeApp/build.gradle.kts` before building a new bundle.

## Store Listing Assets

Prepare these before creating the listing:

- App icon: 512 x 512 PNG
- Feature graphic: 1024 x 500 PNG or JPG
- Phone screenshots: at least 2, recommended 4 to 8
- Short description: 80 characters max
- Full description: up to 4,000 characters
- App category: likely `Lifestyle` or `Education`
- Contact email
- Website, if available
- Privacy policy URL

Suggested short description:

```text
Prayer times, announcements, events, and Qibla tools for Darul Ummah Shadwell.
```

Suggested full description:

```text
Darul Ummah Shadwell helps the local community stay connected with mosque prayer times, Jummah details, announcements, classes, events, notifications, contact actions, directions, and a Qibla compass.

The app includes live timetable information, reminders, announcement updates, and mosque contact details in one simple mobile experience.
```

## App Content Forms

Complete these in Play Console under App content:

- Privacy policy
- App access
- Ads: select `No` unless ads are added later
- Content rating questionnaire
- Target audience and content
- News apps: select `No`
- Data safety
- Government apps: select `No`, unless the mosque has a specific government affiliation
- Financial features: select `No`
- Health apps: select `No`

## Data Safety Guidance

Review this carefully against the final production behavior before submitting:

- Location: the Qibla compass requests approximate/fine location permission and processes latitude/longitude on device for compass behavior/logging.
- Email/phone/directions: the app opens external phone, mail, and maps apps; it does not need to store those user actions.
- Notifications: the app schedules local reminders.
- Announcements/media: admin media upload uses Supabase. If admin-only features are available in production, disclose any collected/shared media or account data accurately.
- No ads are currently configured in the codebase.

Because the app requests location permission, the Play listing and the app should have a privacy policy URL.

## Recommended Pre-Submission Checks

Run:

```bash
./gradlew :composeApp:check
./gradlew :composeApp:bundleRelease
```

Install and test a release build through Play Console internal testing before production release.

