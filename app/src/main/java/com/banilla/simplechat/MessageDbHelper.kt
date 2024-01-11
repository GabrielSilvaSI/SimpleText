package com.banilla.simplechat

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

class MessageDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val SQL_CREATE_MESSAGE_ENTRIES =
            "CREATE TABLE ${MessageContract.MessageEntry.TABLE_NAME} (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                    "${MessageContract.MessageEntry.COLUMN_USER_ID} TEXT," +
                    "${MessageContract.MessageEntry.COLUMN_USER_NAME} TEXT," +
                    "${MessageContract.MessageEntry.COLUMN_MESSAGE} TEXT)"

        val SQL_CREATE_KEY_ENTRIES =
            "CREATE TABLE ${KeyContract.KeyEntry.TABLE_NAME} (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                    "${KeyContract.KeyEntry.COLUMN_SYMMETRIC_KEY} TEXT)"


        db.execSQL(SQL_CREATE_MESSAGE_ENTRIES)
        db.execSQL(SQL_CREATE_KEY_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

    }

    companion object {
        const val DATABASE_NAME = "messages.db"
        const val DATABASE_VERSION = 1
    }

    fun insertMessage(userId: String, userName: String, message: String) {
        val db = writableDatabase

        val values = ContentValues().apply {
            put(MessageContract.MessageEntry.COLUMN_USER_ID, userId)
            put(MessageContract.MessageEntry.COLUMN_USER_NAME, userName)
            put(MessageContract.MessageEntry.COLUMN_MESSAGE, message)
        }

        val newRowId = db.insert(MessageContract.MessageEntry.TABLE_NAME, null, values)

    }

    fun getAllMessages(dbHelper: MessageDbHelper): List<String> {
        val db = dbHelper.readableDatabase
        val projection = arrayOf(
            MessageContract.MessageEntry.COLUMN_USER_ID,
            MessageContract.MessageEntry.COLUMN_USER_NAME,
            MessageContract.MessageEntry.COLUMN_MESSAGE
        )

        val cursor = db.query(
            MessageContract.MessageEntry.TABLE_NAME,
            projection,
            null,
            null,
            null,
            null,
            null
        )

        val messages = mutableListOf<String>()

        with(cursor) {
            while (moveToNext()) {
                val userId = getString(getColumnIndexOrThrow(MessageContract.MessageEntry.COLUMN_USER_ID))
                val userName = getString(getColumnIndexOrThrow(MessageContract.MessageEntry.COLUMN_USER_NAME))
                val message = getString(getColumnIndexOrThrow(MessageContract.MessageEntry.COLUMN_MESSAGE))

                messages.add("$userName ($userId): $message")
            }
        }

        cursor.close()

        return messages
    }


}
