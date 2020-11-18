package com.heguro.monst_clip

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.core.content.pm.PackageInfoCompat
import androidx.preference.*
import kotlin.math.roundToInt

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pref, rootKey)

        val context = activity ?: run {return}

        val pkgMan = context.packageManager
        val pkgName = context.packageName
        val pkgInfo = pkgMan.getPackageInfo(pkgName, 0)

        val verName = pkgInfo.versionName
        val verCode = PackageInfoCompat.getLongVersionCode(pkgInfo)
        val versionPref = findPreference<Preference>("Version")
        if (versionPref != null) versionPref.summary = "$verName ($verCode)"

        val firstTextPref = findPreference<EditTextPreference>(SettingsWrapper.Key.TextBeforeInviteMessage.name)
        val lastTextPref = findPreference<EditTextPreference>(SettingsWrapper.Key.TextAfterInviteMessage.name)
        val textPrefProvider = Preference.SummaryProvider<EditTextPreference> {pref ->
            when {
                pref.text.isBlank() -> getString(R.string.ui_setting_empty_text_before_after_invite_message)
                else -> pref.text
            }
        }
        firstTextPref?.summaryProvider = textPrefProvider
        lastTextPref?.summaryProvider = textPrefProvider

        // if installed multiple monst app
        val monstIntentForQuerying = Intent(Intent.ACTION_VIEW)
        monstIntentForQuerying.data =
            Uri.parse("monsterstrike-app://joingame/")
        val packageMatchAllFlag = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ->
                PackageManager.MATCH_ALL
            else ->
                PackageManager.MATCH_DEFAULT_ONLY
        }
        val monstAppActivities =
            pkgMan.queryIntentActivities(monstIntentForQuerying, packageMatchAllFlag)

        if (monstAppActivities.size >= 2) {
            val waitTimePref =
                findPreference<SeekBarPreference>(SettingsWrapper.Key.WaitTimeBetweenStartMultipleApp.name)
                    ?: throw IllegalStateException("WaitTimeBetweenStartMultipleApp not found")

            waitTimePref.isVisible = true
            waitTimePref.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                if (newValue !is Int) throw IllegalStateException("value of WaitTimeBetweenStartMultipleApp should be Int")
                val fixedValue = (newValue.toFloat() / 250).roundToInt() * 250
                if (fixedValue != newValue) {
                    if (fixedValue != waitTimePref.value)
                        waitTimePref.value = fixedValue
                    false
                } else {
                    true
                }
            }
        }

        val receiveMonstAppPref = findPreference<CheckBoxPreference>(SettingsWrapper.Key.ReceiveMonstAppIntent.name)
        receiveMonstAppPref?.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            val enabledState = when (newValue) {
                true -> PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                else -> PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            }
            pkgMan.setComponentEnabledSetting(
                ComponentName(pkgName, "$pkgName.CopyActivityMonstApp"),
                enabledState,
                PackageManager.DONT_KILL_APP
            )
            true
        }
        val receiveHttpsPref = findPreference<CheckBoxPreference>(SettingsWrapper.Key.ReceiveHttpsIntent.name)
        receiveHttpsPref?.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            val enabledState = when (newValue) {
                true -> PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                else -> PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            }
            pkgMan.setComponentEnabledSetting(
                ComponentName(pkgName, "$pkgName.CopyActivityHttps"),
                enabledState,
                PackageManager.DONT_KILL_APP
            )
            true
        }
    }
}