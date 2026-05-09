# Stay Finder — Design Map

This document maps **visual design**, **UI surfaces**, and **navigation** for the hybrid Fragment + Jetpack Compose app.

**Premium refresh:** warm canvas (`premiumCanvas`), burgundy-wine accent (`premiumAccentDeep`), outlined search + filter surfaces, floating pill bottom nav, elevated listing cards with glass badges, gradient auth + elevated form cards, and expanded Material 3 **Compose** color roles for Favorites.

---

## 1. Design system

### Brand and color
| Role | Light | Dark (values-night) | Notes |
|------|--------|----------------------|--------|
| Primary | `#E53935` (`primaryRed`) | `#FF6F60` (`primaryRedLight`) | CTAs, nav accent, chips when selected |
| Primary dark | `#C62828` | — | Status bar (light theme) |
| Surfaces | `#FFFFFF` / `#FAFAFA` | `#1E1E1E` / `#121212` | Cards, backgrounds |
| Text primary | `#212121` | `#F5F5F5` | Titles, body |
| Text secondary | `#616161` | `#BDBDBD` | Subtitles, hints |
| Status | Green / amber / blue | Same tokens | Bookings chips, badges |

Chips for categories use `chipBgLight` + `chipTextLight` (pink-tinted background, red text) for a consistent “Airbnb-adjacent” accent.

### Typography
- **Headings / emphasis:** `sans-serif-medium` (XML layouts).
- **Body:** default sans-serif; sizes typically **14–28sp** for hierarchy (explore title 28sp, section headers 18–20sp).
- **Compose (Favorites):** Material 3 `Typography` defaults from `StayFinderTheme` (only primary color is customized; scale follows M3).

### Shape and elevation
- **Corner radius:** **12dp** (buttons, text fields), **16–20dp** (cards, bottom sheet–style map card), **28dp** (floating search on map).
- **Elevation:** bottom nav **12dp**; cards **2–8dp** depending on layer.

### Theme entry points
- **Views / Fragments:** `Theme.StayFinder` → `Theme.Material3.DayNight.NoActionBar` (`values/themes.xml`, `values-night/themes.xml`).
- **Compose:** `StayFinderTheme` in `ui/compose/Theme.kt` — mirrors primary red; follows system light/dark.

### Dark mode (home)
`HomeFragment` exposes a **MaterialSwitch** (`switchDarkMode`) that toggles `AppCompatDelegate` night mode app-wide (affects XML UI; Compose favorites follow `isSystemInDarkTheme()`).

---

## 2. Information architecture

```text
AuthRouterActivity (no chrome)
    ├── signed in → MainActivity
    └── signed out → AuthActivity
            ├── LoginFragment
            └── RegisterFragment (back stack)

MainActivity
    └── FrameLayout (content_frame) + BottomNavigationView
            ├── HomeFragment        (listings + filters + recommendations)
            ├── FavoritesFragment   → ComposeView → FavoritesComposeRoute
            ├── MapFragment         (embedded map + search card)
            ├── BookingsFragment
            └── ProfileFragment
    └── DetailFragment, HostFragment (pushed on content_frame + back stack)
```

---

## 3. Screen inventory

| Screen | Technology | Layout / entry | Primary actions |
|--------|------------|----------------|-----------------|
| Auth router | Activity | Theme only | Redirect |
| Login | Fragment | `fragment_login` | Email login, Google, link to register |
| Register | Fragment | `fragment_register` | Create account, back to login |
| Home | Fragment | `fragment_home` | Search, filters, chips, dual lists, dark toggle |
| Listing detail | Fragment | `activity_listing_details` | Favorite, book, back |
| Favorites | Fragment host | `fragment_favorites` → Compose | Search, remove, Firestore-driven list |
| Map | Fragment | `activity_map` | Full-screen map + top card + CTA |
| Bookings | Fragment | `fragment_bookings` | Search, sort, CRUD dialogs |
| Profile | Fragment | `activity_profile` | Host listing, logout |
| Host | Fragment | `fragment_host` | Publish listing (Firestore) |

---

## 4. Component patterns (XML)

- **Inputs:** `TextInputLayout` outlined boxes, **12dp** corners (`fragment_login`, `fragment_register`, `fragment_host`).
- **Primary buttons:** `MaterialButton`, `primaryRed` fill, **12dp** radius, sentence case.
- **Secondary:** Outlined Google button on login (`strokeColor` primary red).
- **Lists:** `RecyclerView` + `item_house_listing` (image, title, location, price badge, rating, guest/bed/bath row).
- **Bottom navigation:** `BottomNavigationView`, labeled mode, tint from `bottom_nav_color`.
- **Profile header:** Gradient `profile_header_bg`, circular `ShapeableImageView`, stats row.

---

## 5. Compose surface (Favorites)

- **Scaffold pattern:** `TopAppBar` + `OutlinedTextField` + `LazyColumn` of `Card`s.
- **Imagery:** `AsyncImage` (Coil), **180dp** height, `ContentScale.Crop`, placeholder/error `ic_home`.
- **Actions:** `TextButton` for remove (destructive by action, not color — keeps M3 default text button).

Compose is **scoped to the Favorites tab** so the rest of the app stays Fragment/XML for assignment constraints.

---

## 6. Navigation and state

- **Primary navigation:** Bottom nav **replaces** `content_frame` (no separate NavHost graph in XML).
- **Secondary:** `FragmentTransaction.replace` + `addToBackStack` for **Detail** and **Host**.
- **Auth:** Clearing task to `AuthRouterActivity` / `MainActivity` for a clean stack after login/logout.

---

## 7. Assets and iconography

- **Vector drawables:** `ic_home`, `ic_favorite`, `ic_map`, `ic_person`, `ic_search`, amenities (`ic_wifi`, `ic_pool`, …), `ic_location`, `ic_star`.
- **Listing placeholders:** `ic_home` where photos are missing (Glide / Compose).

---

## 8. Related docs

- **Architecture / Firebase mapping:** `docs/logic_map.html` (print to PDF if required).

---

## 9. Design consistency checklist (for future UI work)

1. Prefer **12dp** radii on new buttons and fields; **16dp+** on cards.
2. Use **`primaryRed`** for primary actions; outlined variant for secondary on auth.
3. New **Compose** screens should wrap with **`StayFinderTheme`** and reuse **primary `0xFFE53935`** (or extend `Theme.kt` with full typography/surfaces).
4. New **Fragments** should use **`?android:colorBackground`** / **`?colorSurface`** for night compatibility instead of hard-coded whites.
