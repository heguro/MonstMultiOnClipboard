package com.heguro.monst_clip

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

class SettingsWrapper(private val context: Context) {
    companion object {
        private const val SETTING_VERSION = 1
    }
    enum class Key(def: Any) {
        SettingVersion(0),
        ReceiveHttpsIntent(true),
        ReceiveMonstAppIntent(false),
        TextBeforeInviteMessage(""),
        TextAfterInviteMessage(""),
        ShortenInviteMessage("default"),
        WaitTimeBetweenStartMultipleApp(2500),
        ;

        val defaultBool: Boolean?
        val defaultInt: Int?
        val defaultStr: String?

        init {
            when (def) {
                is Boolean -> {
                    defaultBool = def
                    defaultInt = null
                    defaultStr = null
                }
                is Int -> {
                    defaultBool = null
                    defaultInt = def
                    defaultStr = null
                }
                is String -> {
                    defaultBool = null
                    defaultInt = null
                    defaultStr = def
                }
                else -> throw IllegalArgumentException()
            }
        }
    }

    fun checkUpdate() {
        val currentSettingVersion = getInt(Key.SettingVersion)
        if (currentSettingVersion == SETTING_VERSION) return
        if (currentSettingVersion == 0 || currentSettingVersion > SETTING_VERSION) {
            // reset settings
            Key.values().forEach {
                when {
                    it.defaultBool != null -> setBool(it, it.defaultBool)
                    it.defaultInt  != null -> setInt (it, it.defaultInt )
                    it.defaultStr  != null -> setStr (it, it.defaultStr )
                }
            }
        }
        setInt(Key.SettingVersion, SETTING_VERSION)
    }

    fun setBool(key: Key, value: Boolean) {
        if (key.defaultBool == null)
            throw IllegalArgumentException("Cannot set Boolean to key: " + key.name)
        val pref = getPref()
        pref.edit()
            .putBoolean(key.name, value)
            .apply()
    }
    fun setInt(key: Key, value: Int) {
        if (key.defaultInt == null)
            throw IllegalArgumentException("Cannot set Int to key: " + key.name)
        val pref = getPref()
        pref.edit()
            .putInt(key.name, value)
            .apply()
    }
    fun setStr(key: Key, value: String) {
        if (key.defaultStr == null)
            throw IllegalArgumentException("Cannot set String to key: " + key.name)
        val pref = getPref()
        pref.edit()
            .putString(key.name, value)
            .apply()
    }
    fun getBool(key: Key): Boolean {
        if (key.defaultBool == null)
            throw IllegalArgumentException("Cannot get Boolean from key: " + key.name)
        val pref = getPref()
        return pref.getBoolean(key.name, key.defaultBool)
    }
    fun getInt(key: Key): Int {
        if (key.defaultInt == null)
            throw IllegalArgumentException("Cannot get String from key: " + key.name)
        val pref = getPref()
        return pref.getInt(key.name, key.defaultInt)
    }
    fun getStr(key: Key): String {
        if (key.defaultStr == null)
            throw IllegalArgumentException("Cannot get String from key: " + key.name)
        val pref = getPref()
        return pref.getString(key.name, key.defaultStr) ?: throw IllegalArgumentException()
    }
    private fun getPref(): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }
}