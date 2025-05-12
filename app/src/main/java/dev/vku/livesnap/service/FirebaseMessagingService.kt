package dev.vku.livesnap.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dev.vku.livesnap.MainActivity
import dev.vku.livesnap.R
import dev.vku.livesnap.data.local.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LiveSnapFirebaseMessagingService : FirebaseMessagingService() {
    private val firestore = FirebaseFirestore.getInstance()
    private val tokenManager = TokenManager(this)

    init {
        Log.d("FirebaseMessagingService", "Service initialized")
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("FirebaseMessagingService", "Service onCreate called")
        
        // Check if FCM is auto-initialized
        val isAutoInitEnabled = FirebaseMessaging.getInstance().isAutoInitEnabled
        Log.d("FirebaseMessagingService", "FCM Auto-initialization: $isAutoInitEnabled")
        
        // Check current FCM token
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FirebaseMessagingService", "Current FCM token: $token")
            } else {
                Log.e("FirebaseMessagingService", "Failed to get FCM token", task.exception)
            }
        }

        // Subscribe to a topic to test FCM
        FirebaseMessaging.getInstance().subscribeToTopic("test")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FirebaseMessagingService", "Subscribed to test topic")
                } else {
                    Log.e("FirebaseMessagingService", "Failed to subscribe to test topic", task.exception)
                }
            }
    }

    override fun onNewToken(token: String) {
        Log.d("FirebaseMessagingService", "New FCM token: $token")
        super.onNewToken(token)
        // Save token to Firestore when user is logged in
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userId = getCurrentUserId()
                if (userId != null) {
                    Log.d("FirebaseMessagingService", "Saving FCM token for user: $userId")
                    // Create or update the document
                    firestore.collection("users")
                        .document(userId)
                        .set(mapOf("fcmToken" to token), com.google.firebase.firestore.SetOptions.merge())
                        .await()
                    Log.d("FirebaseMessagingService", "FCM token saved successfully")
                } else {
                    Log.d("FirebaseMessagingService", "No user ID found, skipping token save")
                }
            } catch (e: Exception) {
                Log.e("FirebaseMessagingService", "Error saving FCM token: ${e.message}", e)
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Log.d("FirebaseMessagingService", "onMessageReceived called")
        super.onMessageReceived(message)

        Log.d("FirebaseMessagingService", "Message received: $message")
        Log.d("FirebaseMessagingService", "Message notification received: ${message.notification}")
        Log.d("FirebaseMessagingService", "Message data received: ${message.data}")

        val notification = message.notification
        val data = message.data

        if (notification != null) {
            showNotification(
                title = notification.title ?: "New Notification",
                body = notification.body ?: "",
                data = data
            )
        }
    }

    private fun showNotification(title: String, body: String, data: Map<String, String>) {
        val channelId = "livesnap_notifications"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "LiveSnap Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for friend requests and other updates"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Create intent for notification click
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Add data to intent if needed
            data.forEach { (key, value) ->
                putExtra(key, value)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        // Show notification
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private suspend fun getCurrentUserId(): String? {
        val token = tokenManager.getToken() ?: return null
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return null
            
            val payload = parts[1]
            val decodedPayload = android.util.Base64.decode(payload, android.util.Base64.URL_SAFE)
            val jsonString = String(decodedPayload)
            val jsonObject = org.json.JSONObject(jsonString)
            jsonObject.getString("userId")
        } catch (e: Exception) {
            null
        }
    }
} 