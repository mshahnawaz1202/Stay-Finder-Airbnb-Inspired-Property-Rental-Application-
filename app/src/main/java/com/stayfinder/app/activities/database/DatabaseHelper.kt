package com.stayfinder.app.activities.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.stayfinder.app.models.Booking
import com.stayfinder.app.models.Property
import com.stayfinder.app.models.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DatabaseHelper private constructor(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "stayfinder.db"
        private const val DATABASE_VERSION = 4

        @Volatile
        private var instance: DatabaseHelper? = null

        fun getInstance(context: Context): DatabaseHelper {
            return instance ?: synchronized(this) {
                instance ?: DatabaseHelper(context.applicationContext).also { instance = it }
            }
        }

        const val TABLE_USERS = "users"
        const val U_ID = "id"
        const val U_NAME = "full_name"
        const val U_EMAIL = "email"
        const val U_PASSWORD = "password"
        const val U_ROLE = "role"
        const val U_BIO = "profile_bio"
        const val U_PHONE = "phone_number"
        const val U_DATE_JOINED = "date_joined"
        const val U_AVATAR_COLOR = "avatar_color"
        const val U_PREFS = "preferences"

        const val TABLE_WISHLIST = "wishlist"
        const val W_ID = "id"
        const val W_USER_ID = "user_id"
        const val W_PROP_NAME = "property_name"
        const val W_PROP_LOC = "property_location"
        const val W_PRICE = "price_per_night"
        const val W_RATING = "rating"
        const val W_TYPE = "property_type"
        const val W_API_ID = "api_post_id"
        const val W_DATE_SAVED = "date_saved"

        const val TABLE_SEARCH = "search_history"
        const val S_ID = "id"
        const val S_USER_ID = "user_id"
        const val S_QUERY = "search_query"
        const val S_DATE = "search_date"

        const val TABLE_BOOKINGS = "bookings"
        const val B_ID = "id"
        const val B_USER_ID = "user_id"
        const val B_PROP_NAME = "property_name"
        const val B_PROP_LOC = "property_location"
        const val B_PRICE = "price_per_night"
        const val B_CHECK_IN = "check_in_date"
        const val B_CHECK_OUT = "check_out_date"
        const val B_TOTAL = "total_price"
        const val B_STATUS = "status"
        const val B_DATE = "booking_date"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE $TABLE_USERS (
                $U_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $U_NAME TEXT NOT NULL,
                $U_EMAIL TEXT NOT NULL UNIQUE,
                $U_PASSWORD TEXT NOT NULL,
                $U_ROLE TEXT NOT NULL DEFAULT 'Guest',
                $U_BIO TEXT DEFAULT '',
                $U_PHONE TEXT DEFAULT '',
                $U_DATE_JOINED TEXT NOT NULL,
                $U_AVATAR_COLOR TEXT DEFAULT '#FF385C',
                $U_PREFS TEXT DEFAULT ''
            )
        """)

        db.execSQL("""
            CREATE TABLE $TABLE_WISHLIST (
                $W_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $W_USER_ID INTEGER NOT NULL,
                $W_PROP_NAME TEXT NOT NULL,
                $W_PROP_LOC TEXT NOT NULL,
                $W_PRICE REAL NOT NULL,
                $W_RATING REAL NOT NULL,
                $W_TYPE TEXT NOT NULL,
                $W_API_ID INTEGER,
                $W_DATE_SAVED TEXT NOT NULL
            )
        """)

        db.execSQL("""
            CREATE TABLE $TABLE_SEARCH (
                $S_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $S_USER_ID INTEGER NOT NULL,
                $S_QUERY TEXT NOT NULL,
                $S_DATE TEXT NOT NULL,
                FOREIGN KEY ($S_USER_ID) REFERENCES $TABLE_USERS($U_ID)
            )
        """)

        db.execSQL("""
            CREATE TABLE $TABLE_BOOKINGS (
                $B_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $B_USER_ID INTEGER NOT NULL,
                $B_PROP_NAME TEXT NOT NULL,
                $B_PROP_LOC TEXT NOT NULL,
                $B_PRICE REAL NOT NULL,
                $B_CHECK_IN TEXT NOT NULL,
                $B_CHECK_OUT TEXT NOT NULL,
                $B_TOTAL REAL NOT NULL,
                $B_STATUS TEXT DEFAULT 'Confirmed',
                $B_DATE TEXT NOT NULL,
                FOREIGN KEY ($B_USER_ID) REFERENCES $TABLE_USERS($U_ID)
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_WISHLIST")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SEARCH")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_BOOKINGS")
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    fun insertUser(name: String, email: String, pass: String, role: String, dateJoined: String, avatarColor: String): Long {
        val values = ContentValues().apply {
            put(U_NAME, name)
            put(U_EMAIL, email)
            put(U_PASSWORD, pass)
            put(U_ROLE, role)
            put(U_DATE_JOINED, dateJoined)
            put(U_AVATAR_COLOR, avatarColor)
        }
        return writableDatabase.insert(TABLE_USERS, null, values)
    }

    fun getUserByEmail(email: String): User? {
        val cursor = readableDatabase.query(TABLE_USERS, null, "$U_EMAIL=?", arrayOf(email), null, null, null)
        return cursor.use { if (it.moveToFirst()) mapUser(it) else null }
    }

    fun getUserById(id: Long): User? {
        val cursor = readableDatabase.query(TABLE_USERS, null, "$U_ID=?", arrayOf(id.toString()), null, null, null)
        return cursor.use { if (it.moveToFirst()) mapUser(it) else null }
    }

    fun updateUserProfile(id: Long, name: String, bio: String, phone: String): Boolean {
        val values = ContentValues().apply {
            put(U_NAME, name)
            put(U_BIO, bio)
            put(U_PHONE, phone)
        }
        return writableDatabase.update(TABLE_USERS, values, "$U_ID=?", arrayOf(id.toString())) > 0
    }

    fun isEmailExists(email: String): Boolean {
        val cursor = readableDatabase.query(TABLE_USERS, arrayOf(U_ID), "$U_EMAIL=?", arrayOf(email), null, null, null)
        return cursor.use { it.count > 0 }
    }
    
    fun deleteUser(id: Long): Boolean {
        val db = writableDatabase
        db.delete(TABLE_WISHLIST, "$W_USER_ID=?", arrayOf(id.toString()))
        db.delete(TABLE_BOOKINGS, "$B_USER_ID=?", arrayOf(id.toString()))
        db.delete(TABLE_SEARCH, "$S_USER_ID=?", arrayOf(id.toString()))
        return db.delete(TABLE_USERS, "$U_ID=?", arrayOf(id.toString())) > 0
    }

    // WISHLIST METHODS
    fun addToWishlist(userId: Long, property: Property): Long {
        val values = ContentValues().apply {
            put(W_USER_ID, userId)
            put(W_PROP_NAME, property.name)
            put(W_PROP_LOC, property.location)
            put(W_PRICE, property.pricePerNight)
            put(W_RATING, property.rating)
            put(W_TYPE, property.type)
            put(W_API_ID, property.apiPostId)
            put(W_DATE_SAVED, property.dateSaved)
        }
        return writableDatabase.insert(TABLE_WISHLIST, null, values)
    }

    fun getWishlistByUser(userId: Long): List<Property> {
        val list = mutableListOf<Property>()
        val cursor = readableDatabase.query(TABLE_WISHLIST, null, "$W_USER_ID=?", arrayOf(userId.toString()), null, null, "$W_DATE_SAVED DESC")
        cursor.use {
            while (it.moveToNext()) {
                list.add(Property(
                    it.getInt(it.getColumnIndexOrThrow(W_ID)),
                    it.getInt(it.getColumnIndexOrThrow(W_API_ID)),
                    it.getString(it.getColumnIndexOrThrow(W_PROP_NAME)) ?: "",
                    it.getString(it.getColumnIndexOrThrow(W_PROP_LOC)) ?: "",
                    it.getDouble(it.getColumnIndexOrThrow(W_PRICE)),
                    it.getDouble(it.getColumnIndexOrThrow(W_RATING)),
                    it.getString(it.getColumnIndexOrThrow(W_TYPE)) ?: "Entire Stay",
                    it.getLong(it.getColumnIndexOrThrow(W_USER_ID)).toInt(),
                    it.getString(it.getColumnIndexOrThrow(W_DATE_SAVED)) ?: "",
                    true
                ))
            }
        }
        return list
    }

    fun removeFromWishlist(userId: Long, apiPostId: Int): Boolean {
        return writableDatabase.delete(TABLE_WISHLIST, "$W_USER_ID=? AND $W_API_ID=?", arrayOf(userId.toString(), apiPostId.toString())) > 0
    }
    
    fun removeFromWishlistById(wishlistId: Int): Boolean {
        return writableDatabase.delete(TABLE_WISHLIST, "$W_ID=?", arrayOf(wishlistId.toString())) > 0
    }

    fun isPropertyInWishlist(userId: Long, apiPostId: Int): Boolean {
        val cursor = readableDatabase.query(TABLE_WISHLIST, arrayOf(W_ID), "$W_USER_ID=? AND $W_API_ID=?", arrayOf(userId.toString(), apiPostId.toString()), null, null, null)
        return cursor.use { it.count > 0 }
    }

    fun getWishlistCount(userId: Long): Int {
        val cursor = readableDatabase.rawQuery("SELECT COUNT(*) FROM $TABLE_WISHLIST WHERE $W_USER_ID=?", arrayOf(userId.toString()))
        return cursor.use { if (it.moveToFirst()) it.getInt(0) else 0 }
    }

    // SEARCH HISTORY
    fun insertSearchHistory(userId: Long, query: String): Long {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = sdf.format(Date())
        val values = ContentValues().apply {
            put(S_USER_ID, userId)
            put(S_QUERY, query)
            put(S_DATE, date)
        }
        return writableDatabase.insert(TABLE_SEARCH, null, values)
    }

    fun getSearchHistory(userId: Long): List<String> {
        val list = mutableListOf<String>()
        val cursor = readableDatabase.query(TABLE_SEARCH, arrayOf(S_QUERY), "$S_USER_ID=?", arrayOf(userId.toString()), null, null, "$S_DATE DESC")
        cursor.use {
            while (it.moveToNext()) {
                list.add(it.getString(0) ?: "")
            }
        }
        return list
    }

    // BOOKINGS
    fun insertBooking(userId: Long, property: Property, checkIn: String, checkOut: String, total: Double): Long {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = sdf.format(Date())
        val values = ContentValues().apply {
            put(B_USER_ID, userId)
            put(B_PROP_NAME, property.name)
            put(B_PROP_LOC, property.location)
            put(B_PRICE, property.pricePerNight)
            put(B_CHECK_IN, checkIn)
            put(B_CHECK_OUT, checkOut)
            put(B_TOTAL, total)
            put(B_DATE, date)
        }
        return writableDatabase.insert(TABLE_BOOKINGS, null, values)
    }

    fun getBookingsByUser(userId: Long): List<Booking> {
        val list = mutableListOf<Booking>()
        val cursor = readableDatabase.query(TABLE_BOOKINGS, null, "$B_USER_ID=?", arrayOf(userId.toString()), null, null, "$B_DATE DESC")
        cursor.use {
            while (it.moveToNext()) {
                list.add(Booking(
                    it.getInt(it.getColumnIndexOrThrow(B_ID)),
                    it.getLong(it.getColumnIndexOrThrow(B_USER_ID)),
                    it.getString(it.getColumnIndexOrThrow(B_PROP_NAME)) ?: "",
                    it.getString(it.getColumnIndexOrThrow(B_PROP_LOC)) ?: "",
                    it.getDouble(it.getColumnIndexOrThrow(B_PRICE)),
                    it.getString(it.getColumnIndexOrThrow(B_CHECK_IN)) ?: "",
                    it.getString(it.getColumnIndexOrThrow(B_CHECK_OUT)) ?: "",
                    it.getDouble(it.getColumnIndexOrThrow(B_TOTAL)),
                    it.getString(it.getColumnIndexOrThrow(B_STATUS)) ?: "Confirmed",
                    it.getString(it.getColumnIndexOrThrow(B_DATE)) ?: ""
                ))
            }
        }
        return list
    }

    fun getBookingCount(userId: Long): Int {
        val cursor = readableDatabase.rawQuery("SELECT COUNT(*) FROM $TABLE_BOOKINGS WHERE $B_USER_ID=?", arrayOf(userId.toString()))
        return cursor.use { if (it.moveToFirst()) it.getInt(0) else 0 }
    }

    private fun mapUser(cursor: Cursor): User {
        return User(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(U_ID)),
            fullName = cursor.getString(cursor.getColumnIndexOrThrow(U_NAME)) ?: "",
            email = cursor.getString(cursor.getColumnIndexOrThrow(U_EMAIL)) ?: "",
            password = cursor.getString(cursor.getColumnIndexOrThrow(U_PASSWORD)) ?: "",
            role = cursor.getString(cursor.getColumnIndexOrThrow(U_ROLE)) ?: "Guest",
            profileBio = cursor.getString(cursor.getColumnIndexOrThrow(U_BIO)) ?: "",
            phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow(U_PHONE)) ?: "",
            dateJoined = cursor.getString(cursor.getColumnIndexOrThrow(U_DATE_JOINED)) ?: "",
            avatarColor = cursor.getString(cursor.getColumnIndexOrThrow(U_AVATAR_COLOR)) ?: "#FF385C",
            preferences = cursor.getString(cursor.getColumnIndexOrThrow(U_PREFS)) ?: ""
        )
    }
}
