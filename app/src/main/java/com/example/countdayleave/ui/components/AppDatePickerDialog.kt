package com.example.countdayleave.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.countdayleave.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDatePickerDialog(
    initialMillis: Long?,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { onDateSelected(it) }
                onDismiss()
            }) {
                Text("Chọn", color = AppTheme.colors.accentPurple)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy", color = AppTheme.colors.textSecondary)
            }
        },
        colors = DatePickerDefaults.colors(
            containerColor = AppTheme.colors.surfaceCard
        )
    ) {
        DatePicker(
            state = datePickerState,
            modifier = Modifier.animateContentSize(),
            colors = DatePickerDefaults.colors(
                containerColor = AppTheme.colors.surfaceCard,
                titleContentColor = AppTheme.colors.textPrimary,
                headlineContentColor = AppTheme.colors.accentPurple,
                weekdayContentColor = AppTheme.colors.textSecondary,
                subheadContentColor = AppTheme.colors.textSecondary,
                navigationContentColor = AppTheme.colors.textPrimary,
                yearContentColor = AppTheme.colors.textPrimary,
                currentYearContentColor = AppTheme.colors.accentPurple,
                selectedYearContentColor = AppTheme.colors.textPrimary,
                selectedYearContainerColor = AppTheme.colors.accentPurple,
                dayContentColor = AppTheme.colors.textPrimary,
                selectedDayContentColor = AppTheme.colors.textPrimary,
                selectedDayContainerColor = AppTheme.colors.accentPurple,
                todayContentColor = AppTheme.colors.accentBlue,
                todayDateBorderColor = AppTheme.colors.accentBlue
            )
        )
    }
}
