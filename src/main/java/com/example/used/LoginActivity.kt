package com.example.used

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private val userEmail: EditText by lazy {
        findViewById(R.id.email)
    }

    private val password: EditText by lazy {
        findViewById(R.id.password)
    }

    private val signInButton: Button by lazy {
        findViewById(R.id.sign_in)
    }

    private val signUpButton: Button by lazy {
        findViewById(R.id.sign_up)
    }

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = Firebase.auth

        setupEditTexts()
        setupButtons()
    }

    private fun setupEditTexts() {
        userEmail.addTextChangedListener {
            checkEnableLoginButton()
        }

        password.addTextChangedListener {
            checkEnableLoginButton()
        }
    }

    private fun checkEnableLoginButton() {
        signInButton.isEnabled = userEmail.text.isNotEmpty() && password.text.isNotEmpty()
    }

    private fun setupButtons() {
        signInButton.isEnabled = false
        signInButton.setOnClickListener {
            doLogin()
        }

        signUpButton.setOnClickListener {
            goToSignUpActivity()
        }
    }

    private fun doLogin() {
        val email = userEmail.text.toString()
        val password = password.text.toString()

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    goToMainActivity()
                } else {
                    Log.w("LoginActivity", "signInWithEmail", task.exception)
                    Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun goToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun goToSignUpActivity() {
        startActivity(Intent(this, SignUpActivity::class.java))
    }
}
