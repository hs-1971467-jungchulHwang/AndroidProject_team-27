package com.example.used.home


data class ArticleModel(
    val sellerId: String,
    var title: String,
    val createdAt: Long,
    var price: String,
    val imageUrl: String,
    //val userName: String,
    var status: String = Status.ONSALE.name,
    var description: String,
    val chatKey: String? = null
){
    constructor(): this("","",0,"","", "", "")
}
