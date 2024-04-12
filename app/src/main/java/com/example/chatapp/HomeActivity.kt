package com.example.chatapp

import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.chatapp.databinding.ActivityHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: UserAdapter
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private lateinit var usersArrayList: ArrayList<Users>
    private lateinit var setImageUri: Uri
    private lateinit var email: String
    private lateinit var password: String
    private lateinit var name : String
    private lateinit var status : String
    private lateinit var sharedPreferences: SharedPreferences



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE)


        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        val reference: DatabaseReference = database.getReference("user")
        usersArrayList = ArrayList()
        binding.recyclerview.layoutManager = LinearLayoutManager(this)
        adapter = UserAdapter(this@HomeActivity, usersArrayList)
        binding.recyclerview.adapter = adapter

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                usersArrayList.clear()
                for (dataSnapshot in snapshot.children) {
                    val users: Users? = dataSnapshot.getValue(Users::class.java)
                    users?.let { usersArrayList.add(it) }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        val references = database.getReference().child("user").child(auth.uid!!)
        references.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                email = snapshot.child("mail").value.toString()
                password = snapshot.child("password").value.toString()
                 name = snapshot.child("userName").value.toString()
                val profile = snapshot.child("profilePic").value.toString()
                 status = snapshot.child("status").value.toString()
               // Picasso.get().load(profile).into(binding.userImage12)
                Glide.with(this@HomeActivity).load(profile).into(binding.userImage12)

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
        binding.userImage12.setOnClickListener {
            startActivity(Intent(this@HomeActivity,ProfileActivity2::class.java))
        }
        binding.logout.setOnClickListener {
            val dialog = Dialog(this@HomeActivity)
            dialog.setContentView(R.layout.dialog_layout)
            val yes: Button = dialog.findViewById(R.id.yesbnt)
            val no: Button = dialog.findViewById(R.id.nobnt)
            yes.setOnClickListener {
                // Clear login state from SharedPreferences
                sharedPreferences.edit().putBoolean("loggedIn", false).apply()

                // Sign out user from Firebase Authentication
                FirebaseAuth.getInstance().signOut()

                // Navigate back to the login screen (assuming LoginActivity is the main entry point)
                startActivity(Intent(this@HomeActivity, LoginActivity2::class.java))
                finish() // Finish activity after starting LoginActivity
            }
            no.setOnClickListener {
                dialog.dismiss() // Dismiss the dialog when "No" is clicked
            }
            dialog.show()
        }


    }
}