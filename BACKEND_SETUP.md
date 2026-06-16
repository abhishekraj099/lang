# LinguaPath Backend Setup

This backend plan follows `LinguaPath_Blueprint.docx`: Firebase Auth + Firestore + FCM + Crashlytics/Analytics, while Room remains the offline-first source of truth.

## What Is Implemented Now

- Room `sync_outbox` table for progress events waiting to upload.
- Database migration `3 -> 4` that preserves existing local progress.
- Local sync events queued for:
  - lesson completion
  - SRS review
  - daily challenge completion
- Profile screen shows pending cloud sync event count.
- Firestore security rules draft in `firestore.rules`.

## Firebase Project Steps

1. Create Firebase project.
2. Add Android app using the final package name.
3. Download `google-services.json`.
4. Put `google-services.json` into `app/`.
5. Enable Firebase Authentication:
   - Email/password
   - Google Sign-In
6. Enable Firestore.
7. Publish `firestore.rules`.
8. Enable Cloud Messaging.
9. Enable Crashlytics and Analytics.

## Firestore Shape

```text
users/{userId}
  displayName
  email
  selectedLanguages
  activeLanguage
  placementResults
  settings
  createdAt

users/{userId}/progress/{language}
  totalXp
  streakDays
  completedLessons
  dailyGoalMinutes
  lastSyncedAt

users/{userId}/syncEvents/{eventId}
  eventType
  payload
  createdAt

leaderboard/{weekId}
  userId
  displayName
  xp
  language
```

## Sync Strategy

- App writes to Room first.
- Each important progress action creates a `sync_outbox` event.
- Firebase sync worker will upload pending events after login.
- After successful upload, events are marked synced locally.
- Lesson content remains bundled offline to avoid Firestore read cost.

## Needed From You

- Final app name.
- Final package name.
- `google-services.json`.
- Firebase Web client ID for Google Sign-In if needed.
- Decision: keep guest mode before login? Recommended: yes.
- Decision: allow cloud sync only after login? Recommended: yes.
