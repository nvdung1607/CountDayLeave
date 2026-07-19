package com.nvdung1607.countdayleave.model

import java.util.UUID

data class CountdownConfig(
    val id: String = UUID.randomUUID().toString(), // ID duy nhất cho từng sự kiện
    val milestoneName: String,      // Tên mốc thời gian
    val targetEpochMillis: Long,    // Thời điểm đích (epoch milliseconds)
    val notifyTimes: List<NotifyTime>, // Danh sách các giờ gửi thông báo hằng ngày
    val notifyEnabled: Boolean      // Bật/tắt thông báo
)

