package com.example.stayfinder

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// ─────────────────────────────────────────────
// Data models for the two DB tables
// ─────────────────────────────────────────────

data class ListingEntity(
    val id: Long = 0,
    val title: String,
    val price: String,
    val amenities: String = "",
    val imageUrl: String = ""
)

data class FavoriteEntity(
    val id: Long = 0,
    val listingId: Long,   // FK → listings(id)
    val note: String = ""
)

// ─────────────────────────────────────────────
// DatabaseHelper
// ─────────────────────────────────────────────

class DatabaseHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    companion object {
        // ── Database meta ──────────────────────────────────────────────
        private const val DATABASE_NAME    = "stayfinder.db"
        private const val DATABASE_VERSION = 1

        // ── Table: listings ───────────────────────────────────────────
        const val TABLE_LISTINGS          = "listings"
        const val COL_LISTING_ID          = "id"
        const val COL_LISTING_TITLE       = "title"
        const val COL_LISTING_PRICE       = "price"
        const val COL_LISTING_AMENITIES   = "amenities"
        const val COL_LISTING_IMAGE_URL   = "image_url"

        // ── Table: favorites ──────────────────────────────────────────
        const val TABLE_FAVORITES         = "favorites"
        const val COL_FAV_ID              = "id"
        const val COL_FAV_LISTING_ID      = "listing_id"   // FK → listings(id)
        const val COL_FAV_NOTE            = "note"

        // ── CREATE statements ─────────────────────────────────────────
        private const val SQL_CREATE_LISTINGS = """
            CREATE TABLE $TABLE_LISTINGS (
                $COL_LISTING_ID        INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_LISTING_TITLE     TEXT    NOT NULL,
                $COL_LISTING_PRICE     TEXT    NOT NULL,
                $COL_LISTING_AMENITIES TEXT,
                $COL_LISTING_IMAGE_URL TEXT
            )
        """

        /**
         * The FOREIGN KEY declaration on COL_FAV_LISTING_ID creates the
         * relational link between 'favorites' and 'listings'.
         * ON DELETE CASCADE ensures orphan rows are automatically removed.
         */
        private const val SQL_CREATE_FAVORITES = """
            CREATE TABLE $TABLE_FAVORITES (
                $COL_FAV_ID         INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_FAV_LISTING_ID INTEGER,
                $COL_FAV_NOTE       TEXT,
                FOREIGN KEY($COL_FAV_LISTING_ID)
                    REFERENCES $TABLE_LISTINGS($COL_LISTING_ID)
                    ON DELETE CASCADE
            )
        """

