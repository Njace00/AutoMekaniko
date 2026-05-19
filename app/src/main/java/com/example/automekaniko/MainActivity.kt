package com.example.automekaniko

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Tab bar
        val tab3D  = findViewById<TextView>(R.id.tab3D)
        val tabOBD = findViewById<TextView>(R.id.tabOBD)

        // Cards (the visible clickable areas)
        val card3D   = findViewById<CardView>(R.id.card3D)
        val cardLive = findViewById<CardView>(R.id.cardLive)

        // Hidden buttons kept for backward compat — wire them too just in case
        val viewBtn = findViewById<Button>(R.id.viewbtn)
        val liveBtn = findViewById<Button>(R.id.livebtn)

        // Bottom nav
        val homeTxt    = findViewById<TextView>(R.id.hometxt)
        val liveTxt    = findViewById<TextView>(R.id.livetxt)
        val connectTxt = findViewById<TextView>(R.id.connecttxt)
        val settingTxt = findViewById<TextView>(R.id.settingtxt)

        // Tab bar
        tab3D.setOnClickListener  { go(GuidesActivity::class.java) }
        tabOBD.setOnClickListener { go(OBDActivity::class.java) }

        // Cards
        card3D.setOnClickListener   { go(GuidesActivity::class.java) }
        cardLive.setOnClickListener { go(OBDActivity::class.java) }

        // Hidden buttons (fallback)
        viewBtn.setOnClickListener { go(GuidesActivity::class.java) }
        liveBtn.setOnClickListener { go(OBDActivity::class.java) }

        // Bottom nav
        homeTxt.setOnClickListener    { /* already here */ }
        liveTxt.setOnClickListener    { go(OBDActivity::class.java) }
        connectTxt.setOnClickListener { go(OBDActivity::class.java) }
        settingTxt.setOnClickListener { /* future settings */ }
    }

    private fun <T : Any> go(target: Class<T>) {
        startActivity(Intent(this, target).apply {
            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        })
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}