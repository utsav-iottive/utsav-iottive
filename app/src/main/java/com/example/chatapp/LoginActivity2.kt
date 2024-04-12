package com.example.chatapp

import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.chatapp.databinding.ActivityLogin2Binding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class LoginActivity2 : AppCompatActivity() {
    private lateinit var binding: ActivityLogin2Binding
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+".toRegex()
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogin2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        progressDialog = ProgressDialog(this@LoginActivity2)
        progressDialog.setMessage("Login SuccessFully...")
        progressDialog.setCancelable(false)
        supportActionBar?.hide()
        auth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE)

        if (sharedPreferences.getBoolean("loggedIn", false)) {
            startActivity(Intent(this@LoginActivity2, HomeActivity::class.java))
            finish()
            return
        }

        binding.loginEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                val input = p0?.toString() ?: ""
                if (input.isEmpty()) {
                    binding.emailText.text = "This field is required"
                } else {
                    binding.emailText.text = ""
                }
            }
        })

        binding.loginPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                val input = p0?.toString() ?: ""
                if (input.isEmpty()) {
                    binding.passwordText.text = "This field is required"
                } else {
                    binding.passwordText.text = ""
                }
            }
        })

        binding.login.setOnClickListener {
            TheLoginData()
        }
    }

    private fun TheLoginData() {
        val Email = binding.loginEmail.text.toString()
        val pass = binding.loginPassword.text.toString()

        if (TextUtils.isEmpty(Email)) {
            binding.emailText.setText("This field is required")
            progressDialog.dismiss()
            return
        } else if (!Email.matches(emailPattern)) {
            binding.emailText.setText("Type A Valid Email Here")
            progressDialog.dismiss()

            return
        }

        if (TextUtils.isEmpty(pass)) {
            binding.passwordText.setText("This field is required")
            progressDialog.dismiss()

            return
        } else if (pass.length < 6) {
            binding.passwordText.setText("Password must be six characters")
            progressDialog.dismiss()

            return
        }

        progressDialog.show()
        try {
            auth.signInWithEmailAndPassword(Email, pass).addOnCompleteListener { task ->
              progressDialog.dismiss()
                if (task.isSuccessful) {
                    sharedPreferences.edit().putBoolean("loggedIn", true).apply()
                    val intent = Intent(this@LoginActivity2, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    progressDialog.dismiss()
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e("LoginActivity2", "Exception during login: ${e.message}", e)
            Toast.makeText(this, "An error occurred during login", Toast.LENGTH_SHORT).show()
        }
    }

    fun GoToRegister(view: View) {
        startActivity(Intent(this@LoginActivity2, RegisterActivity::class.java))
        finish()
    }
}
