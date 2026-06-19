package com.example.automekaniko

import android.content.Intent
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

object AppNavigation {

    fun wire(activity: AppCompatActivity) {
        activity.findOptional<View>(R.id.burger)?.setOnClickListener {
            activity.finish()
        }

        setNav(activity, R.id.hometxt, MainActivity::class.java)
        setNav(activity, R.id.connecttxt, GuidesActivity::class.java)
        setNav(activity, R.id.livetxt, OBDActivity::class.java)

        highlight(activity)
    }

    private fun highlight(activity: AppCompatActivity) {
        val homeIcon = activity.findOptional<ImageView>(R.id.hometxt)
        val carIcon = activity.findOptional<ImageView>(R.id.livetxt)
        val bluetoothIcon = activity.findOptional<ImageView>(R.id.connecttxt)
        val settingsIcon = activity.findOptional<ImageView>(R.id.settingtxt)

        val activeColor = 0xFFe02020.toInt()
        val inactiveColor = 0xFF555555.toInt()

        homeIcon?.setColorFilter(if (activity is MainActivity) activeColor else inactiveColor)
        carIcon?.setColorFilter(if (activity is OBDActivity) activeColor else inactiveColor)
        bluetoothIcon?.setColorFilter(
            if (activity is GuidesActivity || activity is DtcActivity || activity is DtcCodesListingActivity)
                activeColor else inactiveColor
        )
        settingsIcon?.setColorFilter(inactiveColor)
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
        findViewById<T>(id)
}
