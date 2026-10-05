package com.example.automekaniko.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.automekaniko.MAINTAINANCEActivity
import com.example.automekaniko.R
import com.example.automekaniko.SettingsActivity
import com.example.automekaniko.VehicleManager
import com.example.automekaniko.VehicleProfile
import com.example.automekaniko.ui.components.BottomNavBar
import com.example.automekaniko.ui.components.GlassCard
import com.example.automekaniko.ui.components.NavTab
import com.example.automekaniko.ui.components.VehicleSelectorDialog
import com.example.automekaniko.ui.theme.ThemeRed

@Composable
fun SettingsScreen(
    onHomeClick: () -> Unit,
    onBackClick: () -> Unit,
    onThemeChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(SettingsActivity.PREFS_NAME, Context.MODE_PRIVATE) }

    // Persistent Settings State
    var activeVehicle by remember { mutableStateOf(VehicleManager.getActiveVehicle(context)) }
    var selectedTheme by remember { mutableIntStateOf(prefs.getInt(SettingsActivity.KEY_THEME, SettingsActivity.THEME_SYSTEM)) }

    var obdAutoConnect by remember { mutableStateOf(prefs.getBoolean(SettingsActivity.KEY_OBD_AUTO_CONNECT, true)) }
    var redlineAlert by remember { mutableStateOf(prefs.getBoolean(SettingsActivity.KEY_REDLINE_ALERT, true)) }
    var tempAlert by remember { mutableStateOf(prefs.getBoolean(SettingsActivity.KEY_TEMP_ALERT, true)) }

    var useMetric by remember { mutableStateOf(prefs.getBoolean(SettingsActivity.KEY_UNITS_METRIC, true)) }
    var redlineThreshold by remember { mutableIntStateOf(prefs.getInt(SettingsActivity.KEY_REDLINE, 6500)) }
    var keepScreenOn by remember { mutableStateOf(prefs.getBoolean(SettingsActivity.KEY_KEEP_SCREEN_ON, false)) }
    var autoExpand3D by remember { mutableStateOf(prefs.getBoolean(SettingsActivity.KEY_AUTO_EXPAND, false)) }

    // Dialog States
    var showVehicleSelectorDialog by remember { mutableStateOf(false) }
    var showResetChecklistsDialog by remember { mutableStateOf(false) }
    var showClearDtcCacheDialog by remember { mutableStateOf(false) }
    var showRestoreDefaultsDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .height(64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = 12.dp)
                                .size(40.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_back_red),
                                contentDescription = "Back",
                                tint = ThemeRed,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        val annotatedTitle = buildAnnotatedString {
                            withStyle(SpanStyle(color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 22.sp)) {
                                append("Auto")
                            }
                            withStyle(SpanStyle(color = ThemeRed, fontWeight = FontWeight.Bold, fontSize = 22.sp)) {
                                append("Mekaniko")
                            }
                        }

                        Text(text = annotatedTitle, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            bottomBar = {
                BottomNavBar(
                    selectedTab = NavTab.SETTINGS,
                    onTabSelected = { tab ->
                        if (tab == NavTab.HOME) {
                            onHomeClick()
                        }
                    }
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {

                // ── SECTION 1: ACTIVE VEHICLE PROFILE ───────────────────────
                SectionHeader(
                    iconRes = R.drawable.ic_car,
                    title = "ACTIVE VEHICLE PROFILE"
                )

                GlassCard(
                    modifier = Modifier.padding(bottom = 24.dp),
                    borderColor = ThemeRed,
                    borderWidth = 1.5.dp,
                    cornerRadius = 20.dp,
                    elevation = 6.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_car),
                            contentDescription = "Car Profile Image",
                            tint = ThemeRed,
                            modifier = Modifier
                                .size(40.dp)
                                .padding(4.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = activeVehicle.name,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${activeVehicle.engine} • ${activeVehicle.oilCapacity} Oil",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        Button(
                            onClick = { showVehicleSelectorDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = ThemeRed),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Text(
                                text = "Switch",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // ── SECTION 2: APP VISUAL THEME ──────────────────────────
                SectionHeader(
                    iconRes = R.drawable.ic_settings,
                    title = "APP THEME"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeOptionCard(
                        title = "Light",
                        isSelected = selectedTheme == SettingsActivity.THEME_LIGHT,
                        onClick = {
                            selectedTheme = SettingsActivity.THEME_LIGHT
                            prefs.edit().putInt(SettingsActivity.KEY_THEME, SettingsActivity.THEME_LIGHT).apply()
                            onThemeChanged(SettingsActivity.THEME_LIGHT)
                        },
                        modifier = Modifier.weight(1f)
                    )

                    ThemeOptionCard(
                        title = "Dark",
                        isSelected = selectedTheme == SettingsActivity.THEME_DARK,
                        onClick = {
                            selectedTheme = SettingsActivity.THEME_DARK
                            prefs.edit().putInt(SettingsActivity.KEY_THEME, SettingsActivity.THEME_DARK).apply()
                            onThemeChanged(SettingsActivity.THEME_DARK)
                        },
                        modifier = Modifier.weight(1f)
                    )

                    ThemeOptionCard(
                        title = "System",
                        isSelected = selectedTheme == SettingsActivity.THEME_SYSTEM,
                        onClick = {
                            selectedTheme = SettingsActivity.THEME_SYSTEM
                            prefs.edit().putInt(SettingsActivity.KEY_THEME, SettingsActivity.THEME_SYSTEM).apply()
                            onThemeChanged(SettingsActivity.THEME_SYSTEM)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // ── SECTION 3: OBD-II SCANNER PREFERENCES ───────────────
                SectionHeader(
                    iconRes = R.drawable.ic_bluetooth,
                    title = "OBD-II SCANNER PREFERENCES"
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Auto-Connect to OBD Adapter",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Automatically pair with ELM327 on launch",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }

                            CustomSwitch(
                                checked = obdAutoConnect,
                                onCheckedChange = { isChecked ->
                                    obdAutoConnect = isChecked
                                    prefs.edit().putBoolean(SettingsActivity.KEY_OBD_AUTO_CONNECT, isChecked).apply()
                                }
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 14.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    prefs.edit().remove(SettingsActivity.KEY_OBD_MAC).apply()
                                    Toast.makeText(context, "Saved OBD-II Bluetooth adapter cleared.", Toast.LENGTH_SHORT).show()
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Forget Paired OBD-II Adapter",
                                color = ThemeRed,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "Clear ›",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // ── SECTION 4: GAUGE ALERTS & AUDIO WARNINGS ──────────────
                SectionHeader(
                    iconRes = R.drawable.ic_sensors,
                    title = "GAUGE ALERTS & AUDIO WARNINGS"
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Over-RPM Redline Audio Warning",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                            CustomSwitch(
                                checked = redlineAlert,
                                onCheckedChange = { isChecked ->
                                    redlineAlert = isChecked
                                    prefs.edit().putBoolean(SettingsActivity.KEY_REDLINE_ALERT, isChecked).apply()
                                }
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 14.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "High Engine Temp Alert (>105°C)",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                            CustomSwitch(
                                checked = tempAlert,
                                onCheckedChange = { isChecked ->
                                    tempAlert = isChecked
                                    prefs.edit().putBoolean(SettingsActivity.KEY_TEMP_ALERT, isChecked).apply()
                                }
                            )
                        }
                    }
                }

                // ── SECTION 5: MEASUREMENT & DISPLAY ────────────────────
                SectionHeader(
                    iconRes = R.drawable.ic_star_circle,
                    title = "MEASUREMENT & DISPLAY"
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Use Metric Units (km, °C, kPa)",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                            CustomSwitch(
                                checked = useMetric,
                                onCheckedChange = { isChecked ->
                                    useMetric = isChecked
                                    prefs.edit().putBoolean(SettingsActivity.KEY_UNITS_METRIC, isChecked).apply()
                                }
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 14.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "RPM Redline Threshold",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = redlineThreshold.toString(),
                                onValueChange = { input ->
                                    val newVal = input.toIntOrNull() ?: 6500
                                    redlineThreshold = newVal
                                    prefs.edit().putInt(SettingsActivity.KEY_REDLINE, newVal).apply()
                                },
                                modifier = Modifier.width(90.dp),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    color = ThemeRed,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.End,
                                    fontSize = 15.sp
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ThemeRed,
                                    unfocusedBorderColor = Color.Transparent
                                )
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 14.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Keep Screen On During Diagnostics",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                            CustomSwitch(
                                checked = keepScreenOn,
                                onCheckedChange = { isChecked ->
                                    keepScreenOn = isChecked
                                    prefs.edit().putBoolean(SettingsActivity.KEY_KEEP_SCREEN_ON, isChecked).apply()
                                }
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 14.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Auto-Expand 3D Step Instructions",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                            CustomSwitch(
                                checked = autoExpand3D,
                                onCheckedChange = { isChecked ->
                                    autoExpand3D = isChecked
                                    prefs.edit().putBoolean(SettingsActivity.KEY_AUTO_EXPAND, isChecked).apply()
                                }
                            )
                        }
                    }
                }

                // ── SECTION 6: DATA & RESET MANAGEMENT ────────────────────
                SectionHeader(
                    iconRes = R.drawable.ic_check_white,
                    title = "DATA & RESET MANAGEMENT"
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showResetChecklistsDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ThemeRed)
                        ) {
                            Text(
                                text = "Reset Checklist Progress",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = { showClearDtcCacheDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, ThemeRed)
                        ) {
                            Text(
                                text = "Clear Saved DTC Trouble Codes",
                                color = ThemeRed,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = { showRestoreDefaultsDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Text(
                                text = "Restore Default App Preferences",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // ── SECTION 7: ABOUT AUTOMEKANIKO ─────────────────────────
                SectionHeader(
                    iconRes = R.drawable.ic_info_modern,
                    title = "ABOUT AUTOMEKANIKO"
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "AutoMekaniko v1.0.0 Stable",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Your digital 3D automotive diagnostic & maintenance companion.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        // Vehicle Selector Dialog
        if (showVehicleSelectorDialog) {
            VehicleSelectorDialog(
                currentVehicle = activeVehicle,
                onVehicleSelected = { updatedVehicle ->
                    activeVehicle = updatedVehicle
                },
                onDismiss = { showVehicleSelectorDialog = false }
            )
        }

        // Reset Checklist Dialog
        if (showResetChecklistsDialog) {
            AlertDialog(
                onDismissRequest = { showResetChecklistsDialog = false },
                title = { Text(text = "Reset Checklist Progress?") },
                text = { Text(text = "This will clear all completed step checkmarks across all maintenance and diagnostic guides.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val checklistPrefs = context.getSharedPreferences(MAINTAINANCEActivity.PREFS_CHECKLIST, Context.MODE_PRIVATE)
                            checklistPrefs.edit().clear().apply()
                            Toast.makeText(context, "All checklist progress has been reset.", Toast.LENGTH_SHORT).show()
                            showResetChecklistsDialog = false
                        }
                    ) {
                        Text(text = "Reset", color = ThemeRed)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetChecklistsDialog = false }) {
                        Text(text = "Cancel")
                    }
                }
            )
        }

        // Clear DTC Cache Dialog
        if (showClearDtcCacheDialog) {
            AlertDialog(
                onDismissRequest = { showClearDtcCacheDialog = false },
                title = { Text(text = "Clear Saved DTC Codes?") },
                text = { Text(text = "This will clear cached trouble code scan logs.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            Toast.makeText(context, "Saved trouble code logs cleared.", Toast.LENGTH_SHORT).show()
                            showClearDtcCacheDialog = false
                        }
                    ) {
                        Text(text = "Clear", color = ThemeRed)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearDtcCacheDialog = false }) {
                        Text(text = "Cancel")
                    }
                }
            )
        }

        // Restore Defaults Dialog
        if (showRestoreDefaultsDialog) {
            AlertDialog(
                onDismissRequest = { showRestoreDefaultsDialog = false },
                title = { Text(text = "Restore Default Preferences?") },
                text = { Text(text = "Are you sure you want to restore all app settings to factory defaults?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            prefs.edit().clear().apply()
                            obdAutoConnect = true
                            redlineAlert = true
                            tempAlert = true
                            useMetric = true
                            redlineThreshold = 6500
                            keepScreenOn = false
                            autoExpand3D = false
                            selectedTheme = SettingsActivity.THEME_SYSTEM
                            onThemeChanged(SettingsActivity.THEME_SYSTEM)
                            Toast.makeText(context, "All app settings restored to defaults.", Toast.LENGTH_SHORT).show()
                            showRestoreDefaultsDialog = false
                        }
                    ) {
                        Text(text = "Restore", color = ThemeRed)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRestoreDefaultsDialog = false }) {
                        Text(text = "Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun SectionHeader(
    iconRes: Int,
    title: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = title,
            tint = ThemeRed,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            color = ThemeRed,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.1.sp
        )
    }
}

@Composable
private fun ThemeOptionCard(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) ThemeRed else MaterialTheme.colorScheme.outline
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Text(
            text = title,
            color = if (isSelected) ThemeRed else MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp)
        )
    }
}

@Composable
private fun CustomSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = ThemeRed,
            uncheckedThumbColor = Color.White,
            uncheckedTrackColor = MaterialTheme.colorScheme.outline
        )
    )
}
