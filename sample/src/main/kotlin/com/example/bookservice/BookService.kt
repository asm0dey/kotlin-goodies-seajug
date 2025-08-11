package com.example.bookservice

import org.springframework.stereotype.Service

@Service
class BookService(private val bookRepository: JdbcBookRepository) {
    
    fun getAllBooks(): Iterable<Book?> {
        return bookRepository.findAll()
    }
    
    fun addBook(book: Book): Book {
        return bookRepository.save(book)
    }
    
    fun getRecommendation(genre: String?): Book? {
        return if (genre.isNullOrBlank()) {
            // If no genre specified, return random book from all books
            val allBooks = bookRepository.findAll().toList()
            if (allBooks.isEmpty()) null else allBooks.random()
        } else {
            // Return random book from specified genre
            val booksInGenre = bookRepository.findByGenre(genre)
            if (booksInGenre.isEmpty()) null else booksInGenre.random()
        }
    }
    
    fun getBooksByGenre(genre: String): List<Book> {
        return bookRepository.findByGenre(genre)
    }
}