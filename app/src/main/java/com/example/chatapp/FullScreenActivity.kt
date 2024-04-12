package com.example.chatapp

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import com.example.chatapp.databinding.ActivityFullScreenBinding
import com.squareup.picasso.Picasso

class FullScreenActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFullScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        val mediaUrl = intent.getStringExtra("mediaUrl")

        if (!mediaUrl.isNullOrEmpty()) {
            Picasso.get().load(mediaUrl).into(binding.imageFullScreen)
        }

       /* if (!mediaUrls.isNullOrEmpty()){
            binding.videoFullScreen.setVideoURI(Uri.parse(mediaUrls))

            // Start playing the video
            binding.videoFullScreen.start()

        }*/
      /*  binding.fullScreenLeft.setOnClickListener {
            startActivity(Intent(this@FullScreenActivity,Chatwindo::class.java))
            finish()

        }

        binding.imageFullScreen.setOnClickListener {
            if (binding.fullScreenLeft.visibility == View.VISIBLE) {
                binding.fullScreenLeft.visibility = View.GONE
            } else {
                binding.fullScreenLeft.visibility = View.VISIBLE
            }
        }*/
    }
}