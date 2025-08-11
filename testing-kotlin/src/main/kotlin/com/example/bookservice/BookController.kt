package com.example.bookservice

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/books")
class BookController(private val bookService: BookService) {
    
    @GetMapping
    fun getAllBooks(): ResponseEntity<Iterable<Book?>> {
        val books = bookService.getAllBooks()
        return ResponseEntity.ok(books)
    }
    
    @PostMapping
    fun addBook(@RequestBody book: Book): ResponseEntity<Book> {
        // Basic validation
        if (book.title.isBlank() || book.author.isBlank() || book.genre.isBlank()) {
            return ResponseEntity.badRequest().build()
        }
        
        val savedBook = bookService.addBook(book)
        return ResponseEntity.status(HttpStatus.CREATED).body(savedBook)
    }
    
    @GetMapping("/recommendation")
    fun getRecommendation(@RequestParam(required = false) genre: String?): ResponseEntity<Book> {
        val recommendation = bookService.getRecommendation(genre)
        return if (recommendation != null) {
            ResponseEntity.ok(recommendation)
        } else {
            ResponseEntity.notFound().build()
        }
    }
}