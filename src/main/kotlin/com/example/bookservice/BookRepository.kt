package com.example.bookservice

interface BookRepository {
    fun findAll(): List<Book>
    fun save(book: Book): Book
    fun findByGenre(genre: String): List<Book>
    fun findById(id: Long): Book?
}