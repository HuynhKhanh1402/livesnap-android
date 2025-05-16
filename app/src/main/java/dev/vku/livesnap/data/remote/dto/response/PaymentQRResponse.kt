package dev.vku.livesnap.data.remote.dto.response

data class PaymentQRResponse(
    val code: Int,
    val message: String,
    val data: Data
) {
    data class Data(
        val qrCode: String,
        val bank: String,
        val accountNumber: String,
        val accountName: String,
        val amount: Int,
        val transferContent: String
    )
} 