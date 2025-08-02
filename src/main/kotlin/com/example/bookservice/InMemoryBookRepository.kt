package com.example.bookservice

import org.springframework.stereotype.Repository
import java.util.concurrent.atomic.AtomicLong

@Repository
class InMemoryBookRepository : BookRepository {
    
    private val books = mutableListOf<Book>()
    private val idGenerator = AtomicLong(1)
    
    init {
        // Pre-populate with sample data
        books.addAll(listOf(
            Book(id = idGenerator.getAndIncrement(), title = "The Kotlin Programming Language", author = "JetBrains", genre = "Programming", isbn = "978-0123456789", publishedYear = 2023),
            Book(id = idGenerator.getAndIncrement(), title = "Clean Code", author = "Robert C. Martin", genre = "Programming", isbn = "978-0132350884", publishedYear = 2008),
            Book(id = idGenerator.getAndIncrement(), title = "The Lord of the Rings", author = "J.R.R. Tolkien", genre = "Fantasy", isbn = "978-0544003415", publishedYear = 1954),
            Book(id = idGenerator.getAndIncrement(), title = "1984", author = "George Orwell", genre = "Dystopian", isbn = "978-0451524935", publishedYear = 1949),
            Book(id = idGenerator.getAndIncrement(), title = "To Kill a Mockingbird", author = "Harper Lee", genre = "Fiction", isbn = "978-0061120084", publishedYear = 1960),
            Book(id = idGenerator.getAndIncrement(), title = "The Pragmatic Programmer", author = "David Thomas", genre = "Programming", isbn = "978-0201616224", publishedYear = 1999)
        ))
    }
    
    override fun findAll(): List<Book> {
        return books.toList()
    }
    
    override fun save(book: Book): Book {
        return if (book.id == null) {
            // Create new book with generated ID
            val savedBook = book.copy(id = idGenerator.getAndIncrement())
            books.add(savedBook)
            savedBook
        } else {
            // Update existing book
            val index = books.indexOfFirst { it.id == book.id }
            if (index >= 0) {
                books[index] = book
            } else {
                books.add(book)
            }
            book
        }
    }
    
    override fun findByGenre(genre: String): List<Book> {
        return books.filter { it.genre.equals(genre, ignoreCase = true) }
    }
    
    override fun findById(id: Long): Book? {
        return books.find { it.id == id }
    }
}