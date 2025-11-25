package com.application.messagechat.signup

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.application.messagechat.activity.ForgetPasswordActivity
import com.application.messagechat.R
import com.application.messagechat.databinding.ActivityLoginBinding
import com.application.messagechat.util.CheckInternetConnection
import com.application.messagechat.activity.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class LoginActivity : AppCompatActivity() {

    lateinit var binding: ActivityLoginBinding
    private  val firestore = FirebaseFirestore.getInstance()
    private lateinit var sharedPreferences: SharedPreferences
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = DataBindingUtil.setContentView(this,R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.buttonLogin.setOnClickListener {
            val email = binding.editTextLoginEmail.text.toString().lowercase()
            val password = binding.editTextLoginPassword.text.toString()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(
                    this@LoginActivity,
                    "Please Enter Email and Password",
                    Toast.LENGTH_LONG
                ).show()
            }else{
                loginUser(email,password)
            }

        }

        binding.buttonSignUpActivity.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        binding.textViewForgetPassword.setOnClickListener {
            val activity = Intent(this@LoginActivity, ForgetPasswordActivity::class.java)
            startActivity(activity)
        }
    }

    fun loginUser(email: String,password: String){
        if (!CheckInternetConnection.checkInternetConnection(this)){
            Toast.makeText(this,"No Internet connection!", Toast.LENGTH_LONG).show()
            binding.loginProgressBar.isVisible = false
            return
        }
        binding.loginProgressBar.isVisible = true
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    firestore.collection("users")
                        .whereEqualTo("email", email)
                        .get()
                        .addOnSuccessListener { documents ->
                            if (!documents.isEmpty) {
                                val document = documents.documents[0]
                                val userId = document.id
                                sharedPreferences = getSharedPreferences("MyLoginInfo", MODE_PRIVATE)
                                val info = sharedPreferences.edit()
                                info.putBoolean("userLoged", true)
                                info.putString("userId", userId)
                                info.apply()
                                val activity = Intent(this@LoginActivity, MainActivity::class.java)
                                startActivity(activity)
                                finish()
                            }
                        }
                }else{
                    Toast.makeText(this@LoginActivity,"Incorrect Email or password", Toast.LENGTH_LONG).show()
                    binding.loginProgressBar.isVisible = false
                }
            }.addOnFailureListener {error->
                Toast.makeText(this,"Login failed: ${error.message}", Toast.LENGTH_LONG).show()
                binding.loginProgressBar.isVisible = false
            }
    }


}