package com.example.chatapp

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.chatapp.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private var imageURI: Uri? = null
    private var imageuri: String? = null
    private val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+".toRegex()
    private val PICK_IMAGE = 10 // Request code for selecting an image
    private lateinit var progressDialog: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        progressDialog = ProgressDialog(this@RegisterActivity)
        progressDialog.setMessage("Register SuccessFully....")
        progressDialog.setCancelable(false)
        supportActionBar?.hide()
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()


        binding.registerName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                val input = p0.toString()
                if (input.isEmpty()) {
                    // Handle empty input
                    binding.nameRgText.text = "This field is required"
                } else {
                    // Clear any error message if input is not empty
                    binding.nameRgText.text = ""
                }
            }

        })
        binding.registerEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                val input = p0.toString()
                if (input.isEmpty()) {
                    binding.emailRgText.text = "This field is required"
                } else {
                    binding.emailRgText.text = ""
                }
            }

        })
        binding.registerPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                val input = p0.toString()
                if (input.isEmpty()) {
                    binding.passwordRgText.text = "This field is required"
                } else {
                    // Clear any error message if input is not empty
                    binding.passwordRgText.text = ""
                }
            }

        })
        binding.confirmPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // binding.cPasswordRgText.text = ""

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                val input = p0.toString()
                val password = binding.registerPassword.text.toString()

                if (input.isEmpty()) {

                    binding.cPasswordRgText.text = "This field is required"
                }/*else if (password != input) {
                    binding.cPasswordRgText.setText("The Passwords Don't Match")
                }*/
                else {
                    binding.cPasswordRgText.text = ""
                }
            }

        })

        binding.checked.setOnCheckedChangeListener { _, isCheckbox ->
            if (isCheckbox) {
                binding.checkText.text = ""
                Log.d("TAG", "this is log 1: ")
            } else {
                binding.checkText.text = "Please check the checkbox"
                Log.d("TAG", "this is log 2: ")
            }
        }


        binding.Register.setOnClickListener {
            theDataRegister()
        }

        binding.imageProfile.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE)
        }
    }

    private fun theDataRegister() {
        val namee = binding.registerName.text.toString()
        val emaill = binding.registerEmail.text.toString()
        val password = binding.registerPassword.text.toString()
        val cPassword = binding.confirmPassword.text.toString()
        val status = "Hey I'm Using This Application"
        val checkbox = binding.checked.isChecked



        if (TextUtils.isEmpty(namee)) {
            binding.nameRgText.setText("This field is required")
            progressDialog.dismiss()
        } else {
            binding.nameRgText.text = ""
        }
        if (TextUtils.isEmpty(emaill)) {
            binding.emailRgText.setText("This field is required")
            progressDialog.dismiss()

        } else if (!emaill.matches(emailPattern)) {
            binding.emailRgText.setText("Type A Valid Email Here")
            progressDialog.dismiss()

        } else {
            binding.emailRgText.text = ""
        }

        if (TextUtils.isEmpty(password)){
            binding.passwordRgText.setText("This field is required")
            progressDialog.dismiss()

        }else if (password.length < 6){
            binding.passwordRgText.setText("The password must be at least 6 characters long")
            progressDialog.dismiss()

        }else{
            binding.passwordRgText.text = ""
        }

        if (TextUtils.isEmpty(cPassword)) {
            binding.cPasswordRgText.setText("This field is required")
            progressDialog.dismiss()

        } else if (password != cPassword) {
            binding.cPasswordRgText.setText("Confirm password does not match")
            progressDialog.dismiss()

        } else {
            binding.cPasswordRgText.text = ""
        }

        if (binding.checked.isChecked) {
            binding.checkText.text = ""

            if (TextUtils.isEmpty(namee) || TextUtils.isEmpty(checkbox.toString()) || TextUtils.isEmpty(
                    emaill
                ) || TextUtils.isEmpty(password) || TextUtils.isEmpty(cPassword)
            ) {
                Log.d("TAG", "onCreate:${binding.registerName} ")
                progressDialog.dismiss()


            } else {
                progressDialog.show()

                auth.createUserWithEmailAndPassword(emaill, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val id = task.result?.user?.uid
                            val reference = database.reference.child("user").child(id!!)
                            val storageReference = storage.reference.child("Upload").child(id)

                            if (imageURI != null) {
                                storageReference.putFile(imageURI!!)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            storageReference.downloadUrl.addOnSuccessListener { uri ->
                                                imageuri = uri.toString()
                                                val users = Users(
                                                    id, namee, emaill, password,
                                                    imageuri!!, status
                                                )
                                                reference.setValue(users)
                                                    .addOnCompleteListener { task ->
                                                        if (task.isSuccessful) {
                                                            val intent = Intent(
                                                                this@RegisterActivity,
                                                                LoginActivity2::class.java
                                                            )
                                                            startActivity(intent)
                                                            progressDialog.dismiss()

                                                            finish()
                                                        } else {
                                                            Toast.makeText(
                                                                this@RegisterActivity,
                                                                "Error in creating the user",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                        progressDialog.dismiss()


                                                    }
                                            }
                                        }
                                    }
                            } else {
                                imageuri =
                                    "https://firebasestorage.googleapis.com/v0/b/bionic-aspect-418409.appspot.com/o/user.png?alt=media&token=5b538780-6548-4dd5-a3c8-888e5eff4b95"
                                val users =
                                    Users(id, namee, emaill, password, imageuri!!, status)
                                reference.setValue(users).addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val intent = Intent(
                                            this@RegisterActivity,
                                            LoginActivity2::class.java
                                        )
                                        startActivity(intent)
                                        finish()
                                    } else {
                                        Toast.makeText(
                                            this@RegisterActivity,
                                            "Error in creating the user",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    progressDialog.dismiss()


                                }
                            }
                        } else {
                            Toast.makeText(
                                this@RegisterActivity,
                                task.exception?.message,
                                Toast.LENGTH_SHORT
                            ).show()
                            progressDialog.dismiss()

                        }
                    }
            }
        }else{
            binding.checkText.text = "Please check the checkbox"
        }
    }

    fun GoToLogin(view: View) {
        startActivity(Intent(this@RegisterActivity,LoginActivity2::class.java))
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 10 && resultCode == RESULT_OK) {
            data?.data?.let {
                imageURI = it
                binding.imageProfile.setImageURI(imageURI)
            }
        }
    }


}