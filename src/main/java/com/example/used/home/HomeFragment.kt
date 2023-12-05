package com.example.used.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.used.DBKey.Companion.CHILD_CHAT
import com.example.used.DBKey.Companion.DB_ARTICLES
import com.example.used.DBKey.Companion.DB_USERS
import com.example.used.chatlist.ChatListItem
import com.example.used.databinding.FragmentHomeBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.example.used.R
import com.google.firebase.ktx.Firebase

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var binding: FragmentHomeBinding? = null

    private lateinit var articleAdapter: ArticleAdapter
    private lateinit var articleDB: DatabaseReference
    private lateinit var userDB: DatabaseReference

    private val articleList = mutableListOf<ArticleModel>()

    fun filterArticlesBySaleStatus(onSale: Boolean) {
        val filteredList = articleList.filter { article ->
            when (onSale) {
                true -> {
                    article.status == "ONSALE"
                }
                false ->{
                    article.status == "SOLDOUT"

                }
            }

        }

        articleAdapter.submitList(filteredList)
    }

    private val listener = object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {

            val articleModel = snapshot.getValue(ArticleModel::class.java)
            articleModel ?: return

            articleList.add(articleModel)
            articleAdapter.submitList(articleList)

        }
        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            val articleModel = snapshot.getValue(ArticleModel::class.java) ?: return

            val index = articleList.indexOfFirst { it.chatKey == articleModel.chatKey }
            if (index > -1) {
                articleList[index] = articleModel
                articleAdapter.notifyItemChanged(index)
            }
        }        override fun onChildRemoved(snapshot: DataSnapshot) {}
        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
        override fun onCancelled(error: DatabaseError) {}
    }

    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentHomeBinding = FragmentHomeBinding.bind(view)
        binding = fragmentHomeBinding

        articleList.clear()

        articleDB = Firebase.database.reference.child(DB_ARTICLES)
        userDB = Firebase.database.reference.child(DB_USERS)
        articleAdapter = ArticleAdapter(onItemClicked = { articleModel->
            if (auth.currentUser != null){
                // todo 로그인을 한 상태

                if(auth.currentUser!!.uid != articleModel.sellerId){


                    val chatRoom = ChatListItem(
                        buyerId = auth.currentUser!!.uid,
                        sellerId = articleModel.sellerId,
                        itemTitle = articleModel.title,
                        key = System.currentTimeMillis().toString()
                    )

                    userDB.child(auth.currentUser!!.uid)
                        .child(CHILD_CHAT)
                        .push()
                        .setValue(chatRoom)

                    userDB.child(articleModel.sellerId)
                        .child(CHILD_CHAT)
                        .push()
                        .setValue(chatRoom)


                }

            } else {
                // todo 로그인을 안한 상태
                Snackbar.make(view,"로그인 후 사용해주세요", Snackbar.LENGTH_LONG).show()

            }

        } )

        fragmentHomeBinding.articleRecyclerView.layoutManager = LinearLayoutManager(context)
        fragmentHomeBinding.articleRecyclerView.adapter = articleAdapter

        fragmentHomeBinding.addFloatingButton.setOnClickListener{

            if(auth.currentUser != null){
                val intent = Intent(requireContext(), AddArticleActivity::class.java)
                startActivity(intent)
            }else{
                Snackbar.make(view,"로그인 후 사용해주세요", Snackbar.LENGTH_LONG).show()
            }
        }

        fragmentHomeBinding.filteringButton.setOnClickListener { view ->
            val popupMenu = PopupMenu(requireContext(), view)

            popupMenu.menuInflater.inflate(R.menu.filtering_menu, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_item_onsale -> {
                        filterArticlesBySaleStatus(true) // true for on sale
                        true
                    }
                    R.id.menu_item_soldout -> {
                        filterArticlesBySaleStatus(false) // false for sold out
                        true
                    }
                    R.id.menu_item_all -> {
                        articleAdapter.submitList(articleList)
                        true
                    }
                    // Add more cases for additional menu items as needed
                    else -> false
                }
            }
            popupMenu.show()
        }



        articleDB.addChildEventListener(listener)


    }
    override fun onResume() {
        super.onResume()

        articleAdapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        articleDB.removeEventListener(listener)
    }



}