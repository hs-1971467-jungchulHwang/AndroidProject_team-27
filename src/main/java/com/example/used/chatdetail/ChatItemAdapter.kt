package com.example.used.chatdetail
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.used.databinding.ItemChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ChatItemAdapter : ListAdapter<ChatItem, ChatItemAdapter.ViewHolder>(diffUtil) {
    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }

    inner class ViewHolder(private val binding: ItemChatBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(chatItem: ChatItem) {
            binding.senderTextView.text = chatItem.senderId
            binding.messageTextView.text = chatItem.message
            binding.timeTextView.text = chatItem.time

            isSentByCurrentUser(chatItem) { isSent ->
                if (isSent) {
                    binding.root.visibility = View.VISIBLE
                } else {
                    binding.root.visibility = View.GONE
                }
            }


        }

        private fun isSentByCurrentUser(chatItem: ChatItem, callback: (Boolean) -> Unit) {
            val currentUserId = auth.currentUser?.uid

            if (currentUserId != null) {
                val userRef = Firebase.database.reference.child("UserInfo").child(currentUserId)
                userRef.get().addOnSuccessListener { dataSnapshot ->
                    val email = dataSnapshot.child("email").value.toString()

                    val isSentByCurrentUser = chatItem.senderId == email
                    callback.invoke(isSentByCurrentUser)
                }
            } else {
                callback.invoke(false)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemChatBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<ChatItem>() {

            override fun areItemsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean {
                return oldItem.time == newItem.time
            }

            override fun areContentsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}