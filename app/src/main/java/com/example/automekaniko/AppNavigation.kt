package com.example.automekaniko

import android.content.Intent
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

object AppNavigation {

    fun wire(activity: AppCompatActivity) {
        activity.findOptional<View>(R.id.burger)?.setOnClickListener {
            activity.finish()
        }

        activity.findOptional<TextView>(R.id.connecttxt)?.text = "Guides"

        setNav(activity, R.id.hometxt, MainActivity::class.java)
        setNav(activity, R.id.connecttxt, GuidesActivity::class.java)
        setNav(activity, R.id.livetxt, OBDActivity::class.java)
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
