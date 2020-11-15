package com.cs007s.live.notes.utilities.sharedpreference

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor

class SharedPreferenceHelper private constructor(private val context: Context) :
    SharedPreferencesRepository {
    private val pref: SharedPreferences
    private val editor: Editor

    override var displayWidth: Int
        get() = pref.getInt(KEY_DISPLAY_WIDTH, 0)
        set(width) {
            editor.putInt(KEY_DISPLAY_WIDTH, width)
            editor.commit()
        }

    override var displayHeight: Int
        get() = pref.getInt(KEY_DISPLAY_HEIGHT, 0)
        set(height) {
            editor.putInt(KEY_DISPLAY_HEIGHT, height)
            editor.commit()
        }

    override var lockStatus: Boolean
        get() = pref.getBoolean(KEY_LOCK_STATUS, false)
        set(status) {
            editor.putBoolean(KEY_LOCK_STATUS, status)
            editor.commit()
        }

    companion object {
        private const val PREFERENCE_NAME = "test_notes"
        private const val KEY_DISPLAY_WIDTH = "display_width"
        private const val KEY_DISPLAY_HEIGHT = "display_height"
        private const val KEY_LOCK_STATUS = "lock_status"
        private var instance: SharedPreferenceHelper? = null
        @JvmStatic
        fun getInstance(context: Context): SharedPreferenceHelper? {
            if (instance == null) {
                synchronized(SharedPreferenceHelper::class.java) {
                    if (instance == null) {
                        instance = SharedPreferenceHelper(context)
                    }
                }
            }
            return instance
        }
    }

    /**
     * Constructor takes Context as param
     */
    init {
        val PRIVATE_MODE = 0
        pref = context.getSharedPreferences(
            PREFERENCE_NAME,
            PRIVATE_MODE
        )
        editor = pref.edit()
    }
}