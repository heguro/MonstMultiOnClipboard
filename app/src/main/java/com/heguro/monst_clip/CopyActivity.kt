package com.heguro.monst_clip

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast

class CopyActivity : AppCompatActivity(), SelectMonstAppFragment.SelectIntentListener {
    companion object {
        private const val APP_NOT_FOUND = 0
        private const val APP_STARTED = 1
        private const val APP_SELECTING = 2
    }
    private val context = this
    private val settings = SettingsWrapper(context)

    private fun showToast(text: String) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show()
    }
    private fun copyText(text: String) {
        val clipMan: ClipboardManager =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val copyStr =
            settings.getStr(SettingsWrapper.Key.TextBeforeInviteMessage) +
                    text +
                    settings.getStr(SettingsWrapper.Key.TextAfterInviteMessage)
        clipMan.setPrimaryClip(ClipData.newPlainText("", copyStr))
        showToast("${getString(R.string.ui_message_copied)}: $copyStr")
    }

    private val monstAppIntents = mutableListOf<Intent>()

    private fun openMonstApp(inviteTarget: String, multiParam: String): Int {

        val host = when {
            inviteTarget == "stage" -> "joingame"
            else -> "join$inviteTarget"
        }

        val pkgMan = context.packageManager

        val monstIntentForQuerying = Intent(Intent.ACTION_VIEW)
        monstIntentForQuerying.data =
            Uri.parse("monsterstrike-app://$host/?$multiParam")
        val packageMatchAllFlag = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ->
                PackageManager.MATCH_ALL
            else ->
                PackageManager.MATCH_DEFAULT_ONLY
        }
        val monstAppActivities =
            pkgMan.queryIntentActivities(monstIntentForQuerying, packageMatchAllFlag)

        monstAppActivities.forEach {
            if (it.activityInfo.packageName == context.packageName) return@forEach
            val itIntent = Intent(monstIntentForQuerying)
            itIntent.`package` = it.activityInfo.packageName
            monstAppIntents.add(itIntent)
        }

        return if (monstAppIntents.isEmpty()) {
            showToast(getString(R.string.ui_message_monst_app_not_found))
            APP_NOT_FOUND
        } else {
            if (monstAppIntents.size > 1) {
                val selections = monstAppIntents.map { intent ->
                    intent.`package`
                        ?.replace("^\\w{2}\\.\\w{2}\\.\\w{4}\\.".toRegex(), "") ?: ""
                }.toTypedArray()
                SelectMonstAppFragment().newInstance(selections)?.show(supportFragmentManager, "")
                APP_SELECTING
            } else {
                startActivity(monstAppIntents[0])
                APP_STARTED
            }
        }
    }
    private fun uriToMultiParam(uri: Uri): String {
        val params = mutableListOf<String>()
        uri.queryParameterNames?.forEach {
            val key = when (it) { "pass_code" -> "join" else -> it }
            if (key != "target") {
                val value = uri.getQueryParameter(it) ?: ""
                val keyAndValue = "$key=$value"
                params.add(keyAndValue)
            }
        }
        return params.joinToString("&")
    }
    private fun uriToInviteTarget(uri: Uri): String {
        return (
                uri.getQueryParameter("target")
                    ?: when {
                        uri.getQueryParameter("g") != null -> "gacha"
                        else -> "stage"
                    }
                )
    }

    override fun onSelectIntent(indexes: MutableList<Int>?) {
        indexes?.forEachIndexed { listIndex, selectedIndex ->
            val wait = when {
                listIndex != 0 -> settings.getInt(SettingsWrapper.Key.WaitTimeBetweenStartMultipleApp).toLong()
                else -> 0L
            }
            if (monstAppIntents.size > selectedIndex) {
                if (listIndex != 0) Thread.sleep(wait)
                startActivity(monstAppIntents[selectedIndex])
            }
        }
        context.finish()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val openedIntent: Intent = intent
        val uri: Uri = openedIntent.data ?: run {
            showToast(getString(R.string.ui_message_invalid_intent))
            this.finish()
            return
        }

        settings.checkUpdate()

        val scheme: String = uri.scheme ?: ""
        val path: String = uri.path ?: ""
        val query: String = uri.query ?: ""
        var openStat: Int? = null

        if (scheme == "line") {

            // create multiParam
            val regexToSeparateMsg = "([\\s\\S]*?)(https://static\\.monster-strike\\.com/line/[?&=\\w]+)[\\s\\S]*".toRegex()

            val lineMsg: String = path.replace("/text/", "")
            val inviteUrl: String = lineMsg.replace(regexToSeparateMsg, "$2")
            val inviteUri = Uri.parse(inviteUrl) ?: run {
                showToast(getString(R.string.ui_message_invalid_intent))
                this.finish()
                return
            }
            val inviteTarget = uriToInviteTarget(inviteUri)
            val multiParam = uriToMultiParam(inviteUri)

            // copy
            val omitType = settings.getStr(SettingsWrapper.Key.ShortenInviteMessage)
            copyText(
                when (omitType) {
                    "url" -> inviteUrl
                    "short" -> lineMsg.replace(regexToSeparateMsg, "$1$2")
                    else -> lineMsg
                }
            )

            // open monst app
            openStat = openMonstApp(inviteTarget, multiParam)
            if (openStat == APP_NOT_FOUND) {
                copyText(inviteUrl)
            }

        } else if ((scheme == "monsterstrike-app" || scheme == "https") && query != "") {

            // check multiParam
            val inviteTarget = uriToInviteTarget(uri)
            val multiParam = uriToMultiParam(uri)
            val inviteUrl = "https://static.monster-strike.com/line/?target=$inviteTarget&$multiParam"

            // monsterstrike-app: copy
            if (scheme == "monsterstrike-app") {
                copyText(inviteUrl)
            }

            // https: open monst app
            if (scheme == "https") {
                openStat = openMonstApp(inviteTarget, multiParam)
                if (openStat == APP_NOT_FOUND) {
                    copyText(inviteUrl)
                }
            }
        } else {
            showToast(getString(R.string.ui_message_invalid_intent))
        }

        if (openStat != APP_SELECTING) this.finish()
    }
}
