package com.example.automekaniko.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.automekaniko.R
import com.example.automekaniko.ui.components.BottomNavBar
import com.example.automekaniko.ui.components.BrandedTopBar
import com.example.automekaniko.ui.components.NavTab
import com.example.automekaniko.ui.components.ObdSensorCard
import com.example.automekaniko.ui.components.TutorialOverlay
import com.example.automekaniko.ui.theme.ThemeRed

data class SensorMeta(
    val cardId: Int,
    val label: String,
    val unitMetric: String,
    val unitImperial: String,
    val maxVal: Float,
    val colorLogic: ((Float) -> Color)? = null,
    val onLongClick: (() -> Unit)? = null
)

@Composable
fun ObdScreen(
    sensorValues: Map<Int, String>,
    isConnected: Boolean,
    statusMessage: String,
    isMetric: Boolean,
    rpmRedline: Int,
    onConnectClick: () -> Unit,
    onHomeClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onFuelLevelLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    fun getRpmColor(rpm: Float): Color {
        return when {
            rpm < rpmRedline * 0.45f -> Color(0xFF00E676) // Green
            rpm < rpmRedline * 0.85f -> Color(0xFFFFD600) // Yellow/Amber
            else -> ThemeRed                               // Red
        }
    }

    fun getTempColor(temp: Float): Color {
        return when {
            temp < 40  -> Color(0xFF2196F3) // Blue (Cold)
            temp < 100 -> Color(0xFF00E676) // Green (Normal)
            else -> ThemeRed                // Red (Hot)
        }
    }

    val coreSensors = listOf(
        SensorMeta(R.id.cardRpm, "RPM", "rpm", "rpm", 8000f, ::getRpmColor),
        SensorMeta(R.id.cardSpeed, "Speed", "km/h", "mph", 240f),
        SensorMeta(R.id.cardCoolant, "Coolant Temp", "°C", "°F", 130f, ::getTempColor),
        SensorMeta(R.id.cardThrottle, "Throttle", "%", "%", 100f),
        SensorMeta(R.id.cardLoad, "Engine Load", "%", "%", 100f)
    )

    val liveObdSensors = listOf(
        SensorMeta(R.id.cardMaf, "Mass Air Flow", "g/s", "lb/m", 100f),
        SensorMeta(R.id.cardIat, "Intake Air Temp", "°C", "°F", 100f, ::getTempColor),
        SensorMeta(R.id.cardO2s1, "O2 Sensor 1", "V", "V", 1.2f),
        SensorMeta(R.id.cardO2s2, "O2 Sensor 2", "V", "V", 1.2f),
        SensorMeta(R.id.cardMap, "MAP", "kPa", "psi", 255f),
        SensorMeta(R.id.cardFuelLevel, "Fuel Level", "%", "%", 100f, onLongClick = onFuelLevelLongClick)
    )

    val fuelTrimSensors = listOf(
        SensorMeta(R.id.cardStft, "Short Fuel Trim", "%", "%", 100f),
        SensorMeta(R.id.cardLtft, "Long Fuel Trim", "%", "%", 100f),
        SensorMeta(R.id.cardTiming, "Timing Advance", "°", "°", 60f),
        SensorMeta(R.id.cardBaro, "Baro Pressure", "kPa", "psi", 110f)
    )

    val catalystSensors = listOf(
        SensorMeta(R.id.cardCat1, "Catalyst T1", "°C", "°F", 1000f),
        SensorMeta(R.id.cardCat2, "Catalyst T2", "°C", "°F", 1000f),
        SensorMeta(R.id.cardModVoltage, "Module Voltage", "V", "V", 16f),
        SensorMeta(R.id.cardFuelRate, "Fuel Rate", "L/h", "g/h", 50f)
    )

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                BrandedTopBar(onTutorialClick = null)
            },
            bottomBar = {
                Column {
                    // Connect Control Bar
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isConnected) "● CONNECTED" else "● DISCONNECTED",
                                color = if (isConnected) Color(0xFF00E676) else ThemeRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(end = 10.dp)
                            )

                            Text(
                                text = statusMessage,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 12.dp)
                            )

                            Button(
                                onClick = onConnectClick,
                                modifier = Modifier
                                    .width(120.dp)
                                    .height(38.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ThemeRed)
                            ) {
                                Text(
                                    text = if (isConnected) "DISCONNECT" else "CONNECT",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Bottom Nav Bar
                    BottomNavBar(
                        selectedTab = NavTab.HOME,
                        onTabSelected = { tab ->
                            if (tab == NavTab.HOME) onHomeClick() else if (tab == NavTab.SETTINGS) onSettingsClick()
                        }
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp)
            ) {
                // Category 1: CORE ENGINE DATA
                ObdSectionHeader(title = "CORE ENGINE DATA")
                SensorGrid(
                    sensors = coreSensors,
                    sensorValues = sensorValues,
                    isMetric = isMetric
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category 2: LIVE OBD-II SENSORS
                ObdSectionHeader(title = "LIVE OBD-II SENSORS")
                SensorGrid(
                    sensors = liveObdSensors,
                    sensorValues = sensorValues,
                    isMetric = isMetric
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category 3: FUEL TRIM & TIMING
                ObdSectionHeader(title = "FUEL TRIM & TIMING")
                SensorGrid(
                    sensors = fuelTrimSensors,
                    sensorValues = sensorValues,
                    isMetric = isMetric
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category 4: CATALYST & ELECTRICAL
                ObdSectionHeader(title = "CATALYST & ELECTRICAL")
                SensorGrid(
                    sensors = catalystSensors,
                    sensorValues = sensorValues,
                    isMetric = isMetric
                )
            }
        }

        // Onboarding Tutorial Overlay
        TutorialOverlay()
    }
}

@Composable
private fun ObdSectionHeader(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(12.dp)
                .background(ThemeRed)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.1.sp
        )
    }
}

@Composable
private fun SensorGrid(
    sensors: List<SensorMeta>,
    sensorValues: Map<Int, String>,
    isMetric: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        sensors.chunked(2).forEach { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                pair.forEach { meta ->
                    val valStr = sensorValues[meta.cardId] ?: "—"
                    val unitStr = if (isMetric) meta.unitMetric else meta.unitImperial

                    ObdSensorCard(
                        label = meta.label,
                        value = valStr,
                        unit = unitStr,
                        maxVal = meta.maxVal,
                        colorLogic = meta.colorLogic,
                        onLongClick = meta.onLongClick,
                        modifier = Modifier.weight(1f)
                    )
                }
                // Fill space if row has an odd number of items
                if (pair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
