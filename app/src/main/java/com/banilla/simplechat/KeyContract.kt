package com.banilla.simplechat

import android.provider.BaseColumns

object KeyContract {
    class KeyEntry : BaseColumns {
        companion object {
            const val TABLE_NAME = "keys"
            const val COLUMN_SYMMETRIC_KEY = "key_id"
        }
    }
}
