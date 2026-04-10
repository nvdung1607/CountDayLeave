package com.example.countdayleave.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.countdayleave.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    title: String = "Chọn giờ",
    onTimeSelected: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(AppTheme.colors.surfaceCard)
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.colors.textPrimary,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = AppTheme.colors.surfaceElevated,
                        clockDialSelectedContentColor = AppTheme.colors.textPrimary,
                        clockDialUnselectedContentColor = AppTheme.colors.textSecondary,
                        selectorColor = AppTheme.colors.accentPurple,
                        containerColor = AppTheme.colors.surfaceCard,
                        timeSelectorSelectedContainerColor = AppTheme.colors.accentPurple,
                        timeSelectorUnselectedContainerColor = AppTheme.colors.surfaceElevated,
                        timeSelectorSelectedContentColor = AppTheme.colors.textPrimary,
                        timeSelectorUnselectedContentColor = AppTheme.colors.textSecondary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Hủy", color = AppTheme.colors.textSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = {
                        onTimeSelected(timePickerState.hour, timePickerState.minute)
                        onDismiss()
                    }) {
                        Text("Chọn", color = AppTheme.colors.accentPurple, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
