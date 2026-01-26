package indi.dmzz_yyhyy.lightnovelreader.data.plugin

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import indi.dmzz_yyhyy.lightnovelreader.R
import kotlin.system.exitProcess

class DebugReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Toast.makeText(context, context.getString(R.string.plugin_debug_reloading), Toast.LENGTH_SHORT).show()
        restartApp(context)
    }

    private fun restartApp(context: Context) {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        exitProcess(0)
    }
}
