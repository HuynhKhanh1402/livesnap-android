package dev.vku.livesnap.data.remote.dto.response

import dev.vku.livesnap.data.remote.dto.SnapDTO

data class SnapResponse(
    val code: Int,
    val message: String,
    val data: SnapDTO
)

