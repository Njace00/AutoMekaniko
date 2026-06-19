package com.example.automekaniko

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class GuidesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guides)

        val appTitle = findViewById<TextView>(R.id.appTitle)
        val titleText = "AutoMekaniko"
        val spannable = SpannableString(titleText)
        spannable.setSpan(ForegroundColorSpan(0xFFFFFFFF.toInt()), 0, 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(ForegroundColorSpan(0xFFe02020.toInt()), 4, titleText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        appTitle.text = spannable
        AppNavigation.wire(this)

        findViewById<View>(R.id.cardDtc).setOnClickListener {
            go(DtcActivity::class.java)
        }

        findViewById<View>(R.id.cardMaintenance).setOnClickListener {
            go(MAINTAINANCEActivity::class.java)
        }

        findViewById<View>(R.id.backBtn).setOnClickListener {
            finish()
        }

        setupGuidePreviews()
    }

    private fun setupGuidePreviews() {
        GuideCardUi.bindPreviewCards(
            this,
            listOf(
                R.id.cardDtcPreview1,
                R.id.cardDtcPreview2,
                R.id.cardDtcPreview3,
                R.id.cardDtcPreview4
            ),
            GuideCardUi.dtcPreviewCards()
        )

        GuideCardUi.bindPreviewCards(
            this,
            listOf(
                R.id.cardMaintPreview1,
                R.id.cardMaintPreview2,
                R.id.cardMaintPreview3,
                R.id.cardMaintPreview4
            ),
            GuideCardUi.maintenancePreviewCards()
        )
    }

    private fun <T : Any> go(target: Class<T>) {
        val intent = Intent(this, target).apply {
            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        }
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}

