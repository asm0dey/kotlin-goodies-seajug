package com.example.bookservice

data class Book(
    val id: Long? = null,
    val title: String,
    val author: String,
    val genre: String,
    val isbn: String? = null,
    val publishedYear: Int? = null
)