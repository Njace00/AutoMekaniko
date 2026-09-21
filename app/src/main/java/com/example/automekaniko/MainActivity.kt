package com.example.automekaniko

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class MainActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // ── Header Branding ──────────────────────────────────────────────────
        AppNavigation.setupBrandedTitle(this, findViewById(R.id.appTitle))
        AppNavigation.wire(this)

        // Cards (the visible clickable areas)
        val card3D   = findViewById<CardView>(R.id.card3D)
        val cardLive = findViewById<CardView>(R.id.cardLive)

        // Hidden buttons kept for backward compat — wire them too just in case
        val viewBtn = findViewById<Button>(R.id.viewbtn)
        val liveBtn = findViewById<Button>(R.id.livebtn)

        // Cards
        card3D.setOnClickListener   { go(GuidesActivity::class.java) }
        cardLive.setOnClickListener { go(OBDActivity::class.java) }

        // Hidden buttons (fallback)
        viewBtn.setOnClickListener { go(GuidesActivity::class.java) }
        liveBtn.setOnClickListener { go(OBDActivity::class.java) }

    }

    private fun <T : Any> go(target: Class<T>) {
        startActivity(Intent(this, target).apply {
            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        })
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
