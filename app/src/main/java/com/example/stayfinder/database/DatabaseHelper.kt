package com.example.stayfinder.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "airbnb_clone.db"
        private const val DATABASE_VERSION = 2

        const val TABLE_BOOKINGS = "bookings"
        const val TABLE_FAVORITES = "favorites"

        const val COL_FAV_ID = "id"
        const val COL_FAV_PROPERTY_ID = "property_id"
        const val COL_FAV_PROPERTY_NAME = "property_name"
        const val COL_FAV_LOCATION = "location"
        const val COL_FAV_PRICE = "price_per_night"
        const val COL_FAV_RATING = "rating"

        const val COL_BOOK_ID = "id"
        const val COL_BOOK_PROPERTY_ID = "property_id"
        const val COL_BOOK_PROPERTY_NAME = "property_name"
        const val COL_BOOK_CHECK_IN = "check_in_date"
        const val COL_BOOK_CHECK_OUT = "check_out_date"
        const val COL_BOOK_GUEST_NAME = "guest_name"
        const val COL_BOOK_TOTAL_PRICE = "total_price"

        private const val CREATE_TABLE_FAVORITES = ("CREATE TABLE " + TABLE_FAVORITES + "("
                + COL_FAV_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_FAV_PROPERTY_ID + " TEXT UNIQUE,"
                + COL_FAV_PROPERTY_NAME + " TEXT,"
                + COL_FAV_LOCATION + " TEXT,"
                + COL_FAV_PRICE + " TEXT,"
                + COL_FAV_RATING + " TEXT" + ")")

        private const val CREATE_TABLE_BOOKINGS = ("CREATE TABLE " + TABLE_BOOKINGS + "("
                + COL_BOOK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_BOOK_PROPERTY_ID + " TEXT,"
                + COL_BOOK_PROPERTY_NAME + " TEXT,"
                + COL_BOOK_CHECK_IN + " TEXT,"
                + COL_BOOK_CHECK_OUT + " TEXT,"
                + COL_BOOK_GUEST_NAME + " TEXT,"
                + COL_BOOK_TOTAL_PRICE + " TEXT" + ")")
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("PRAGMA foreign_keys=ON;")
        db.execSQL(CREATE_TABLE_FAVORITES)
        db.execSQL(CREATE_TABLE_BOOKINGS)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_BOOKINGS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FAVORITES")
        onCreate(db)
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }
}
