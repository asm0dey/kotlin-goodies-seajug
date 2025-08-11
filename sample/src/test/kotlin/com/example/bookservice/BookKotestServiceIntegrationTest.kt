package com.example.bookservice

import io.kotest.core.spec.style.ExpectSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotBeBlank
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookKotestServiceIntegrationTest(var restTemplate: TestRestTemplate, @LocalServerPort port: Int = 0) : ExpectSpec({

    var baseUrl = "http://localhost:$port"

    context("GET /books endpoint") {
        expect("should return pre-populated books") {
            // When
            val response = restTemplate.getForEntity("$baseUrl/books", Array<Book>::class.java)

            // Then
            response.statusCode shouldBe HttpStatus.OK
            response.body shouldNotBe null
            val books = response.body!!.toList()

            books shouldHaveSize 6 // Pre-populated sample data
            books.forEach { book ->
                book.id shouldNotBe null
                book.title.shouldNotBeBlank()
                book.author.shouldNotBeBlank()
                book.genre.shouldNotBeBlank()
            }

            // Verify some expected books from sample data
            books.any { it.title == "The Kotlin Programming Language" } shouldBe true
            books.any { it.title == "Clean Code" } shouldBe true
        }

    }

    context("POST /books endpoint") {
        expect("should create and return new book") {
            // Given
            val newBook = Book(
                title = "Integration Test Book",
                author = "Test Author",
                genre = "Testing",
                isbn = "978-1234567890",
                publishedYear = 2024
            )

            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON
            val request = HttpEntity(newBook, headers)

            // When
            val response = restTemplate.postForEntity("$baseUrl/books", request, Book::class.java)

            // Then
            response.statusCode shouldBe HttpStatus.CREATED
            response.body shouldNotBe null

            val savedBook = response.body!!
            savedBook.id shouldNotBe null
            savedBook.title shouldBe "Integration Test Book"
            savedBook.author shouldBe "Test Author"
            savedBook.genre shouldBe "Testing"
            savedBook.isbn shouldBe "978-1234567890"
            savedBook.publishedYear shouldBe 2024

            // Verify book was actually saved by retrieving all books
            val allBooksResponse = restTemplate.getForEntity("$baseUrl/books", Array<Book>::class.java)
            val allBooks = allBooksResponse.body!!.toList()
            allBooks shouldContain savedBook
        }

        expect("should return 400 for invalid book data") {
            // Given
            val invalidBook = Book(
                title = "", // Invalid: blank title
                author = "Test Author",
                genre = "Testing"
            )

            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON
            val request = HttpEntity(invalidBook, headers)

            // When
            val response = restTemplate.postForEntity("$baseUrl/books", request, String::class.java)

            // Then
            response.statusCode shouldBe HttpStatus.BAD_REQUEST
        }

    }

    context("GET /books/recommendation endpoint") {
        expect("should return recommendation for existing genre") {
            // When
            val response = restTemplate.getForEntity(
                "$baseUrl/books/recommendation?genre=Programming",
                Book::class.java
            )

            // Then
            response.statusCode shouldBe HttpStatus.OK
            response.body shouldNotBe null

            val recommendation = response.body!!
            recommendation.id shouldNotBe null
            recommendation.genre shouldBe "Programming"
            recommendation.title.shouldNotBeBlank()
            recommendation.author.shouldNotBeBlank()
        }

        expect("should return recommendation without genre parameter") {
            // When
            val response = restTemplate.getForEntity(
                "$baseUrl/books/recommendation",
                Book::class.java
            )

            // Then
            response.statusCode shouldBe HttpStatus.OK
            response.body shouldNotBe null

            val recommendation = response.body!!
            recommendation.id shouldNotBe null
            recommendation.title.shouldNotBeBlank()
            recommendation.author.shouldNotBeBlank()
            recommendation.genre.shouldNotBeBlank()
        }

        expect("should return 404 for non-existent genre") {
            // When
            val response = restTemplate.getForEntity(
                "$baseUrl/books/recommendation?genre=NonExistentGenre",
                String::class.java
            )

            // Then
            response.statusCode shouldBe HttpStatus.NOT_FOUND
        }

    }

    context("Book lifecycle") {
        expect("should handle complete book lifecycle") {
            // Step 1: Get initial book count
            val initialResponse = restTemplate.getForEntity("$baseUrl/books", Array<Book>::class.java)
            val initialCount = initialResponse.body!!.size

            // Step 2: Add a new book
            val newBook = Book(
                title = "Workflow Test Book",
                author = "Workflow Author",
                genre = "WorkflowTesting"
            )

            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON
            val createRequest = HttpEntity(newBook, headers)

            val createResponse =
                restTemplate.postForEntity("$baseUrl/books", createRequest, Book::class.java)
            createResponse.statusCode shouldBe HttpStatus.CREATED
            val savedBook = createResponse.body!!

            // Step 3: Verify book count increased
            val afterCreateResponse =
                restTemplate.getForEntity("$baseUrl/books", Array<Book>::class.java)
            afterCreateResponse.body!!.size shouldBe initialCount + 1

            // Step 4: Get recommendation for the new genre
            val recommendationResponse = restTemplate.getForEntity(
                "$baseUrl/books/recommendation?genre=WorkflowTesting",
                Book::class.java
            )
            recommendationResponse.statusCode shouldBe HttpStatus.OK
            recommendationResponse.body!!.genre shouldBe "WorkflowTesting"

            // Step 5: Verify the saved book is in the recommendation pool
            val recommendation = recommendationResponse.body!!
            recommendation.id shouldBe savedBook.id
        }
    }

    context("Error handling") {
        expect("should handle malformed JSON gracefully") {
            // Given
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON
            val malformedJson = "{ invalid json }"
            val request = HttpEntity(malformedJson, headers)

            // When
            val response = restTemplate.postForEntity("$baseUrl/books", request, String::class.java)

            // Then
            response.statusCode shouldBe HttpStatus.BAD_REQUEST
        }

        expect("should handle missing content type") {
            // Given
            val newBook = Book(title = "Test", author = "Author", genre = "Genre")
            val request = HttpEntity(newBook) // No content type header

            // When
            val response = restTemplate.postForEntity("$baseUrl/books", request, String::class.java)

            // Then - Should still work as Spring Boot handles this gracefully
            response.statusCode shouldBe HttpStatus.CREATED
        }
    }
})