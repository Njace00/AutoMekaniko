package com.example.automekaniko.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.automekaniko.R
import com.example.automekaniko.VehicleManager
import com.example.automekaniko.VehicleProfile
import com.example.automekaniko.ui.theme.ThemeRed

@Composable
fun VehicleSelectorDialog(
    currentVehicle: VehicleProfile,
    onVehicleSelected: (VehicleProfile) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedVehicleId by remember { mutableStateOf(currentVehicle.id) }

    Dialog(onDismissRequest = onDismiss) {
        GlassCard(
            modifier = modifier.fillMaxWidth(),
            cornerRadius = 24.dp,
            elevation = 12.dp,
            borderColor = ThemeRed
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Dialog Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🚘 Select Vehicle Profile",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back_red),
                            contentDescription = "Close Dialog",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(20.dp)
                                .rotate(270f)
                        )
                    }
                }

                // Vehicle Options List
                VehicleManager.ALL_VEHICLES.forEach { vehicle ->
                    val isSelected = vehicle.id == selectedVehicleId

                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    selectedVehicleId = vehicle.id
                                    VehicleManager.setActiveVehicle(context, vehicle)
                                    onVehicleSelected(vehicle)
                                    onDismiss()
                                }
                            ),
                        cornerRadius = 16.dp,
                        borderColor = if (isSelected) ThemeRed else MaterialTheme.colorScheme.outline,
                        borderWidth = if (isSelected) 1.5.dp else 1.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_car),
                                contentDescription = vehicle.name,
                                tint = ThemeRed,
                                modifier = Modifier.size(44.dp)
                            )

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = vehicle.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = "${vehicle.engine} • ${vehicle.oilCapacity} Oil",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    selectedVehicleId = vehicle.id
                                    VehicleManager.setActiveVehicle(context, vehicle)
                                    onVehicleSelected(vehicle)
                                    onDismiss()
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = ThemeRed,
                                    unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
