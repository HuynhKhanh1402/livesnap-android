package dev.vku.livesnap.data.remote.dto.response

import dev.vku.livesnap.domain.model.User

data class FriendSuggestionListDTO(
    val code: Int,
    val message: String,
    val data: Data
) {
    data class Data(
        val suggestions: List<FriendSuggestion>
    )

    data class FriendSuggestion(
        val mutualCount: Int,
        val user: User,
    )
}