package dev.vku.livesnap.data.remote.dto.response

import dev.vku.livesnap.data.remote.dto.SnapDTO

data class SnapsResponse(
    val code: Int,
    val message: String,
    val data: Data
) {
    data class Data(
        val snaps: List<SnapDTO>,
        val pagination: Pagination
    )

    data class Pagination(
        val page: Int,
        val limit: Int,
        val total: Int,
        val totalPages: Int
    )
}

