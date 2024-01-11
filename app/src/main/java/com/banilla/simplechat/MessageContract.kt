package com.banilla.simplechat

import android.provider.BaseColumns

object MessageContract {
    class MessageEntry : BaseColumns {
        companion object {
            const val TABLE_NAME = "messages"
            const val COLUMN_USER_ID = "user_id"
            const val COLUMN_USER_NAME = "user_name"
            const val COLUMN_MESSAGE = "message"
        }
    }
}
