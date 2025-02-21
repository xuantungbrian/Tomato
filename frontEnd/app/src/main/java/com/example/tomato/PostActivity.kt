package com.example.tomato

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import java.util.Date

class PostActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_post)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val images = intent.getParcelableArrayListExtra<Uri>("images")
        val location = intent.getStringExtra("location")
        val date = intent.getStringExtra("date")
        val note = intent.getStringExtra("note")
        val private = intent.getBooleanExtra("private", false)
        val postLocation: TextView = findViewById(R.id.post_activity_postLocation)
        postLocation.text = location

        val postDate: TextView = findViewById(R.id.post_activity_postDate)

        // Update post's date
        postDate.text = "Posted on ${commonFunction.convertDateToString(date.toString())}"

        //Update post's note
        val postNote: TextView = findViewById(R.id.post_activity_postNote)
        postNote.text = note

        //Update post's images
        val postViewPager: ViewPager2 = findViewById(R.id.postViewPager)
        postViewPager.adapter = PostAdapter(images!!)


    }
}

class PostAdapter(private val images: List<Uri>): RecyclerView.Adapter<PostAdapter.PostViewHolder>() {
    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val postImage: ImageView = itemView.findViewById(R.id.post_activity_postImage)

        fun bind(image: Uri) {
            postImage.setImageURI(image)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.post_activity_post_item, parent, false)
        return PostViewHolder(view)
    }

    override fun getItemCount(): Int {
        return images.size
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(images[position])
    }


}