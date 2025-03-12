import com.google.gson.annotations.SerializedName

/**
 * Representation of a chatroom
 */
data class ChatItem(
    @SerializedName("_id") val chatId: String,
    val member_1: String,
    val member_2: String
)

