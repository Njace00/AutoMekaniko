package com.example.automekaniko

import android.content.Intent
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

object AppNavigation {

    fun wire(activity: AppCompatActivity) {
        applyAppTheme(activity)
        initEdgeToEdge(activity)
        setNav(activity, R.id.navHome, MainActivity::class.java)
        setNav(activity, R.id.navSettings, SettingsActivity::class.java)
        highlight(activity)
    }

    fun setupBrandedTitle(activity: AppCompatActivity, textView: TextView?) {
        if (textView == null) return
        val titleText = "AutoMekaniko"
        val spannable = SpannableString(titleText)
        
        val primaryColor = ContextCompat.getColor(activity, R.color.text_primary)
        val redColor = ContextCompat.getColor(activity, R.color.theme_red)
        
        spannable.setSpan(ForegroundColorSpan(primaryColor), 0, 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(ForegroundColorSpan(redColor), 4, titleText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        
        textView.text = spannable
    }

    private fun applyAppTheme(activity: AppCompatActivity) {
        val prefs = activity.getSharedPreferences(SettingsActivity.PREFS_NAME, android.content.Context.MODE_PRIVATE)
        val theme = prefs.getInt(SettingsActivity.KEY_THEME, SettingsActivity.THEME_SYSTEM)
        val mode = when (theme) {
            SettingsActivity.THEME_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            SettingsActivity.THEME_DARK -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        if (AppCompatDelegate.getDefaultNightMode() != mode) {
            AppCompatDelegate.setDefaultNightMode(mode)
        }
    }

    private fun initEdgeToEdge(activity: AppCompatActivity) {
        activity.enableEdgeToEdge()
        val rootView = activity.findViewById<View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            // Apply padding to Top Bar
            activity.findViewById<View>(R.id.topBar)?.updatePadding(top = systemBars.top)

            // Apply padding to Bottom Bar
            activity.findViewById<View>(R.id.bottomBar)?.updatePadding(bottom = systemBars.bottom)

            insets
        }
    }

    private fun highlight(activity: AppCompatActivity) {
        setNavColor(activity.findOptional<View>(R.id.navHome), activity is MainActivity)
        setNavColor(activity.findOptional<View>(R.id.navSettings), activity is SettingsActivity)
    }

    private fun setNavColor(view: View?, active: Boolean) {
        if (view == null) return
        val color = if (active) 0xFFe02020.toInt() else 0xFF555555.toInt()
        
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                setNavColor(view.getChildAt(i), active)
            }
        }

        when (view) {
            is ImageView -> view.setColorFilter(color)
            is TextView -> {
                view.setTextColor(color)
                view.setTypeface(null, if (active) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
            }
        }
    }

    private fun setNav(activity: AppCompatActivity, viewId: Int, target: Class<out AppCompatActivity>) {
        val navView = activity.findOptional<View>(viewId) ?: return
        ViewAnimationUtils.applyPressScale(navView, 0.92f)
        navView.setOnClickListener {
            if (activity::class.java == target) return@setOnClickListener
            activity.startActivity(Intent(activity, target).apply {
                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            })
            ViewAnimationUtils.overrideActivityTransition(activity, isEntering = true)
        }
    }

    private fun <T : View> AppCompatActivity.findOptional(id: Int): T? =
        runCatching { findViewById<T>(id) }.getOrNull()
}
