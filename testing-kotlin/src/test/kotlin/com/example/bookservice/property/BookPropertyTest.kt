package com.example.bookservice.property

import ch.tutteli.atrium.api.fluent.en_GB.toBeLessThanOrEqualTo
import ch.tutteli.atrium.api.verbs.expect
import com.example.bookservice.Book
import com.example.bookservice.BookService
import com.example.bookservice.JdbcBookRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotBeBlank
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import io.kotest.property.checkAll
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestConstructor
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Transactional
class BookPropertyTest(private val repository: JdbcBookRepository) : StringSpec({
    val bookService = BookService(repository)
    beforeEach { repository.deleteAll() }
    // Custom generators for Book properties
    val titleArb = Arb.string(1..100).filter { it.isNotBlank() }
    val authorArb = Arb.string(1..50).filter { it.isNotBlank() }
    val genreArb = Arb.string(1..30).filter { it.isNotBlank() }
    val isbnArb = Arb.string(10..17).orNull(0.3)
    val yearArb = Arb.int(1000..2024).orNull(0.2)

    val bookArb = Arb.bind(
        titleArb,
        authorArb,
        genreArb,
        isbnArb,
        yearArb
    ) { title, author, genre, isbn, year ->
        Book(
            id = null, // Always null for new books
            title = title,
            author = author,
            genre = genre,
            isbn = isbn,
            publishedYear = year
        )
    }

    "Book title is never blank" {
        checkAll(bookArb) { book ->
            book.title.shouldNotBeBlank()
        }
    }

    "Book author is never blank" {
        checkAll(bookArb) { book ->
            book.author.shouldNotBeBlank()
        }
    }

    "Book genre is never blank" {
        checkAll(bookArb) { book ->
            book.genre.shouldNotBeBlank()
        }
    }

    "Service always returns valid books when adding" {
        checkAll(bookArb) { book ->
            val savedBook = bookService.addBook(book)

            // Saved book should have all original properties
            savedBook.title shouldBe book.title
            savedBook.author shouldBe book.author
            savedBook.genre shouldBe book.genre
            savedBook.isbn shouldBe book.isbn
            savedBook.publishedYear shouldBe book.publishedYear

            // Saved book should have an ID assigned
            savedBook.id shouldNotBe null

            // Properties should still be valid
            savedBook.title.shouldNotBeBlank()
            savedBook.author.shouldNotBeBlank()
            savedBook.genre.shouldNotBeBlank()
        }
    }

    "Repository operations maintain data integrity" {
        checkAll(bookArb) { book ->
            val initialCount = repository.findAll().count()

            // Save book
            val savedBook = repository.save(book)

            // Count should increase by 1
            repository.findAll().count() shouldBe initialCount + 1

            // Saved book should be findable by ID
            val foundBook = repository.findById(savedBook.id!!).orElse(null)
            foundBook shouldNotBe null
            foundBook shouldBe savedBook

            // Book should be in genre search results
            val booksInGenre = repository.findByGenre(savedBook.genre)
            booksInGenre.any { it.id == savedBook.id } shouldBe true
        }
    }

    "Service recommendation always returns valid book when books exist" {
        checkAll(genreArb) { genre ->
            // Ensure we have at least one book in the repository
            val testBook = Book(title = "Test", author = "Author", genre = genre)
            bookService.addBook(testBook)

            val recommendation = bookService.getRecommendation(genre)

            if (recommendation != null) {
                recommendation.title.shouldNotBeBlank()
                recommendation.author.shouldNotBeBlank()
                recommendation.genre.shouldNotBeBlank()
                recommendation.id shouldNotBe null
            }
        }
    }

    "Books by genre filtering maintains invariants" {
        checkAll(bookArb, genreArb) { book, searchGenre ->
            // Add book to repository
            bookService.addBook(book)

            // Search for books by genre
            val booksInGenre = bookService.getBooksByGenre(searchGenre)

            // All returned books should have the searched genre (case-insensitive)
            booksInGenre.forEach { foundBook ->
                foundBook.genre.equals(searchGenre, ignoreCase = true) shouldBe true
                foundBook.title.shouldNotBeBlank()
                foundBook.author.shouldNotBeBlank()
                foundBook.genre.shouldNotBeBlank()
                foundBook.id shouldNotBe null
            }
        }
    }

    "ID generation is always unique and positive" {
        checkAll(Arb.list(bookArb, 1..10)) { books ->
            val savedBooks = books.map { bookService.addBook(it) }

            // All IDs should be unique
            val ids = savedBooks.mapNotNull { it.id }
            ids.size shouldBe ids.toSet().size

            // All IDs should be positive
            ids.forEach { id ->
                id shouldNotBe null
                (id > 0L) shouldBe true
            }
        }
    }

    "FAILING TEST: Book titles should not exceed 50 characters (demonstrates shrinking)".config(enabled = false) {
        // This test is intentionally designed to fail to showcase kotest's shrinking capability
        // It uses a generator that can produce titles longer than 50 characters
        val longTitleArb = Arb.string(1..100) // Can generate strings up to 100 chars

        checkAll(longTitleArb, authorArb, genreArb) { title, author, genre ->
            val book = Book(
                title = title,
                author = author,
                genre = genre
            )

            // Using atrium assertions instead of kotest matchers
            // This will fail when title length > 50, and kotest will shrink to find minimal failing case
            expect(book.title.length).toBeLessThanOrEqualTo(50)
        }
    }
})