package com.example.automekaniko

import android.content.Intent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

object AppNavigation {

    fun wire(activity: AppCompatActivity) {
        activity.findOptional<View>(R.id.burger)?.setOnClickListener {
            activity.finish()
        }

        (activity.findOptional<View>(R.id.connecttxt) as? TextView)?.text = "Guides"

        setNav(activity, R.id.hometxt, MainActivity::class.java)
        setNav(activity, R.id.connecttxt, GuidesActivity::class.java)
        setNav(activity, R.id.livetxt, OBDActivity::class.java)

        highlight(activity)
    }

    private fun highlight(activity: AppCompatActivity) {
        setNavColor(activity.findOptional<View>(R.id.hometxt), activity is MainActivity)
        setNavColor(activity.findOptional<View>(R.id.livetxt), activity is OBDActivity)
        setNavColor(
            activity.findOptional<View>(R.id.connecttxt),
            activity is GuidesActivity || activity is DtcActivity || activity is MAINTAINANCEActivity
        )
        setNavColor(activity.findOptional<View>(R.id.settingtxt), false)
    }

    private fun setNavColor(view: View?, active: Boolean) {
        val color = if (active) 0xFFe02020.toInt() else 0xFF555555.toInt()
        when (view) {
            is ImageView -> view.setColorFilter(color)
            is TextView -> view.setTextColor(color)
        }
    }

    private fun setNav(activity: AppCompatActivity, viewId: Int, target: Class<out AppCompatActivity>) {
        activity.findOptional<View>(viewId)?.setOnClickListener {
            if (activity::class.java == target) return@setOnClickListener
            activity.startActivity(Intent(activity, target).apply {
                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            })
            activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    private fun <T : View> AppCompatActivity.findOptional(id: Int): T? =
        runCatching { findViewById<T>(id) }.getOrNull()
}
