package com.example.chatapp

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.chatapp.databinding.ActivityVideoOpenBinding

class VideoOpenActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVideoOpenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoOpenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        val videoUrl = intent.getStringExtra("videoUrl")

        // Check if the video URL is not null or empty
        if (!videoUrl.isNullOrEmpty()) {
            // Set the video URI to the VideoView
            binding.videoViewFullScreen.setVideoURI(Uri.parse(videoUrl))
            // Start playing the video
            binding.videoViewFullScreen.start()

            binding.videoViewFullScreen.setOnErrorListener { mediaPlayer, what, extra ->
                // Handle the error here
                Toast.makeText(this, "Error occurred while playing the video", Toast.LENGTH_SHORT).show()
                finish()
                true // Return true to indicate that the error has been handled
            }


        } else {
            // Handle case where video URL is null or empty
            Toast.makeText(this, "Video URL is empty", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}