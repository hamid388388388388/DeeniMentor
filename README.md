# DEENI Mentor — Islamic Life Growth Companion

## Project Info
- **App Name:** DEENI Mentor
- **Team:** Abdul Rauf (24I-0060), M. Hamid (24F-0030), Fauzan Tahir (24F-0042)
- **Institution:** FAST-NUCES | BS Artificial Intelligence
- **Iteration:** 3 (Final)

## Features
1. Firebase Authentication (Login/Register)
2. Growth Path Onboarding (Starter/Pro/Ihsan)
3. Daily Check-In (Salah, Sleep, Mood, Productivity)
4. Quran Progress Tracker
5. Analytics Dashboard
6. Dua Collection with Search & Favorites
7. Islamic Goals Tracker
8. Profile & Streak System
9. Settings Screen
10. Daily Reminder Notifications

## How to Run

### Prerequisites
- Android Studio Panda 2025.3.2 or later
- Android SDK API 28+
- Google Services JSON file

### Setup Steps
1. Clone or extract the project
2. Open Android Studio → File → Open → Select `DeeniMentor` folder
3. Add your `google-services.json` to `app/` folder
4. Enable Firebase Email/Password Authentication in Firebase Console
5. Wait for Gradle sync to complete
6. Run on emulator (Pixel 6, API 36) or physical device

### Build Requirements
- Kotlin 2.0+
- Jetpack Compose BOM
- Room Database
- Firebase Auth + Firestore
- WorkManager

## Architecture
MVVM (Model-View-ViewModel) with Clean Architecture
- **UI Layer:** Jetpack Compose screens + ViewModels
- **Data Layer:** Room DB (local) + Firebase (auth)
- **Repository Pattern:** Single source of truth

## Package Structure
```
com.deenimentor/
├── data/
│   ├── db/          (Room Database, DAOs)
│   ├── model/       (Entities)
│   └── repository/  (AppRepository)
├── notifications/   (WorkManager, NotificationHelper)
├── ui/
│   ├── analytics/
│   ├── auth/
│   ├── checkin/
│   ├── dua/
│   ├── goals/
│   ├── home/
│   ├── onboarding/
│   ├── profile/
│   ├── quran/
│   ├── settings/
│   ├── splash/
│   └── theme/
├── MainActivity.kt
└── Routes.kt
```
