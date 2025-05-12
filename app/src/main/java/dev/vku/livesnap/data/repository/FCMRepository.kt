package dev.vku.livesnap.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

interface FCMRepository {
    suspend fun getFCMToken(): String?
    suspend fun refreshFCMToken(): String
    suspend fun sendFriendRequestNotification(receiverId: String, senderName: String)
    suspend fun sendFriendRequestAcceptedNotification(receiverId: String, accepterName: String)
}

class DefaultFCMRepository(
    private val firestore: FirebaseFirestore
) : FCMRepository {
    override suspend fun getFCMToken(): String? {
        return try {
            FirebaseMessaging.getInstance().token.await()
        } catch (e: Exception) {
            Log.e("FCMRepository", "Error getting FCM token: ${e.message}", e)
            null
        }
    }

    override suspend fun refreshFCMToken(): String {
        try {
            FirebaseMessaging.getInstance().deleteToken().await()
            val token = FirebaseMessaging.getInstance().token.await()
            return token
        } catch (e: Exception) {
            throw RuntimeException("Error refreshing FCM token", e)
        }
    }

    override suspend fun sendFriendRequestNotification(receiverId: String, senderName: String) {
        try {
            // Get receiver's FCM token from Firestore
            val receiverDoc = firestore.collection("users")
                .document(receiverId)
                .get()
                .await()

            val fcmToken = receiverDoc.getString("fcmToken")
            if (fcmToken != null) {
                // Create notification data
                val notificationData = mapOf(
                    "title" to "New Friend Request",
                    "body" to "$senderName sent you a friend request",
                    "type" to "FRIEND_REQUEST",
                    "userId" to receiverId
                )

                // Store notification in Firestore
                firestore.collection("notifications")
                    .add(mapOf(
                        "token" to fcmToken,
                        "notification" to notificationData,
                        "timestamp" to com.google.firebase.Timestamp.now()
                    ))
                    .await()
            }
        } catch (e: Exception) {
            Log.e("FCMRepository", "Error sending friend request notification: ${e.message}", e)
        }
    }

    override suspend fun sendFriendRequestAcceptedNotification(receiverId: String, accepterName: String) {
        try {
            // Get receiver's FCM token from Firestore
            val receiverDoc = firestore.collection("users")
                .document(receiverId)
                .get()
                .await()

            val fcmToken = receiverDoc.getString("fcmToken")
            if (fcmToken != null) {
                // Create notification data
                val notificationData = mapOf(
                    "title" to "Friend Request Accepted",
                    "body" to "$accepterName accepted your friend request",
                    "type" to "FRIEND_REQUEST_ACCEPTED",
                    "userId" to receiverId
                )

                // Store notification in Firestore
                firestore.collection("notifications")
                    .add(mapOf(
                        "token" to fcmToken,
                        "notification" to notificationData,
                        "timestamp" to com.google.firebase.Timestamp.now()
                    ))
                    .await()
            }
        } catch (e: Exception) {
            Log.e("FCMRepository", "Error sending friend request accepted notification: ${e.message}", e)
        }
    }
} 