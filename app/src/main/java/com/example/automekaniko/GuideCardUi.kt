package com.example.automekaniko

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity

data class GuideMiniCardData(
    val label: String,
    val value: String,
    val unit: String,
    @DrawableRes val iconRes: Int
)

object GuideCardUi {

    fun bind(root: View?, data: GuideMiniCardData) {
        if (root == null) return
        root.visibility = View.VISIBLE
        root.findViewById<ImageView>(R.id.cardIcon).setImageResource(data.iconRes)
        root.findViewById<TextView>(R.id.cardLabel).text = data.label
        root.findViewById<TextView>(R.id.cardValue).text = data.value
        root.findViewById<TextView>(R.id.cardUnit).text = data.unit
    }

    fun hide(root: View?) {
        root?.visibility = View.INVISIBLE
    }

    fun bindPreviewCards(
        activity: AppCompatActivity,
        @IdRes viewIds: List<Int>,
        cards: List<GuideMiniCardData>
    ) {
        viewIds.forEachIndexed { index, viewId ->
            val root = activity.findViewById<View>(viewId)
            if (index < cards.size) bind(root, cards[index]) else hide(root)
        }
    }

    fun dtcPreviewCards(): List<GuideMiniCardData> =
        dtcGuides.map { guide ->
            GuideMiniCardData(
                label = "DTC Code",
                value = guide.code,
                unit = guide.name,
                iconRes = R.drawable.ic_star_circle
            )
        }

    fun maintenancePreviewCards(): List<GuideMiniCardData> =
        maintenanceGuides.map { guide ->
            val (value, unit) = when {
                guide.name.contains("VPMC", ignoreCase = true) ->
                    "VPMC" to "Preventive Maintenance Checklist"
                guide.name.contains("Oil", ignoreCase = true) ->
                    "OIL" to "Engine Oil Change Guide"
                else ->
                    guide.name.take(10).uppercase() to guide.name
            }

            GuideMiniCardData(
                label = "Maintenance",
                value = value,
                unit = unit,
                iconRes = if (guide.name.contains("Oil", ignoreCase = true)) {
                    R.drawable.ic_oil_can
                } else {
                    R.drawable.ic_car
                }
            )
        }

    fun homePreviewCards(): List<GuideMiniCardData> {
        val dtc = dtcPreviewCards().take(2)
        val maint = maintenancePreviewCards().take(2)
        return dtc + maint
    }
}
