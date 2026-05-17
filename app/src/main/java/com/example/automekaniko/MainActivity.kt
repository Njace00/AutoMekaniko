package com.example.automekaniko

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Top-bar tabs
        val tab3D  = findViewById<TextView>(R.id.tab3D)
        val tabOBD = findViewById<TextView>(R.id.tabOBD)

        // Centre buttons
        val viewBtn = findViewById<Button>(R.id.viewbtn)
        val liveBtn = findViewById<Button>(R.id.livebtn)

        // Bottom-nav labels (optional — wire these up however you like later)
        val homeTxt    = findViewById<TextView>(R.id.hometxt)
        val liveTxt    = findViewById<TextView>(R.id.livetxt)
        val connectTxt = findViewById<TextView>(R.id.connecttxt)
        val settingTxt = findViewById<TextView>(R.id.settingtxt)

        // ── Tab bar ───────────────────────────────────────────────────────────
        // MainActivity is the "home" screen; neither tab is active here.
        // Tap either tab to jump straight to that screen.
        tab3D.setOnClickListener  { go(GuidesActivity::class.java) }
        tabOBD.setOnClickListener { go(OBDActivity::class.java) }

        // ── Centre buttons ────────────────────────────────────────────────────
        // "3D View" button → Guides screen first, then user picks DTC or Maintenance
        viewBtn.setOnClickListener { go(GuidesActivity::class.java) }
        liveBtn.setOnClickListener { go(OBDActivity::class.java) }

        // ── Bottom nav ────────────────────────────────────────────────────────
        homeTxt.setOnClickListener    { /* already here */ }
        liveTxt.setOnClickListener    { go(OBDActivity::class.java) }
        connectTxt.setOnClickListener { go(OBDActivity::class.java) }
        settingTxt.setOnClickListener { /* wire settings screen later */ }
    }

    /** Launches [target] with a fade transition. */
    private fun <T : Any> go(target: Class<T>) {
        startActivity(Intent(this, target))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}