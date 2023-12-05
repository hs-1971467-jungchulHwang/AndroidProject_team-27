package com.example.used.home

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.used.databinding.ItemArticleBinding
import com.example.used.detail.DetailActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*

class ArticleAdapter(val onItemClicked: (ArticleModel) -> Unit) : ListAdapter<ArticleModel, ArticleAdapter.ViewHolder>(diffUtil) {

    private val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    inner class ViewHolder(private val binding: ItemArticleBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(articleModel: ArticleModel) {
            setArticleInfo(articleModel)
            setImage(articleModel)
            binding.root.setOnClickListener {
                onItemClicked(articleModel)
            }
        }

        private fun setArticleInfo(articleModel: ArticleModel) {
            val format = SimpleDateFormat("MM월 dd일")
            val date = Date(articleModel.createdAt)

            binding.titleTextView.text = articleModel.title
            binding.dateTextView.text = format.format(date).toString()
            binding.priceTextView.text = "${articleModel.price}원"
            binding.statusTextView.text = if (articleModel.status == Status.ONSALE.name) "판매중" else "판매완료"
        }

        private fun setImage(articleModel: ArticleModel) {
            if (articleModel.imageUrl.startsWith("gs://")) {
                val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(articleModel.imageUrl)

                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    Glide.with(binding.thumbnailImageView)
                        .load(imageUrl)
                        .into(binding.thumbnailImageView)
                }.addOnFailureListener {
                }
            } else {
                Glide.with(binding.thumbnailImageView)
                    .load(articleModel.imageUrl)
                    .into(binding.thumbnailImageView)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemArticleBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
        holder.itemView.setOnClickListener {
            startDetailActivity(holder, currentList[position])
        }
    }

    private fun startDetailActivity(holder: ViewHolder, articleModel: ArticleModel) {
        val intent = Intent(holder.itemView.context, DetailActivity::class.java).apply {
            putExtra("chatKey", articleModel.chatKey)
            putExtra("title", articleModel.title)
            putExtra("price", articleModel.price)
            putExtra("description", articleModel.description)
            putExtra("imageUrl", articleModel.imageUrl)
            putExtra("status", articleModel.status)
            putExtra("isSeller", articleModel.sellerId == auth.currentUser?.uid)
        }
        holder.itemView.context.startActivity(intent)
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<ArticleModel>() {

            override fun areItemsTheSame(oldItem: ArticleModel, newItem: ArticleModel): Boolean {
                return oldItem.createdAt == newItem.createdAt
            }
            override fun areContentsTheSame(oldItem: ArticleModel, newItem: ArticleModel): Boolean {
                return oldItem == newItem
            }
        }
    }
}
