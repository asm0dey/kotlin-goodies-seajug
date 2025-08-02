package com.example.bookservice

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class BookServiceTest : DescribeSpec({
    
    val mockRepository = mockk<BookRepository>()
    val bookService = BookService(mockRepository)
    
    val sampleBooks = listOf(
        Book(id = 1, title = "Test Book 1", author = "Author 1", genre = "Fiction"),
        Book(id = 2, title = "Test Book 2", author = "Author 2", genre = "Programming"),
        Book(id = 3, title = "Test Book 3", author = "Author 3", genre = "Fiction")
    )
    
    describe("BookService") {
        
        describe("getAllBooks") {
            it("should return all books from repository") {
                // Given
                every { mockRepository.findAll() } returns sampleBooks
                
                // When
                val result = bookService.getAllBooks()
                
                // Then
                result shouldBe sampleBooks
                verify { mockRepository.findAll() }
            }
            
            it("should return empty list when no books exist") {
                // Given
                every { mockRepository.findAll() } returns emptyList()
                
                // When
                val result = bookService.getAllBooks()
                
                // Then
                result shouldHaveSize 0
                verify { mockRepository.findAll() }
            }
        }
        
        describe("addBook") {
            it("should save and return book") {
                // Given
                val newBook = Book(title = "New Book", author = "New Author", genre = "New Genre")
                val savedBook = newBook.copy(id = 4)
                every { mockRepository.save(newBook) } returns savedBook
                
                // When
                val result = bookService.addBook(newBook)
                
                // Then
                result shouldBe savedBook
                result.id shouldNotBe null
                verify { mockRepository.save(newBook) }
            }
        }
        
        describe("getRecommendation") {
            it("should return random book from specified genre") {
                // Given
                val fictionBooks = sampleBooks.filter { it.genre == "Fiction" }
                every { mockRepository.findByGenre("Fiction") } returns fictionBooks
                
                // When
                val result = bookService.getRecommendation("Fiction")
                
                // Then
                result shouldNotBe null
                fictionBooks shouldContain result!!
                verify { mockRepository.findByGenre("Fiction") }
            }
            
            it("should return null when no books found for genre") {
                // Given
                every { mockRepository.findByGenre("NonExistent") } returns emptyList()
                
                // When
                val result = bookService.getRecommendation("NonExistent")
                
                // Then
                result shouldBe null
                verify { mockRepository.findByGenre("NonExistent") }
            }
            
            it("should return random book from all books when genre is null") {
                // Given
                every { mockRepository.findAll() } returns sampleBooks
                
                // When
                val result = bookService.getRecommendation(null)
                
                // Then
                result shouldNotBe null
                sampleBooks shouldContain result!!
                verify { mockRepository.findAll() }
            }
            
            it("should return random book from all books when genre is blank") {
                // Given
                every { mockRepository.findAll() } returns sampleBooks
                
                // When
                val result = bookService.getRecommendation("")
                
                // Then
                result shouldNotBe null
                sampleBooks shouldContain result!!
                verify { mockRepository.findAll() }
            }
            
            it("should return null when no books exist and genre is null") {
                // Given
                every { mockRepository.findAll() } returns emptyList()
                
                // When
                val result = bookService.getRecommendation(null)
                
                // Then
                result shouldBe null
                verify { mockRepository.findAll() }
            }
        }
        
        describe("getBooksByGenre") {
            it("should return books filtered by genre") {
                // Given
                val fictionBooks = sampleBooks.filter { it.genre == "Fiction" }
                every { mockRepository.findByGenre("Fiction") } returns fictionBooks
                
                // When
                val result = bookService.getBooksByGenre("Fiction")
                
                // Then
                result shouldBe fictionBooks
                result shouldHaveSize 2
                verify { mockRepository.findByGenre("Fiction") }
            }
            
            it("should return empty list when no books found for genre") {
                // Given
                every { mockRepository.findByGenre("NonExistent") } returns emptyList()
                
                // When
                val result = bookService.getBooksByGenre("NonExistent")
                
                // Then
                result shouldHaveSize 0
                verify { mockRepository.findByGenre("NonExistent") }
            }
        }
    }
})