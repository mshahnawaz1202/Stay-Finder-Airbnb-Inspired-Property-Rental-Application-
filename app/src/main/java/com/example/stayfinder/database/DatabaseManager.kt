package com.example.stayfinder.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.stayfinder.models.Booking

class DatabaseManager(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    fun addBooking(booking: Booking): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_BOOK_PROPERTY_ID, booking.propertyId)
            put(DatabaseHelper.COL_BOOK_PROPERTY_NAME, booking.propertyName)
            put(DatabaseHelper.COL_BOOK_CHECK_IN, booking.checkInDate)
            put(DatabaseHelper.COL_BOOK_CHECK_OUT, booking.checkOutDate)
            put(DatabaseHelper.COL_BOOK_GUEST_NAME, booking.guestName)
            put(DatabaseHelper.COL_BOOK_TOTAL_PRICE, booking.totalPrice)
        }
        val id = db.insert(DatabaseHelper.TABLE_BOOKINGS, null, values)
        db.close()
        return id
    }

    fun getAllBookings(): List<Booking> {
        val bookings = mutableListOf<Booking>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(DatabaseHelper.TABLE_BOOKINGS, null, null, null, null, null, null)

        if (cursor.moveToFirst()) {
            do {
                bookings.add(cursorToBooking(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return bookings
    }

    fun updateBooking(booking: Booking): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_BOOK_CHECK_IN, booking.checkInDate)
            put(DatabaseHelper.COL_BOOK_CHECK_OUT, booking.checkOutDate)
            put(DatabaseHelper.COL_BOOK_GUEST_NAME, booking.guestName)
        }
        val result = db.update(
            DatabaseHelper.TABLE_BOOKINGS,
            values,
            "${DatabaseHelper.COL_BOOK_ID} = ?",
            arrayOf(booking.id.toString())
        )
        db.close()
        return result
    }

    fun deleteBooking(id: Int): Int {
        val db = dbHelper.writableDatabase
        val result = db.delete(
            DatabaseHelper.TABLE_BOOKINGS,
            "${DatabaseHelper.COL_BOOK_ID} = ?",
            arrayOf(id.toString())
        )
        db.close()
        return result
    }

    fun searchBookings(query: String): List<Booking> {
        val bookings = mutableListOf<Booking>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM ${DatabaseHelper.TABLE_BOOKINGS} WHERE ${DatabaseHelper.COL_BOOK_PROPERTY_NAME} LIKE ?",
            arrayOf("%$query%")
        )

        if (cursor.moveToFirst()) {
            do {
                bookings.add(cursorToBooking(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return bookings
    }

    fun sortBookingsByDate(ascending: Boolean): List<Booking> {
        val bookings = mutableListOf<Booking>()
        val db = dbHelper.readableDatabase
        val order = if (ascending) "ASC" else "DESC"
        val cursor = db.rawQuery(
            "SELECT * FROM ${DatabaseHelper.TABLE_BOOKINGS} ORDER BY ${DatabaseHelper.COL_BOOK_CHECK_IN} $order",
            null
        )

        if (cursor.moveToFirst()) {
            do {
                bookings.add(cursorToBooking(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return bookings
    }

    fun addFavorite(property: com.example.stayfinder.Property): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_FAV_PROPERTY_ID, property.id)
            put(DatabaseHelper.COL_FAV_PROPERTY_NAME, property.title)
            put(DatabaseHelper.COL_FAV_LOCATION, property.location)
            put(DatabaseHelper.COL_FAV_PRICE, property.price)
            put(DatabaseHelper.COL_FAV_RATING, property.rating)
        }
        val id = db.insert(DatabaseHelper.TABLE_FAVORITES, null, values)
        db.close()
        return id
    }

    fun isFavorite(propertyId: String): Boolean {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_FAVORITES,
            arrayOf(DatabaseHelper.COL_FAV_ID),
            "${DatabaseHelper.COL_FAV_PROPERTY_ID} = ?",
            arrayOf(propertyId),
            null,
            null,
            null
        )
        val exists = cursor.count > 0
        cursor.close()
        db.close()
        return exists
    }

    fun removeFavorite(propertyId: String): Int {
        val db = dbHelper.writableDatabase
        val result = db.delete(
            DatabaseHelper.TABLE_FAVORITES,
            "${DatabaseHelper.COL_FAV_PROPERTY_ID} = ?",
            arrayOf(propertyId)
        )
        db.close()
        return result
    }

    private fun cursorToBooking(cursor: Cursor): Booking {
        return Booking(
            id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BOOK_ID)),
            propertyId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BOOK_PROPERTY_ID)),
            propertyName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BOOK_PROPERTY_NAME)),
            checkInDate = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BOOK_CHECK_IN)),
            checkOutDate = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BOOK_CHECK_OUT)),
            guestName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BOOK_GUEST_NAME)),
            totalPrice = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BOOK_TOTAL_PRICE))
        )
    }
}
