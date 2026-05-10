# Logic Map

| Req ID | Class/File | Function/Method | Implementation Description | (Self-researched: source URL) |
|--------|------------|----------------|---------------------------|-------------------------------|
| F1 | `FirebaseAuthManager.kt` | `getCurrentUser()` / `logout()` | Uses FirebaseAuth to manage auth state | – |
| F1 | `LoginActivity.kt` | `firebaseAuthWithGoogle()` | Launches Google Sign-In intent, handles result with Firebase Auth | – |
| F2 | `FirestoreManager.kt` | `getBookingsRef()` | Listens to `users/{uid}/bookings` collection in Firestore | – |
| F2 | `BookingsFragment.kt` | `setupRealtimeListener()` | Uses `addSnapshotListener` to sync bookings in real-time | – |
| Notif | `MyFirebaseMessagingService.kt` | `onMessageReceived()` | Creates notification channel, shows notification and saves locally | – |
| Compose | `NotificationHistoryActivity.kt` | `NotificationHistoryScreen()` | Jetpack Compose screen that displays LazyColumn of past notifications | – |
| Compose | `DetailFragment.kt` | `FavoriteButtonComposable()` | Displays a Compose button for toggling favorites | – |
| New1 | `ProfileFragment.kt` | `btnToggleTheme.setOnClickListener` | Uses `AppCompatDelegate.setDefaultNightMode` for Dark/Light theme toggle | https://developer.android.com/develop/ui/views/theming/darktheme |
| New2 | `DetailFragment.kt` | `imgShareBtn.setOnClickListener` | Creates implicit Intent with `ACTION_SEND` to share property via text | https://developer.android.com/training/sharing/send |
