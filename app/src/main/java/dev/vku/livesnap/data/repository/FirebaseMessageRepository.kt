package dev.vku.livesnap.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dev.vku.livesnap.data.local.TokenManager
import dev.vku.livesnap.domain.model.Chat
import dev.vku.livesnap.domain.model.Message
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date

class FirebaseMessageRepository(
    firestore: FirebaseFirestore,
    private val tokenManager: TokenManager
) {
    private val chatsCollection = firestore.collection("chats")
    private val messagesCollection = firestore.collection("messages")

    fun getChats(): Flow<List<Chat>> = callbackFlow {
        try {
            val userId = getCurrentUserId() ?: throw IllegalStateException("User not logged in")
            
            val listener = chatsCollection
                .whereArrayContains("participants", userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("FirebaseMessageRepository", "Error in getChats snapshot: ${error.message}", error)
                        close(error)
                        return@addSnapshotListener
                    }

                    val chats = snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(Chat::class.java)?.copy(id = doc.id)
                    } ?: emptyList()
                    
                    trySend(chats)
                }

            awaitClose { listener.remove() }
        } catch (e: Exception) {
            Log.e("FirebaseMessageRepository", "Error in getChats: ${e.message}", e)
            close(e)
        }
    }

    suspend fun getChat(chatId: String): Chat {
        return try {
            val doc = chatsCollection.document(chatId).get().await()
            doc.toObject(Chat::class.java)?.copy(id = doc.id)
                ?: throw IllegalStateException("Chat not found")
        } catch (e: Exception) {
            Log.e("FirebaseMessageRepository", "Error getting chat: ${e.message}", e)
            throw e
        }
    }

    fun getMessages(chatId: String, limit: Long): Flow<List<Message>> = callbackFlow {
        try {
            val listener = messagesCollection
                .whereEqualTo("chatId", chatId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("FirebaseMessageRepository", "Error in getMessages snapshot: ${error.message}", error)
                        close(error)
                        return@addSnapshotListener
                    }

                    val messages = snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(Message::class.java)?.copy(id = doc.id)
                    } ?: emptyList()
                    
                    trySend(messages)
                }

            awaitClose { listener.remove() }
        } catch (e: Exception) {
            Log.e("FirebaseMessageRepository", "Error in getMessages: ${e.message}", e)
            close(e)
        }
    }

     suspend fun sendMessage(chatId: String, content: String, snapId: String? = null): Result<Message> = try {
        val userId = getCurrentUserId() ?: throw IllegalStateException("User not logged in")
        
        // Get chat to find the receiver
        val chat = chatsCollection.document(chatId).get().await().toObject(Chat::class.java)
            ?: throw IllegalStateException("Chat not found")
        
        // Find the receiver ID (the other participant in the chat)
        val receiverId = chat.participants.find { it != userId }
            ?: throw IllegalStateException("Receiver not found in chat participants")
        
        val message = Message(
            chatId = chatId,
            senderId = userId,
            receiverId = receiverId,
            content = content,
            snapId = snapId,
            timestamp = Date()
        )

        val docRef = messagesCollection.document()
        val messageWithId = message.copy(id = docRef.id)
        
        docRef.set(messageWithId).await()
        
        // Update last message in chat
        chatsCollection.document(chatId)
            .update("lastMessage", messageWithId)
            .await()
        Result.success(messageWithId)
    } catch (e: Exception) {
        Log.e("FirebaseMessageRepository", "Error sending message: ${e.message}", e)
        Result.failure(e)
    }

    suspend fun markMessageAsRead(messageId: String) {
        try {
            messagesCollection.document(messageId)
                .update("isRead", true)
                .await()
        } catch (e: Exception) {
            Log.e("FirebaseMessageRepository", "Error marking message as read: ${e.message}", e)
        }
    }

     suspend fun createChat(participantId: String): Result<Chat> = try {
        val userId = getCurrentUserId() ?: throw IllegalStateException("User not logged in")
        
        val chat = Chat(
            participants = listOf(userId, participantId)
        )

        val docRef = chatsCollection.document()
        val chatWithId = chat.copy(id = docRef.id)
        
        docRef.set(chatWithId).await()
        
        Result.success(chatWithId)
    } catch (e: Exception) {
        Log.e("FirebaseMessageRepository", "Error creating chat: ${e.message}", e)
        Result.failure(e)
    }

    fun observeNewMessages(chatId: String): Flow<Message> = callbackFlow {
        try {
            val listener = messagesCollection
                .whereEqualTo("chatId", chatId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("FirebaseMessageRepository", "Error in observeNewMessages snapshot: ${error.message}", error)
                        close(error)
                        return@addSnapshotListener
                    }

                    snapshot?.documents?.firstOrNull()?.let { doc ->
                        doc.toObject(Message::class.java)?.copy(id = doc.id)?.let { message ->
                            trySend(message)
                        }
                    }
                }

            awaitClose { listener.remove() }
        } catch (e: Exception) {
            Log.e("FirebaseMessageRepository", "Error in observeNewMessages: ${e.message}", e)
            close(e)
        }
    }

    suspend fun getOrCreateChat(participantId: String): Result<Chat> = try {
        val userId = getCurrentUserId() ?: throw IllegalStateException("User not logged in")
        // Tìm chat đã tồn tại giữa 2 user
        val querySnapshot = chatsCollection
            .whereArrayContains("participants", userId)
            .get().await()
        val chat = querySnapshot.documents
            .mapNotNull { it.toObject(Chat::class.java)?.copy(id = it.id) }
            .find { it.participants.contains(participantId) }
        if (chat != null) {
            Result.success(chat)
        } else {
            // Nếu chưa có thì tạo mới
            val newChat = Chat(participants = listOf(userId, participantId))
            val docRef = chatsCollection.document()
            val chatWithId = newChat.copy(id = docRef.id)
            docRef.set(chatWithId).await()
            Result.success(chatWithId)
        }
    } catch (e: Exception) {
        Result.failure(e)
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