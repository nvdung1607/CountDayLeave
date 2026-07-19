package com.nvdung1607.countdayleave.ui.components

import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
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
import com.nvdung1607.countdayleave.ui.theme.*
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDatePickerDialog(
    initialMillis: Long?,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val todayStart = remember {
        Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
            val local = Calendar.getInstance()
            set(Calendar.YEAR, local.get(Calendar.YEAR))
            set(Calendar.DAY_OF_YEAR, local.get(Calendar.DAY_OF_YEAR))
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis >= todayStart
            }
            override fun isSelectableYear(year: Int): Boolean {
                return year >= Calendar.getInstance().get(Calendar.YEAR)
            }
        }
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
            // Chỉ cho phép chọn ngày từ hôm nay trở đi (block ngày quá khứ)
            dateValidator = { utcDateMillis ->
                val todayStart = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
                    val local = Calendar.getInstance()
                    set(Calendar.YEAR, local.get(Calendar.YEAR))
                    set(Calendar.DAY_OF_YEAR, local.get(Calendar.DAY_OF_YEAR))
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                utcDateMillis >= todayStart
            },
            modifier = Modifier.animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ),
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
                todayDateBorderColor = AppTheme.colors.accentBlue,
                disabledDayContentColor = AppTheme.colors.textMuted.copy(alpha = 0.3f)
            )
        )
    }
}