        private const val SQL_DROP_LISTINGS  = "DROP TABLE IF EXISTS $TABLE_LISTINGS"
        private const val SQL_DROP_FAVORITES = "DROP TABLE IF EXISTS $TABLE_FAVORITES"
    }

    // ── Lifecycle callbacks ────────────────────────────────────────────

    override fun onCreate(db: SQLiteDatabase) {
        // Enable foreign-key enforcement for this connection
        db.execSQL("PRAGMA foreign_keys = ON")

        // Create tables (favorites depends on listings → listings first)
        db.execSQL(SQL_CREATE_LISTINGS)
        db.execSQL(SQL_CREATE_FAVORITES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Simple strategy: drop and recreate on version bump
        db.execSQL(SQL_DROP_FAVORITES)   // drop child table first (FK constraint)
        db.execSQL(SQL_DROP_LISTINGS)
        onCreate(db)
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        // Re-enable foreign keys every time the DB is opened (required by SQLite)
        if (!db.isReadOnly) {
            db.execSQL("PRAGMA foreign_keys = ON")
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // CRUD — listings
    // ══════════════════════════════════════════════════════════════════

    /** Insert a listing and return its new row ID (-1 on failure). */
    fun insertListing(listing: ListingEntity): Long {
        val values = ContentValues().apply {
            put(COL_LISTING_TITLE,     listing.title)
            put(COL_LISTING_PRICE,     listing.price)
            put(COL_LISTING_AMENITIES, listing.amenities)
            put(COL_LISTING_IMAGE_URL, listing.imageUrl)
        }
        return writableDatabase.insert(TABLE_LISTINGS, null, values)
    }

    /** Return all rows from the listings table. */
    fun getAllListings(): List<ListingEntity> {
        val result = mutableListOf<ListingEntity>()
        val cursor: Cursor = readableDatabase.query(
            TABLE_LISTINGS, null, null, null, null, null, "$COL_LISTING_ID ASC"
        )
        cursor.use {
            while (it.moveToNext()) {
                result += ListingEntity(
                    id        = it.getLong(it.getColumnIndexOrThrow(COL_LISTING_ID)),
                    title     = it.getString(it.getColumnIndexOrThrow(COL_LISTING_TITLE)),
                    price     = it.getString(it.getColumnIndexOrThrow(COL_LISTING_PRICE)),
                    amenities = it.getString(it.getColumnIndexOrThrow(COL_LISTING_AMENITIES)) ?: "",
                    imageUrl  = it.getString(it.getColumnIndexOrThrow(COL_LISTING_IMAGE_URL)) ?: ""
                )
            }
        }
        return result
    }

    /** Fetch a single listing by its primary key. Returns null if not found. */
    fun getListingById(id: Long): ListingEntity? {
        val cursor: Cursor = readableDatabase.query(
            TABLE_LISTINGS,
            null,
            "$COL_LISTING_ID = ?",
            arrayOf(id.toString()),
            null, null, null
        )
        return cursor.use {
            if (it.moveToFirst()) {
                ListingEntity(
                    id        = it.getLong(it.getColumnIndexOrThrow(COL_LISTING_ID)),
                    title     = it.getString(it.getColumnIndexOrThrow(COL_LISTING_TITLE)),
                    price     = it.getString(it.getColumnIndexOrThrow(COL_LISTING_PRICE)),
                    amenities = it.getString(it.getColumnIndexOrThrow(COL_LISTING_AMENITIES)) ?: "",
                    imageUrl  = it.getString(it.getColumnIndexOrThrow(COL_LISTING_IMAGE_URL)) ?: ""
                )
            } else null
        }
    }

    /** Update an existing listing. Returns the number of rows affected. */
    fun updateListing(listing: ListingEntity): Int {
        val values = ContentValues().apply {
            put(COL_LISTING_TITLE,     listing.title)
            put(COL_LISTING_PRICE,     listing.price)
            put(COL_LISTING_AMENITIES, listing.amenities)
            put(COL_LISTING_IMAGE_URL, listing.imageUrl)
        }
        return writableDatabase.update(
            TABLE_LISTINGS, values, "$COL_LISTING_ID = ?", arrayOf(listing.id.toString())
        )
    }

    /**
     * Delete a listing by ID.
     * Because ON DELETE CASCADE is active, all matching favorites rows
     * are automatically removed by SQLite.
     */
    fun deleteListing(id: Long): Int =
        writableDatabase.delete(TABLE_LISTINGS, "$COL_LISTING_ID = ?", arrayOf(id.toString()))

    // ══════════════════════════════════════════════════════════════════
    // CRUD — favorites
    // ══════════════════════════════════════════════════════════════════

    /** Add a listing to favorites. Returns the new row ID (-1 on failure). */
    fun insertFavorite(favorite: FavoriteEntity): Long {
        val values = ContentValues().apply {
            put(COL_FAV_LISTING_ID, favorite.listingId)
            put(COL_FAV_NOTE,       favorite.note)
        }
        return writableDatabase.insert(TABLE_FAVORITES, null, values)
    }

    /** Return all favorite records. */
    fun getAllFavorites(): List<FavoriteEntity> {
        val result = mutableListOf<FavoriteEntity>()
        val cursor: Cursor = readableDatabase.query(
            TABLE_FAVORITES, null, null, null, null, null, "$COL_FAV_ID ASC"
        )
        cursor.use {
            while (it.moveToNext()) {
                result += FavoriteEntity(
                    id        = it.getLong(it.getColumnIndexOrThrow(COL_FAV_ID)),
                    listingId = it.getLong(it.getColumnIndexOrThrow(COL_FAV_LISTING_ID)),
                    note      = it.getString(it.getColumnIndexOrThrow(COL_FAV_NOTE)) ?: ""
                )
            }
        }
        return result
    }

    /** Check whether a given listing is already in favorites. */
    fun isFavorite(listingId: Long): Boolean {
        val cursor = readableDatabase.query(
            TABLE_FAVORITES,
            arrayOf(COL_FAV_ID),
            "$COL_FAV_LISTING_ID = ?",
            arrayOf(listingId.toString()),
            null, null, null
        )
        return cursor.use { it.count > 0 }
    }

    /** Update the user note for a favorite row. Returns rows affected. */
    fun updateFavoriteNote(favoriteId: Long, note: String): Int {
        val values = ContentValues().apply { put(COL_FAV_NOTE, note) }
        return writableDatabase.update(
            TABLE_FAVORITES, values, "$COL_FAV_ID = ?", arrayOf(favoriteId.toString())
        )
    }

    /** Remove a single favorite row by its own ID. */
    fun deleteFavorite(favoriteId: Long): Int =
        writableDatabase.delete(TABLE_FAVORITES, "$COL_FAV_ID = ?", arrayOf(favoriteId.toString()))

    /** Remove a favorite by the associated listing ID. */
    fun deleteFavoriteByListingId(listingId: Long): Int =
        writableDatabase.delete(
            TABLE_FAVORITES, "$COL_FAV_LISTING_ID = ?", arrayOf(listingId.toString())
        )

    // ══════════════════════════════════════════════════════════════════
    // JOIN query — favorites with full listing details
    // ══════════════════════════════════════════════════════════════════

    /**
     * Returns a list of pairs: (FavoriteEntity, ListingEntity)
     * using an INNER JOIN on the foreign key relationship.
     *
     * SQL equivalent:
     *   SELECT f.*, l.title, l.price, l.amenities, l.image_url
     *   FROM favorites f
     *   INNER JOIN listings l ON f.listing_id = l.id
     */
    fun getFavoritesWithListings(): List<Pair<FavoriteEntity, ListingEntity>> {
        val sql = """
            SELECT
                f.$COL_FAV_ID        AS fav_id,
                f.$COL_FAV_LISTING_ID,
                f.$COL_FAV_NOTE,
                l.$COL_LISTING_ID    AS listing_id,
                l.$COL_LISTING_TITLE,
                l.$COL_LISTING_PRICE,
                l.$COL_LISTING_AMENITIES,
                l.$COL_LISTING_IMAGE_URL
            FROM $TABLE_FAVORITES f
            INNER JOIN $TABLE_LISTINGS l
                ON f.$COL_FAV_LISTING_ID = l.$COL_LISTING_ID
            ORDER BY f.$COL_FAV_ID ASC
        """.trimIndent()

        val result = mutableListOf<Pair<FavoriteEntity, ListingEntity>>()
        val cursor = readableDatabase.rawQuery(sql, null)
        cursor.use {
            while (it.moveToNext()) {
                val fav = FavoriteEntity(
                    id        = it.getLong(it.getColumnIndexOrThrow("fav_id")),
                    listingId = it.getLong(it.getColumnIndexOrThrow(COL_FAV_LISTING_ID)),
                    note      = it.getString(it.getColumnIndexOrThrow(COL_FAV_NOTE)) ?: ""
                )
                val listing = ListingEntity(
                    id        = it.getLong(it.getColumnIndexOrThrow("listing_id")),
                    title     = it.getString(it.getColumnIndexOrThrow(COL_LISTING_TITLE)),
                    price     = it.getString(it.getColumnIndexOrThrow(COL_LISTING_PRICE)),
                    amenities = it.getString(it.getColumnIndexOrThrow(COL_LISTING_AMENITIES)) ?: "",
                    imageUrl  = it.getString(it.getColumnIndexOrThrow(COL_LISTING_IMAGE_URL)) ?: ""
                )
                result += Pair(fav, listing)
            }
        }
        return result
    }

    /**
     * Requirement F5: Dynamic Search / Filter
     * Execute LIKE query on title and sort by price.
     */
    fun searchFavoritesWithListings(query: String, isAscending: Boolean): List<Pair<FavoriteEntity, ListingEntity>> {
        val sortOrder = if (isAscending) "ASC" else "DESC"
        val sql = """
            SELECT
                f.$COL_FAV_ID        AS fav_id,
                f.$COL_FAV_LISTING_ID,
                f.$COL_FAV_NOTE,
                l.$COL_LISTING_ID    AS listing_id,
                l.$COL_LISTING_TITLE,
                l.$COL_LISTING_PRICE,
                l.$COL_LISTING_AMENITIES,
                l.$COL_LISTING_IMAGE_URL
            FROM $TABLE_FAVORITES f
            INNER JOIN $TABLE_LISTINGS l
                ON f.$COL_FAV_LISTING_ID = l.$COL_LISTING_ID
            WHERE l.$COL_LISTING_TITLE LIKE ?
            ORDER BY CAST(l.$COL_LISTING_PRICE AS REAL) $sortOrder
        """.trimIndent()

        val result = mutableListOf<Pair<FavoriteEntity, ListingEntity>>()
        val cursor = readableDatabase.rawQuery(sql, arrayOf("%${query}%"))
        cursor.use {
            while (it.moveToNext()) {
                val fav = FavoriteEntity(
                    id        = it.getLong(it.getColumnIndexOrThrow("fav_id")),
                    listingId = it.getLong(it.getColumnIndexOrThrow(COL_FAV_LISTING_ID)),
                    note      = it.getString(it.getColumnIndexOrThrow(COL_FAV_NOTE)) ?: ""
                )
                val listing = ListingEntity(
                    id        = it.getLong(it.getColumnIndexOrThrow("listing_id")),
                    title     = it.getString(it.getColumnIndexOrThrow(COL_LISTING_TITLE)),
                    price     = it.getString(it.getColumnIndexOrThrow(COL_LISTING_PRICE)),
                    amenities = it.getString(it.getColumnIndexOrThrow(COL_LISTING_AMENITIES)) ?: "",
                    imageUrl  = it.getString(it.getColumnIndexOrThrow(COL_LISTING_IMAGE_URL)) ?: ""
                )
                result += Pair(fav, listing)
            }
        }
        return result
    }
}
