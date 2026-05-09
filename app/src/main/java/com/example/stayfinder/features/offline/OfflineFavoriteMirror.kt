package com.example.stayfinder.features.offline

import android.content.Context

/**
 * Mirrors favorite listing ids locally. Firestore already caches offline; this provides
 * a simple SharedPreferences backup for quick reads and debugging.
 */
object OfflineFavoriteMirror {
    private const val PREFS = "offline_favorites_mirror"
    private const val KEY_IDS = "listing_ids"

    fun persist(context: Context, listingIds: List<String>) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putStringSet(KEY_IDS, listingIds.toSet())
            .apply()
    }

    fun readIds(context: Context): Set<String> {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getStringSet(KEY_IDS, emptySet())
            .orEmpty()
    }
}
