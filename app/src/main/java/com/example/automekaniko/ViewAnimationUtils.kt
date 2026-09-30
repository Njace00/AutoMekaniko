package com.example.automekaniko

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

object ViewAnimationUtils {

    /**
     * Attaches tactile spring press scale animation to any view on touch down / up.
     */
    @SuppressLint("ClickableViewAccessibility")
    fun applyPressScale(view: View?, clickScale: Float = 0.96f) {
        if (view == null) return
        view.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.animate().cancel()
                    v.animate()
                        .scaleX(clickScale)
                        .scaleY(clickScale)
                        .setDuration(90L)
                        .setInterpolator(DecelerateInterpolator())
                        .start()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.animate().cancel()
                    v.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(160L)
                        .setInterpolator(OvershootInterpolator(1.8f))
                        .start()
                }
            }
            false
        }
    }

    /**
     * Applies press scale animation to multiple views at once.
     */
    fun applyPressScaleToAll(vararg views: View?, clickScale: Float = 0.96f) {
        views.forEach { applyPressScale(it, clickScale) }
    }

    /**
     * Staggered entrance cascade animation for views when a screen opens.
     */
    fun animateEntranceCascade(
        views: List<View?>,
        startDelayMs: Long = 40L,
        stepDelayMs: Long = 50L,
        translationYDp: Float = 30f
    ) {
        views.filterNotNull().forEachIndexed { index, view ->
            val density = view.resources.displayMetrics.density
            val startTranslationY = translationYDp * density
            view.alpha = 0f
            view.translationY = startTranslationY

            view.animate().cancel()
            view.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(startDelayMs + index * stepDelayMs)
                .setDuration(320L)
                .setInterpolator(DecelerateInterpolator(1.5f))
                .start()
        }
    }

    /**
     * Pop-bounce scale animation for checkmarks or completed action icons.
     */
    fun animateCheckmarkBounce(view: View?) {
        if (view == null) return
        view.scaleX = 0.7f
        view.scaleY = 0.7f
        view.animate().cancel()
        view.animate()
            .scaleX(1.0f)
            .scaleY(1.0f)
            .setDuration(220L)
            .setInterpolator(OvershootInterpolator(2.5f))
            .start()
    }

    /**
     * Smoothly interpolates text values for telemetry gauges or numerical displays.
     */
    fun animateNumberText(
        textView: TextView?,
        fromVal: Float,
        toVal: Float,
        formatString: String = "%.0f",
        durationMs: Long = 280L
    ) {
        if (textView == null) return
        val animator = ValueAnimator.ofFloat(fromVal, toVal).apply {
            duration = durationMs
            interpolator = DecelerateInterpolator()
            addUpdateListener { anim ->
                val curr = anim.animatedValue as Float
                textView.text = String.format(java.util.Locale.getDefault(), formatString, curr)
            }
        }
        animator.start()
    }

    /**
     * Applies custom activity slide transition animations.
     */
    fun overrideActivityTransition(activity: AppCompatActivity, isEntering: Boolean = true) {
        if (isEntering) {
            activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        } else {
            activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }

    private fun Float.toPx(context: Context): Float = this * context.resources.displayMetrics.density
}
