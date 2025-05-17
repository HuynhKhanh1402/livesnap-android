package dev.vku.livesnap.data.remote.dto.response

data class FeedbackHistoryResponse(
    val code: Int,
    val message: String,
    val data: List<FeedbackItem>
) {
    data class FeedbackItem(
        val _id: String,
        val message: String,
        val createdAt: String
    )
} 