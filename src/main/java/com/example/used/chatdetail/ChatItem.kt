package com.example.used.chatdetail

data class ChatItem (
    val time: String,
    val senderId: String,
    val message: String
){
    constructor():this("","","")
}