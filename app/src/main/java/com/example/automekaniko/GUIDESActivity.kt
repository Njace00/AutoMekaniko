package com.example.automekaniko

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class GuidesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guides)

        // ── Header Branding ──────────────────────────────────────────────────
        AppNavigation.setupBrandedTitle(this, findViewById(R.id.appTitle))
        AppNavigation.wire(this)

        findViewById<CardView>(R.id.cardDtc).setOnClickListener {
            go(DtcActivity::class.java)
        }

        findViewById<CardView>(R.id.cardMaintenance).setOnClickListener {
            go(MAINTAINANCEActivity::class.java)
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

