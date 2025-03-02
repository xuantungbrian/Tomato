import android.content.Context
import android.content.Intent
import com.example.tomato.PostActivity
import com.example.tomato.PostImage
import com.example.tomato.PostItem
import com.example.tomato.PostItemRaw
import com.example.tomato.commonFunction.byteToURIs
import com.example.tomato.commonFunction.parseLocation

object PostHelper {
    /**
     * Convert PostItemRaw to PostItem
     * @return the converted PostItem
     */
    fun rawPostToPostItem(rawPost: PostItemRaw, context: Context): PostItem {
        val address = parseLocation(rawPost.latitude, rawPost.longitude, context)
        val imageURIs = byteToURIs(postImagesToByteArrays(rawPost.images), context.cacheDir, context)
        return PostItem(rawPost._id, imageURIs, address, rawPost.date, rawPost.note, rawPost.private, rawPost.userId)
    }

    /**
     * Convert a list of PostImage to a list of ByteArray.
     */
    fun postImagesToByteArrays(images: List<PostImage>): List<ByteArray> {
        val imageByteArrays = mutableListOf<ByteArray>()
        for (image in images) {
            val imageByteArray = image.fileData.data.map { it.toByte() }.toByteArray()
            imageByteArrays.add(imageByteArray)
        }
        return imageByteArrays
    }

    /**
     * Show the post activity of the given post.
     */
    fun showPostActivity(post: PostItemRaw, context: Context){
        val postItem = PostHelper.rawPostToPostItem(post, context)

        val intent = Intent(context, PostActivity::class.java)
        intent.putExtra("postId", postItem.postId)
        intent.putExtra("userId", postItem.userId)
        intent.putExtra("images", ArrayList(postItem.imageData))
        intent.putExtra("location", postItem.location)
        intent.putExtra("date", postItem.date)
        intent.putExtra("note", postItem.note)
        intent.putExtra("private", postItem.private)
        context.startActivity(intent)
    }

}