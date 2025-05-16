package dev.vku.livesnap.ui.screen.checkout

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.vku.livesnap.data.repository.UsersRepository
import dev.vku.livesnap.domain.mapper.toDomain
import dev.vku.livesnap.ui.util.LoadingResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Base64
import javax.inject.Inject

data class PaymentInfo(
    val bank: String,
    val accountNumber: String,
    val accountName: String,
    val amount: Int,
    val transferContent: String
)

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val userRepository: UsersRepository,
) : ViewModel() {

    private val _qrCodeResult = MutableStateFlow<LoadingResult<Bitmap>>(LoadingResult.Idle)
    val qrCodeResult: StateFlow<LoadingResult<Bitmap>> = _qrCodeResult

    private val _showSuccessDialog = MutableStateFlow(false)
    val showSuccessDialog: StateFlow<Boolean> = _showSuccessDialog

    private val _paymentInfo = MutableStateFlow<PaymentInfo?>(null)
    val paymentInfo: StateFlow<PaymentInfo?> = _paymentInfo

    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchPaymentQR() {
        viewModelScope.launch {
            _qrCodeResult.value = LoadingResult.Loading
            try {
                val response = userRepository.getPaymentQR()
                if (response.isSuccessful) {
                    val data = response.body()?.data
                    if (data != null) {
                        // Store payment info
                        _paymentInfo.value = PaymentInfo(
                            bank = data.bank,
                            accountNumber = data.accountNumber,
                            accountName = data.accountName,
                            amount = data.amount,
                            transferContent = data.transferContent
                        )

                        // Process QR code
                        val qrCodeBase64 = data.qrCode
                        val decodedBytes = Base64.getDecoder().decode(qrCodeBase64)
                        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                        _qrCodeResult.value = LoadingResult.Success(bitmap)
                    } else {
                        _qrCodeResult.value = LoadingResult.Error("Payment data is null")
                    }
                } else {
                    _qrCodeResult.value = LoadingResult.Error("Failed to fetch QR code: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("CheckoutViewModel", "Error fetching QR code", e)
                _qrCodeResult.value = LoadingResult.Error("Error: ${e.message}")
            }
        }
    }

    fun fetchUserDetail() {
        viewModelScope.launch {
            try {
                val response = userRepository.fetchUserDetail()
                if (response.isSuccessful) {
                    val user = response.body()?.data?.user?.toDomain()
                    if (user?.isGold == true) {
                        _showSuccessDialog.value = true
                    }
                }
            } catch (e: Exception) {
                // Handle error silently as this is a background check
                Log.e("CheckoutViewModel", "Error fetching user detail", e)
            }
        }
    }

    fun dismissSuccessDialog() {
        _showSuccessDialog.value = false
    }
} 