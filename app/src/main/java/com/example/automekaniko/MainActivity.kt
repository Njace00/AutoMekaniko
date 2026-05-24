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


        // ── "Auto" white, "Mekaniko" red ──────────────────────────────────────
        val appTitle = findViewById<TextView>(R.id.appTitle)
        val titleText = "AutoMekaniko"
        val spannable = SpannableString(titleText)
        spannable.setSpan(ForegroundColorSpan(0xFFFFFFFF.toInt()), 0, 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(ForegroundColorSpan(0xFFe02020.toInt()), 4, titleText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        appTitle.text = spannable
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
