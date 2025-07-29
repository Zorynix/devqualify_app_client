package com.diploma.work.data.models

data class SendFeedbackRequest(
    val userId: Long,
    val subject: String,
    val body: String
)

data class SendFeedbackResponse(
    val success: Boolean,
    val message: String
)
