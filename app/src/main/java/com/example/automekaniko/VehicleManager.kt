package com.example.automekaniko

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

data class VehicleProfile(
    val id: String,
    val name: String,
    val engine: String,
    val displacement: String,
    val cylinders: Int,
    val oilCapacity: String,
    val recommendedOil: String,
    val batterySize: String,
    val defaultRedline: Int
)

object VehicleManager {

    const val PREFS_NAME = "automekaniko_settings"
    const val KEY_ACTIVE_VEHICLE_ID = "active_vehicle_id"

    val VIOS = VehicleProfile(
        id = "vios_1_3",
        name = "Toyota Vios 1.3L",
        engine = "1.3L 1NR-FE Dual VVT-i",
        displacement = "1329 cc",
        cylinders = 4,
        oilCapacity = "3.5 Liters",
        recommendedOil = "0W-20 / 5W-30 Full Synthetic",
        batterySize = "NS40L / 35",
        defaultRedline = 6200
    )

    val WIGO = VehicleProfile(
        id = "wigo_1_0",
        name = "Toyota Wigo 1.0L",
        engine = "1.0L 1KR-VE VVT-i",
        displacement = "998 cc",
        cylinders = 3,
        oilCapacity = "3.1 Liters",
        recommendedOil = "0W-20 Full Synthetic",
        batterySize = "40B19L",
        defaultRedline = 6000
    )

    val ALL_VEHICLES = listOf(VIOS, WIGO)

    fun getActiveVehicle(context: Context): VehicleProfile {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val id = prefs.getString(KEY_ACTIVE_VEHICLE_ID, VIOS.id)
        return ALL_VEHICLES.find { it.id == id } ?: VIOS
    }

    fun setActiveVehicle(context: Context, vehicle: VehicleProfile) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_ACTIVE_VEHICLE_ID, vehicle.id).apply()
    }

    fun showSelectorDialog(activity: AppCompatActivity, onVehicleChanged: (VehicleProfile) -> Unit) {
        val current = getActiveVehicle(activity)
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_vehicle_selector, null)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(activity)
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val cardVios = dialogView.findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardOptionVios)
        val cardWigo = dialogView.findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardOptionWigo)
        val rbVios = dialogView.findViewById<RadioButton>(R.id.rbVios)
        val rbWigo = dialogView.findViewById<RadioButton>(R.id.rbWigo)

        val density = activity.resources.displayMetrics.density
        val redColor = ContextCompat.getColor(activity, R.color.theme_red)
        val dividerColor = ContextCompat.getColor(activity, R.color.divider)

        fun updateUI(selectedId: String) {
            rbVios.isChecked = (selectedId == VIOS.id)
            rbWigo.isChecked = (selectedId == WIGO.id)

            cardVios.strokeColor = if (selectedId == VIOS.id) redColor else dividerColor
            cardVios.strokeWidth = if (selectedId == VIOS.id) (2 * density).toInt() else (1 * density).toInt()

            cardWigo.strokeColor = if (selectedId == WIGO.id) redColor else dividerColor
            cardWigo.strokeWidth = if (selectedId == WIGO.id) (2 * density).toInt() else (1 * density).toInt()
        }

        updateUI(current.id)

        fun selectVehicle(vehicle: VehicleProfile) {
            updateUI(vehicle.id)
            setActiveVehicle(activity, vehicle)
            onVehicleChanged(vehicle)
            dialog.dismiss()
        }

        cardVios.setOnClickListener { selectVehicle(VIOS) }
        cardWigo.setOnClickListener { selectVehicle(WIGO) }

        dialogView.findViewById<View>(R.id.btnCloseDialog).setOnClickListener { dialog.dismiss() }

        dialog.show()
    }
}
