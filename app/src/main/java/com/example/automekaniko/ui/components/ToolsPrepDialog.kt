package com.example.automekaniko.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.automekaniko.R
import com.example.automekaniko.ui.theme.ThemeRed

@Composable
fun ToolsPrepDialog(
    difficultyText: String,
    estTimeText: String,
    toolsList: List<String>,
    safetyChecklist: List<String>,
    onReadyStart: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val checkedMap = remember { mutableStateMapOf<Int, Boolean>() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier.fillMaxWidth(0.92f),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.5.dp, ThemeRed),
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_check_white),
                        contentDescription = "Prep Icon",
                        tint = ThemeRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "🧰 Before You Begin",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back_red),
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(20.dp)
                                .rotate(270f)
                        )
                    }
                }

                // Difficulty & Est. Time Status Badge
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Black,
                    border = BorderStroke(1.dp, ThemeRed)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Difficulty: $difficultyText",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "⏱ Est. Time: $estTimeText",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Section 1: Required Tools & Supplies
                Text(
                    text = "REQUIRED TOOLS & SUPPLIES",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.08.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Tool Chips
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    toolsList.chunked(2).forEach { pair ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            pair.forEach { tool ->
                                ToolChip(text = tool, modifier = Modifier.weight(1f))
                            }
                            if (pair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(bottom = 16.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )

                // Section 2: Safety & Pre-Repair Checklist
                Text(
                    text = "SAFETY & PRE-REPAIR CHECKLIST",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.08.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                safetyChecklist.forEachIndexed { index, safetyItem ->
                    val isChecked = checkedMap[index] ?: false
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { checkedMap[index] = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = ThemeRed,
                                uncheckedColor = MaterialTheme.colorScheme.outline
                            ),
                            modifier = Modifier.padding(top = 0.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = safetyItem,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 13.5.sp,
                            lineHeight = 19.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Ready Button
                Button(
                    onClick = {
                        onReadyStart()
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ThemeRed)
                ) {
                    Text(
                        text = "I'm Ready — Start Guide",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ToolChip(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = Color.Black,
        border = BorderStroke(1.dp, ThemeRed)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}
