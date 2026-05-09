# Stay Finder 🏠✨

**Stay Finder** is a premium, Airbnb-inspired Android application built with a modern hybrid architecture. It seamlessly integrates traditional XML-based Fragments with Jetpack Compose to deliver a high-performance, visually stunning property rental experience.

![Stay Finder Header](https://raw.githubusercontent.com/mshahnawaz1202/Stay-Finder-Airbnb-Inspired-Property-Rental-Application-/master/app/src/main/res/drawable/ic_home.xml) *(Note: Replace with actual screenshot link if available)*

---

## 🚀 Key Features

### 🔐 Advanced Authentication
- **Dual-Mode Login:** Secure Email/Password and one-tap Google Sign-In.
- **Session Persistence:** Persistent user sessions across app restarts using Firebase Auth.
- **Form Validation:** Real-time validation and error handling for a smooth onboarding experience.

### 💎 Premium UI/UX (Glassmorphism)
- **Visual Overhaul:** Stunning design with gradient overlays, glass-effect components, and curated typography.
- **Dark Mode Support:** A fully adaptive night theme with high-contrast surfaces and optimized color roles.
- **Floating Navigation:** A custom, pill-shaped floating bottom navigation bar for a futuristic feel.

### 📡 Real-time Data & Offline Sync
- **Firestore Integration:** Live syncing for property listings, user profiles, and favorites.
- **Offline Bookings:** Local SQLite-backed database (Room-like logic) for managing bookings without connectivity.
- **Smart Search:** Dynamic filtering by category (Beach, Mountain, City), price ranges, and ratings.

### 📍 Interactive Exploration
- **Integrated Maps:** Full Google Maps integration for exploring properties geographically.
- **Dynamic Recommendations:** AI-inspired "Recommended for you" logic based on rating and popularity.
- **Compose Surfaces:** A dedicated Favorites screen built entirely with **Jetpack Compose** and **Material 3**.

---

## 🛠️ Technology Stack

| Layer | Technologies |
|-------|--------------|
| **Core** | Kotlin, Coroutines, Hilt/DI (Legacy Patterns) |
| **UI** | Fragments (XML), Jetpack Compose, Material 3 |
| **Backend** | Firebase Auth, Firestore, Cloud Messaging (FCM) |
| **Networking** | Retrofit 2, GSON, OKHttp |
| **Local Data** | SQLite, SharedPreferences |
| **Media** | Coil (Compose), Glide (XML) |

---

## 📂 Project Structure

```text
com.example.stayfinder
├── auth/           # Login, Registration & Routing
├── firebase/       # Firestore Repositories & Messaging
├── ui/             # Core UI Fragments (Home, Map, Bookings)
│   └── compose/    # Jetpack Compose Screens (Favorites)
├── models/         # Data Models (Listing, Booking, Favorite)
├── features/       # Recommendation Engine & Search
└── database/       # Local SQLite Database Management
```

---

## ⚙️ Setup Instructions

1.  **Clone the Repo:**
    ```bash
    git clone https://github.com/mshahnawaz1202/Stay-Finder-Airbnb-Inspired-Property-Rental-Application-.git
    ```
2.  **Firebase Configuration:**
    - Place your `google-services.json` in the `app/` directory.
    - Enable Email/Password and Google Auth in the Firebase Console.
    - Setup Firestore with `listings`, `favorites`, and `users` collections.
3.  **API Keys:**
    - Add your Google Maps API key to `local.properties` or `AndroidManifest.xml`.
4.  **Build:**
    - Open in Android Studio (Ladybug or later recommended).
    - Sync Gradle and Run on an Emulator/Physical Device (API 24+).

---

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

---

*Stay Finder — Redefining how you find your next home away from home.*
