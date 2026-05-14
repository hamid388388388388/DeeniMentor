<div align="center">

# ☪ DEENI Mentor

### Your Personal Islamic Life Growth Companion

![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android)
![Language](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=flat-square&logo=kotlin)
![UI](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=flat-square&logo=jetpackcompose)
![Firebase](https://img.shields.io/badge/Auth-Firebase-FFCA28?style=flat-square&logo=firebase)
![License](https://img.shields.io/badge/License-MIT-green?style=flat-square)

**DEENI Mentor** is a fully functional Android app that helps Muslims track their daily Islamic practices, read the Holy Quran, monitor spiritual growth, and stay consistent with their faith.

> *"The best of deeds is that which is done consistently, even if it is small."*
> — Sahih al-Bukhari

</div>

---

## 📱 Screenshots

| Splash | Home | Prayer Times |
|:---:|:---:|:---:|
| ![Splash](screenshots/01_splash.jpg) | ![Home](screenshots/05_home.jpg) | ![Prayer](screenshots/06_prayer_times.jpg) |

| Daily Check-In | Mood & Productivity | Holy Quran |
|:---:|:---:|:---:|
| ![CheckIn](screenshots/07_checkin_salah.jpg) | ![Mood](screenshots/08_checkin_mood.jpg) | ![Quran](screenshots/09_quran_list.jpg) |

| Quran Reader | Analytics | Dua Collection |
|:---:|:---:|:---:|
| ![Reader](screenshots/10_quran_read.jpg) | ![Analytics](screenshots/11_analytics.jpg) | ![Dua](screenshots/13_dua.jpg) |

| Profile & Streak | Islamic Goals | Settings (Dark) |
|:---:|:---:|:---:|
| ![Profile](screenshots/14_profile.jpg) | ![Goals](screenshots/16_goals.jpg) | ![Settings](screenshots/15_settings.jpg) |

---

## ✨ Features

| Feature | Description |
|---|---|
| 🔐 **Firebase Auth** | Secure login and registration |
| 🌿 **Growth Paths** | Starter, Pro, and Ihsan levels |
| 🕌 **Prayer Times** | Islamabad namaz times with 10-min reminders |
| ✅ **Daily Check-In** | Track Salah, sleep, mood, and good deeds |
| 📖 **Holy Quran** | Full Quran with Arabic text and English translation |
| 📊 **Analytics** | Visual progress charts and stats |
| 🤲 **Dua Collection** | 100+ duas with Arabic, transliteration & translation |
| 🎯 **Islamic Goals** | Set and track your spiritual goals |
| 🔥 **Streak System** | Daily streaks and achievement badges |
| 📿 **Tasbeeh Counter** | Digital dhikr counter |
| 🌙 **Dark Mode** | Full dark theme support |
| 🔔 **Smart Notifications** | Prayer, sleep, and daily reminders |

---

## 🛠 Tech Stack

```
Language       →  Kotlin 2.0
UI Framework   →  Jetpack Compose (BOM 2025)
Architecture   →  MVVM + Clean Architecture
Database       →  Room DB (local)
Auth           →  Firebase Authentication
Navigation     →  Jetpack Navigation Compose
DI             →  Hilt
Background     →  WorkManager
Preferences    →  DataStore
Min SDK        →  API 26 (Android 8.0)
Target SDK     →  API 35 (Android 15)
```

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Panda 2025.3.2 or later
- Android SDK API 26+
- Firebase project with Email/Password auth enabled

### Setup

**1. Clone the repository**
```bash
git clone https://github.com/YOUR_USERNAME/DeeniMentor.git
cd DeeniMentor
```

**2. Add Firebase configuration**
- Go to [Firebase Console](https://console.firebase.google.com)
- Create a new project (or use existing)
- Enable Email/Password Authentication
- Download `google-services.json`
- Place it in `app/` folder

**3. Open in Android Studio**
```
File → Open → Select the DeeniMentor folder
Wait for Gradle sync to complete
```

**4. Run the app**
```
Select Pixel 6 emulator (API 36)
Click ▶ Run
```

---

## 📁 Project Structure

```
com.deenimentor/
├── data/
│   ├── db/              # Room Database, DAOs
│   ├── model/           # Data entities
│   └── repository/      # AppRepository (single source of truth)
├── notifications/       # WorkManager, NotificationHelper
├── ui/
│   ├── analytics/       # Analytics dashboard
│   ├── auth/            # Login & Register
│   ├── checkin/         # Daily check-in
│   ├── dua/             # Dua collection
│   ├── goals/           # Islamic goals tracker
│   ├── home/            # Home screen
│   ├── onboarding/      # Growth path selection
│   ├── prayer/          # Prayer times
│   ├── profile/         # Streak & achievements
│   ├── quran/           # Quran reader & tracker
│   ├── settings/        # App settings
│   ├── splash/          # Splash screen
│   ├── tasbeeh/         # Tasbeeh counter
│   └── theme/           # Colors, typography
├── MainActivity.kt
└── Routes.kt
```

---

## 🏗 Architecture

```
┌──────────────────────────────────────┐
│           Presentation Layer          │
│   Jetpack Compose UI + ViewModels    │
└──────────────┬───────────────────────┘
               │
┌──────────────▼───────────────────────┐
│            Domain Layer               │
│         AppRepository                │
└──────────────┬───────────────────────┘
               │
┌──────────────▼───────────────────────┐
│             Data Layer                │
│   Room DB (local) + Firebase (auth)  │
└──────────────────────────────────────┘
```

---

## 👥 Team

| Member | Roll No | Responsibilities |
|---|---|---|
| Abdul Rauf | 24I-0060 | Architecture, Analytics, Firebase |
| M. Hamid | 24F-0030 | Quran Reader, Auth, Database |
| Fauzan Tahir | 24F-0042 | Check-In, Notifications, Goals, UI |

**Institution:** FAST-NUCES | **Program:** BS Artificial Intelligence | **Year:** 2026

---

## 📄 License

```
MIT License — Free to use, modify, and distribute
```

---

<div align="center">

Made with ❤️ for Muslims everywhere

**بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيم**

</div>
