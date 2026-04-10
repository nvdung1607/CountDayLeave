package com.example.countdayleave.model

data class CountdownConfig(
    val milestoneName: String,      // Tên mốc thời gian
    val targetEpochMillis: Long,    // Thời điểm đích (epoch milliseconds)
    val notifyHour: Int,            // Giờ gửi thông báo hằng ngày (0-23)
    val notifyMinute: Int,          // Phút gửi thông báo hằng ngày (0-59)
    val notifyEnabled: Boolean      // Bật/tắt thông báo
)
