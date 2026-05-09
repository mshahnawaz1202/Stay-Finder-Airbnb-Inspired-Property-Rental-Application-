# Stay Finder 🏠✨

**Stay Finder** is a premium, Airbnb-inspired Android application built with a modern hybrid architecture. It seamlessly integrates traditional XML-based Fragments with Jetpack Compose to deliver a high-performance, visually stunning property rental experience.

---

## 🌟 Visual Showcase

| Screen | Description | Style |
|--------|-------------|-------|
| **Home** | Curated listings with smart categories & search. | Glassmorphism |
| **Favorites** | Real-time wishlist managed via Jetpack Compose. | Material 3 |
| **Map** | Geographic exploration with interactive markers. | Hybrid |
| **Auth** | Premium onboarding with Google & Email sync. | Elevated |

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
- **Offline Bookings:** Local SQLite-backed database for managing bookings without connectivity.
- **Smart Search:** Dynamic filtering by category (Beach, Mountain, City), price ranges, and ratings.

### 📍 Interactive Exploration
- **Integrated Maps:** Full Google Maps integration for exploring properties geographically.
- **Dynamic Recommendations:** Intelligent recommendation logic based on rating and popularity.
- **Compose Surfaces:** A dedicated Favorites screen built entirely with **Jetpack Compose** and **Material 3**.

---

## 🛠️ Technology Stack

- **Language:** [Kotlin](https://kotlinlang.org/) (100%)
- **Concurrency:** [Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) & [Flow](https://kotlinlang.org/docs/flow.html)
- **Dependency Injection:** Modular singleton patterns
- **Backend:** [Firebase](https://firebase.google.com/) (Auth, Firestore, FCM)
- **UI Architecture:** Hybrid (XML Fragments + Jetpack Compose)
- **Networking:** [Retrofit 2](https://square.github.io/retrofit/) & [GSON](https://github.com/google/gson)
- **Image Loading:** [Coil](https://coil-kt.github.io/coil/) & [Glide](https://github.com/bumptech/glide)

---

## 📂 Project Architecture

The project follows a modular structure for scalability and maintainability:

```text
com.example.stayfinder
├── auth/           # Login, Registration & Auth Router
├── firebase/       # Firestore Repositories & Messaging Service
├── ui/             # View Layer (Fragments & Custom Views)
│   └── compose/    # Jetpack Compose Screens & Theme
├── models/         # Domain Data Models
├── repository/     # Data Access Layer (API & Local)
├── features/       # Business Logic (Recommendations, Search)
└── database/       # SQLite Storage Logic
```

---

## ⚙️ Installation & Setup

1.  **Clone the Repository:**
    ```bash
    git clone https://github.com/mshahnawaz1202/Stay-Finder-Airbnb-Inspired-Property-Rental-Application-.git
    ```
2.  **Firebase Setup:**
    - Download `google-services.json` from your Firebase project.
    - Place it in the `app/` directory.
    - Enable **Email/Password** and **Google** authentication in Firebase.
3.  **Google Maps API:**
    - Obtain an API key from [Google Cloud Console](https://console.cloud.google.com/).
    - Add it to your `local.properties` or directly in `AndroidManifest.xml`.
4.  **Build & Run:**
    - Sync the project with Gradle in Android Studio.
    - Deploy to a device with API level 24 or higher.

---

*Stay Finder — Redefining how you find your next home away from home.*
