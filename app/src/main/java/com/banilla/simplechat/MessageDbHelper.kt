package com.banilla.simplechat

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

class MessageDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val SQL_CREATE_ENTRIES =
            "CREATE TABLE ${MessageContract.MessageEntry.TABLE_NAME} (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                    "${MessageContract.MessageEntry.COLUMN_USER_ID} TEXT," +
                    "${MessageContract.MessageEntry.COLUMN_USER_NAME} TEXT," +
                    "${MessageContract.MessageEntry.COLUMN_MESSAGE} TEXT)"

        db.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

    }

    companion object {
        const val DATABASE_NAME = "messages.db"
        const val DATABASE_VERSION = 1
    }
}
