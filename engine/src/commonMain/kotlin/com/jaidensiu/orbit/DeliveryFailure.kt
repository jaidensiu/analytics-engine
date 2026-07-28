package com.jaidensiu.orbit

data class DeliveryFailure(
    val destinationId: String,
    val reason: String,
    val cause: Throwable? = null,
    val timestampMillis: Long,
    val dropped: Boolean,
)
