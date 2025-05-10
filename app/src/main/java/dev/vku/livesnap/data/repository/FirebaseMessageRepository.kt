package dev.vku.livesnap.data.repository

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
import javax.inject.Inject

class FirebaseMessageRepository @Inject constructor(
    firestore: FirebaseFirestore,
    private val tokenManager: TokenManager
) {
    private val chatsCollection = firestore.collection("chats")
    private val messagesCollection = firestore.collection("messages")

    fun getChats(): Flow<List<Chat>> = callbackFlow {
        val userId = getCurrentUserId() ?: throw IllegalStateException("User not logged in")
        
        val listener = chatsCollection
            .whereArrayContains("participants", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val chats = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Chat::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                
                trySend(chats)
            }

        awaitClose { listener.remove() }
    }

    fun getMessages(chatId: String, limit: Long): Flow<List<Message>> = callbackFlow {
        val listener = messagesCollection
            .whereEqualTo("chatId", chatId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(limit)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val messages = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Message::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                
                trySend(messages)
            }

        awaitClose { listener.remove() }
    }

     suspend fun sendMessage(chatId: String, content: String): Result<Message> = try {
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
        Result.failure(e)
    }

    suspend fun markMessageAsRead(messageId: String) {
        messagesCollection.document(messageId)
            .update("isRead", true)
            .await()
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
        Result.failure(e)
    }

    fun observeNewMessages(chatId: String): Flow<Message> = callbackFlow {
        val listener = messagesCollection
            .whereEqualTo("chatId", chatId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
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