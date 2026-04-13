package com.example.countdayleave.model

data class CountdownConfig(
    val milestoneName: String,      // Tên mốc thời gian
    val targetEpochMillis: Long,    // Thời điểm đích (epoch milliseconds)
    val notifyTimes: List<NotifyTime>, // Danh sách các giờ gửi thông báo hằng ngày
    val notifyEnabled: Boolean      // Bật/tắt thông báo
)
