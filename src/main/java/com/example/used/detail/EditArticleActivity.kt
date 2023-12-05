package com.example.used.detail

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import com.example.used.DBKey
import com.example.used.R
import com.example.used.home.ArticleModel
import com.example.used.home.Status
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database

class EditArticleActivity : AppCompatActivity() {

    private lateinit var editTitleEditText: EditText
    private lateinit var editPriceEditText: EditText
    private lateinit var editDescriptionEditText: EditText
    private lateinit var editSubmitButton: Button
    private lateinit var editProgressBar: ProgressBar
    private lateinit var editSwitch: Switch


    private lateinit var chatKey: String
    private lateinit var title: String
    private lateinit var price: String
    private lateinit var description: String
    private lateinit var imageUrl: String
    private lateinit var status: String

    private val articleDB: DatabaseReference by lazy {
        com.google.firebase.ktx.Firebase.database.reference.child(DBKey.DB_ARTICLES)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("switchState", editSwitch.isChecked)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val switchState = savedInstanceState.getBoolean("switchState", false)
        editSwitch.isChecked = switchState
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_article)

        editTitleEditText = findViewById(R.id.editTitleEditText)
        editPriceEditText = findViewById(R.id.editPriceEditText)
        editDescriptionEditText = findViewById(R.id.editDescriptionEditText)
        editSubmitButton = findViewById(R.id.editSubmitButton)
        editProgressBar = findViewById(R.id.editProgressBar)
        editSwitch = findViewById(R.id.switch1)

        chatKey = intent.getStringExtra("chatKey") ?: ""
        title = intent.getStringExtra("title") ?: ""
        price = intent.getStringExtra("price") ?: ""
        description = intent.getStringExtra("description") ?: ""
        imageUrl = intent.getStringExtra("imageUrl") ?: ""
        status = intent.getStringExtra("status") ?: ""

        val priceWithoutWon = price.replace("원", "")

        editTitleEditText.setText(title)
        editPriceEditText.setText(priceWithoutWon)
        editDescriptionEditText.setText(description)
        editSwitch.isChecked = status == Status.SOLDOUT.name

        editSubmitButton.setOnClickListener {
            Log.d("EditArticleActivity", "editSubmitButton clicked")
            val updatedTitle = editTitleEditText.text.toString()
            val updatedPrice = editPriceEditText.text.toString()
            val updatedDescription = editDescriptionEditText.text.toString()
            val updatedStatus = if (editSwitch.isChecked) Status.SOLDOUT.name else Status.ONSALE.name

            Log.d("EditArticleActivity", "Updated values: title=$updatedTitle, price=$updatedPrice, description=$updatedDescription")



            val resultIntent = Intent().apply {
                putExtra("title", updatedTitle)
                putExtra("price", updatedPrice)
                putExtra("description", updatedDescription)
                putExtra("status", updatedStatus)
            }

            setResult(Activity.RESULT_OK, resultIntent)
            updateArticle()
            finish()
        }
    }

    private fun updateArticle() {
        val updatedTitle = editTitleEditText.text.toString().trim()
        val updatedPrice = editPriceEditText.text.toString().trim()
        val updatedDescription = editDescriptionEditText.text.toString().trim()
        val updatedStatus = if (editSwitch.isChecked) Status.SOLDOUT.name else Status.ONSALE.name

        if (chatKey.isNotEmpty()) {
            Log.d("EditArticleActivity", "Attempting to update article with chatKey: $chatKey")

            articleDB.child(chatKey).get().addOnSuccessListener { snapshot ->
                val article = snapshot.getValue(ArticleModel::class.java) ?: return@addOnSuccessListener

                article.title = updatedTitle
                article.price = updatedPrice
                article.description = updatedDescription
                article.status = updatedStatus

                Log.d("EditArticleActivity", "Article fields updated: $article")

                snapshot.ref.setValue(article).addOnSuccessListener {
                    Log.d("EditArticleActivity", "Update successful. Updated values: $article")

                    val intent = Intent().apply {
                        putExtra("title", updatedTitle)
                        putExtra("price", updatedPrice)
                        putExtra("description", updatedDescription)
                        putExtra("status", updatedStatus)
                    }
                    setResult(Activity.RESULT_OK, intent)
                    finish()

                }.addOnFailureListener { e ->
                    Log.e("EditArticleActivity", "Update failed: ${e.message}")
                }
            }.addOnFailureListener { e ->
                Log.e("EditArticleActivity", "Failed to read the article: ${e.message}")
            }
        } else {
            Log.e("EditArticleActivity", "chatKey is empty")
        }
    }

}
