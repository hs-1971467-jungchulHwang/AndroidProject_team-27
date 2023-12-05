package com.example.used.mypage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.used.LoginActivity
import com.example.used.R
import com.example.used.SignUpActivity
import com.example.used.databinding.FragmentMypageBinding

import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class MyPageFragment : Fragment(R.layout.fragment_mypage) {
    private var binding: FragmentMypageBinding? = null
    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }

    // 현재 사용자의 UID 가져오기
    val currentUser = Firebase.auth.currentUser
    val uid = currentUser?.uid

    // Realtime Database에서 사용자 정보 가져오기
    val database = FirebaseDatabase.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentMypageBinding = FragmentMypageBinding.bind(view)
        binding = fragmentMypageBinding

        // Add the following code to retrieve user information from the Realtime Database
        uid?.let { nonNullUid ->
            val usersRef = database.getReference("UserInfo").child(nonNullUid)

            usersRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Check if the snapshot has data
                    if (snapshot.exists()) {
                        // The snapshot contains user information
                        val user = snapshot.getValue(SignUpActivity.User::class.java)
                        // Now you can use 'user' to access the user information
                        if (user != null) {
                            // Update the TextView with user information
                            val userNameTextView = view.findViewById<TextView>(R.id.userNametextView)
                            userNameTextView.text = "Username: ${user.username}"
                            val userEmailTextView = view.findViewById<TextView>(R.id.userEmailTextView)
                            userEmailTextView.text = "Email: ${user.email}"
                            val userBirthTextView = view.findViewById<TextView>(R.id.userBirthTextView)
                            userBirthTextView.text = "Birth Day: ${user.dob}"
                        }
                    } else {
                        // Handle the case when there is no data for the current user
                        Log.d("MyPageFragment", "No data found for the current user")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle errors here
                    Log.e("MyPageFragment", "Error retrieving user data: ${error.message}")
                }
            })
        }

        val out = view.findViewById<Button>(R.id.logout) // 로그아웃
        out.setOnClickListener {
            Firebase.auth.signOut()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish() // 현재 액티비티를 종료
        }
    }
}