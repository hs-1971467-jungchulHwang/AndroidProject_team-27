package com.example.used.chatlist

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.used.DBKey.Companion.CHILD_CHAT
import com.example.used.DBKey.Companion.DB_USERS
import com.example.used.R
import com.example.used.chatdetail.ChatRoomActivity
import com.example.used.databinding.FragmentChatlistBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ChatListFragment: Fragment(R.layout.fragment_chatlist) {

    private var binding: FragmentChatlistBinding? = null
    private lateinit var chatListAdapter: ChatListAdapter
    private val chatRoomList = mutableListOf<ChatListItem>()
    private lateinit var chatDB: DatabaseReference

    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentChatlistBinding = FragmentChatlistBinding.bind(view)
        binding = fragmentChatlistBinding

        setUpRecyclerView()
        setChatListAdapter()

        if (auth.currentUser == null){
            return
        }

        chatDB = Firebase.database.reference.child(DB_USERS).child(auth.currentUser!!.uid).child(CHILD_CHAT)
        fetchChatRooms()
    }

    private fun setUpRecyclerView() {
        binding?.chatListRecyclerView?.layoutManager = LinearLayoutManager(context)
    }

    private fun setChatListAdapter() {
        chatListAdapter = ChatListAdapter(onItemClicked = { chatRoom ->
            // todo 채팅방으로 이동하는 코드
            val intent = Intent(requireContext(), ChatRoomActivity::class.java)
            intent.putExtra("chatKey", chatRoom.key)
            startActivity(intent)
        })
        binding?.chatListRecyclerView?.adapter = chatListAdapter
    }

    private fun fetchChatRooms() {
        chatDB.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach{
                    val chatRoom = it.getValue(ChatListItem::class.java)
                    chatRoom?.let { room ->
                        chatRoomList.add(room)
                    }
                }
                chatListAdapter.submitList(chatRoomList)
                chatListAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // TODO 서버에서 데이터를 가져오는 것에 실패했을 경우 호출
            }
        })
    }

    override fun onResume() {
        super.onResume()
        chatListAdapter.notifyDataSetChanged()
    }
}
