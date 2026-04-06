package com.symeonchen.wakeupscreen.pages

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.ScBaseActivity
import com.symeonchen.wakeupscreen.compose.FeedbackScreen
import com.symeonchen.wakeupscreen.compose.theme.WakeUpScreenTheme
import com.symeonchen.wakeupscreen.data.ScConstant
import com.symeonchen.wakeupscreen.utils.AppInfoUtils

class FeedbackPageActivity : ScBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WakeUpScreenTheme {
                FeedbackScreen(
                    onBack = { finish() },
                    onSendEmailClick = ::openFeedbackEmail,
                    onCopyEmail = ::copyEmailToClipboard,
                    onContactX = {
                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://x.com/intent/follow?screen_name=riko_time")
                            )
                        )
                    },
                    onContactXiaohongshu = {
                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://www.xiaohongshu.com/user/profile/67348b6a000000001d02e658")
                            )
                        )
                    },
                )
            }
        }
    }

    private fun openFeedbackEmail() {
        var mailBody = ScConstant.DEFAULT_MAIL_BODY
        var mailTitle = ScConstant.DEFAULT_MAIL_HEAD
        try {
            mailBody = AppInfoUtils.getDeviceInfo(this)
            mailTitle = getString(R.string.mail_title)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "plain/text"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(ScConstant.AUTHOR_MAIL))
            putExtra(Intent.EXTRA_SUBJECT, mailTitle)
            putExtra(Intent.EXTRA_TEXT, mailBody)
        }
        startActivity(Intent.createChooser(intent, "Choose your mail app"))
    }

    private fun copyEmailToClipboard() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("email", ScConstant.AUTHOR_MAIL)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, R.string.feedback_email_copied, Toast.LENGTH_SHORT).show()
    }
}
