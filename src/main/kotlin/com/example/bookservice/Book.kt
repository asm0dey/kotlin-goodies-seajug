package com.example.bookservice

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("BOOKS")
data class Book(
    @Id
    val id: Long? = null,
    val title: String,
    val author: String,
    val genre: String,
    val isbn: String? = null,
    val publishedYear: Int? = null
)