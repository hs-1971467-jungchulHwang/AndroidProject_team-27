package com.example.used

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var signUpButton: Button
    private lateinit var emailText: EditText
    private lateinit var passwordText: EditText
    private lateinit var usernameText: EditText
    private lateinit var dobText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        auth = Firebase.auth

        setUpViews()
        setUpSignUpButton()
    }

    private fun setUpViews() {
        signUpButton = findViewById(R.id.buttonSignUp)
        emailText = findViewById(R.id.editTextEmail)
        passwordText = findViewById(R.id.editTextPassword)
        usernameText = findViewById(R.id.editTextUsername)
        dobText = findViewById(R.id.editTextDOB)
    }

    private fun setUpSignUpButton() {
        signUpButton.setOnClickListener {
            val email = emailText.text.toString()
            val password = passwordText.text.toString()

            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "회원가입에 성공했습니다", Toast.LENGTH_SHORT).show()
                    val user = auth.currentUser
                    user?.let {
                        val username = usernameText.text.toString()
                        val dob = dobText.text.toString()
                        writeNewUser(it.uid, username, email, dob)
                    }
                    doLogin(email, password)
                } else {
                    Toast.makeText(this, "이미 가입한 이메일이거나, 회원가입에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun doLogin(email: String, password: String) {
        Firebase.auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val intent = Intent(this@SignUpActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun writeNewUser(userId: String, username: String, email: String, dob: String) {
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val usersRef: DatabaseReference = database.getReference("UserInfo")
        val user = User(username, email, dob)
        usersRef.child(userId).setValue(user)
    }

    data class User(
        val username: String? = "",
        val email: String? = "",
        val dob: String? = ""
    )
}
