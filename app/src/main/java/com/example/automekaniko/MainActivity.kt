package com.example.automekaniko

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.automekaniko.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ── "Auto" white, "Mekaniko" red ──────────────────────────────────────
        val titleText = "AutoMekaniko"
        val spannable = SpannableString(titleText)
        spannable.setSpan(ForegroundColorSpan(0xFFFFFFFF.toInt()), 0, 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(ForegroundColorSpan(0xFFe02020.toInt()), 4, titleText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.appTitle.text = spannable
        AppNavigation.wire(this)

        // Cards (the visible clickable areas)
        binding.card3D.setOnClickListener { go(GuidesActivity::class.java) }
        binding.cardLive.setOnClickListener { go(OBDActivity::class.java) }

        setupHomeGuidePreviews()

        // ── Bottom Navigation Bar ────────────────────────────────────────
        setupBottomNavigation()
    }

    private fun setupHomeGuidePreviews() {
        GuideCardUi.bindPreviewCards(
            this,
            listOf(
                R.id.homeGuidePreview1,
                R.id.homeGuidePreview2,
                R.id.homeGuidePreview3,
                R.id.homeGuidePreview4
            ),
            GuideCardUi.homePreviewCards()
        )
    }

    private fun setupBottomNavigation() {
        binding.hometxt.setOnClickListener {
            Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show()
        }

        binding.livetxt.setOnClickListener {
            go(MAINTAINANCEActivity::class.java)
        }

        binding.connecttxt.setOnClickListener {
            BluetoothManager.connectToELM327(this)
        }

        binding.settingtxt.setOnClickListener {
            Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show()
        }
    }

    private fun <T : Any> go(target: Class<T>) {
        startActivity(Intent(this, target).apply {
            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        })
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}



