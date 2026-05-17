package com.example.automekaniko

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class GuidesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guides)

        // ── "Auto" white, "Mekaniko" red ──────────────────────────────────────
        val appTitle = findViewById<TextView>(R.id.appTitle)
        val titleText = "AutoMekaniko"
        val spannable = SpannableString(titleText)
        spannable.setSpan(ForegroundColorSpan(0xFFFFFFFF.toInt()), 0, 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(ForegroundColorSpan(0xFFe02020.toInt()), 4, titleText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        appTitle.text = spannable

        // ── Card clicks ───────────────────────────────────────────────────────
        // DTC Codes → placeholder for future screen
        findViewById<CardView>(R.id.cardDtc).setOnClickListener {
            go(DtcActivity::class.java)
        }

        // Maintenance → 3D viewer with slide checklist
        findViewById<CardView>(R.id.cardMaintenance).setOnClickListener {
            go(MAINTAINANCEActivity::class.java)
        }

        // ── Bottom nav ────────────────────────────────────────────────────────
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            go(MainActivity::class.java)
        }

        findViewById<LinearLayout>(R.id.nav3D).setOnClickListener {
            // Already on this screen — do nothing
        }

        findViewById<LinearLayout>(R.id.navBluetooth).setOnClickListener {
            go(OBDActivity::class.java)
        }

        findViewById<LinearLayout>(R.id.navSettings).setOnClickListener {
            // TODO: go(SettingsActivity::class.java)
        }
    }

    private fun <T : Any> go(target: Class<T>) {
        val intent = Intent(this, target).apply {
            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        }
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}