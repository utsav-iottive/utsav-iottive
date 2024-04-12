package com.example.chatapp

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.example.chatapp.databinding.ActivityProfile2Binding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class ProfileActivity2 : AppCompatActivity() {
    private lateinit var binding: ActivityProfile2Binding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private var setImageUri: Uri? = null
    private lateinit var email: String
    private lateinit var password: String
    private lateinit var progressDialog: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfile2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        progressDialog = ProgressDialog(this@ProfileActivity2)
        progressDialog.setMessage("Profile Is SuccessFully Update...")
        progressDialog.setCancelable(false)
        supportActionBar?.hide()

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        val reference = database.getReference().child("user").child(auth.uid!!)
        val storageReference = storage.reference.child("upload").child(auth.uid!!)

        reference.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                email = snapshot.child("mail").value.toString()
                password = snapshot.child("password").value.toString()
                val name = snapshot.child("userName").value.toString()
                val profile = snapshot.child("profilepic").value.toString()
                val status = snapshot.child("status").value.toString()
                binding.settingname.setText(name)
                binding.settingstatus.setText(status)
                Picasso.get().load(profile).into(binding.profileimgg1)

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        binding.goHomePage.setOnClickListener {
            startActivity(Intent(this@ProfileActivity2,HomeActivity::class.java))
            finish()
        }

        binding.doneButt.setOnClickListener {
            val name = binding.settingname.text.toString()
            val status = binding.settingstatus.text.toString()

            // Show the progress bar
            progressDialog.show()
            if (setImageUri != null) {
                storageReference.putFile(setImageUri!!).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        storageReference.downloadUrl.addOnSuccessListener { uri ->
                            val finalImageUri = uri.toString()
                            val users = Users(auth.uid!!, name, email, password, finalImageUri, status)
                            reference.setValue(users).addOnCompleteListener { task ->
                                // Hide the progress bar when the operation is complete
                                progressDialog.dismiss()
                                if (task.isSuccessful) {
                                    val intent = Intent(this@ProfileActivity2, MainActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    progressDialog.dismiss()
                                    Toast.makeText(this@ProfileActivity2, "Something went wrong", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } else {
                        // Hide the progress bar if the upload fails
                        progressDialog.dismiss()
                        Toast.makeText(this@ProfileActivity2, "Upload failed", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                storageReference.downloadUrl.addOnSuccessListener { uri ->
                    val finalImageUri = uri.toString()
                    val users = Users(auth.uid!!, name, email, password, finalImageUri, status)
                    reference.setValue(users).addOnCompleteListener { task ->
                        // Hide the progress bar when the operation is complete
                        progressDialog.dismiss()

                        if (task.isSuccessful) {
                            val intent = Intent(this@ProfileActivity2, HomeActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@ProfileActivity2, "Something went wrong", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

/*
        binding.doneButt.setOnClickListener {
            val name = binding.settingname.text.toString()
            val status = binding.settingstatus.text.toString()

            if (setImageUri != null) {
                val uploadTask = storageReference.putFile(setImageUri!!)

                uploadTask.continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    storageReference.downloadUrl
                }.addOnCompleteListener { task
                    ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result

                        val users = Users(auth.uid!!, name, email, password, downloadUri.toString(), status)
                        reference.setValue(users).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                //Toast.makeText(this@ProfileActivity2, "Data is saved", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this@ProfileActivity2, HomeActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(this@ProfileActivity2, "Failed to save data", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        // Handle unsuccessful task
                        Toast.makeText(this@ProfileActivity2, "Failed to upload image", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                // Handle case when setImageUri is null
                Toast.makeText(this@ProfileActivity2, "Please select an image", Toast.LENGTH_SHORT).show()
            }
        }
*/


        binding.profileimgg1.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), 10)
        }


    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 10 && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                setImageUri = uri
                binding.profileimgg1.setImageURI(setImageUri)
            }
        }
    }

}