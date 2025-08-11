package com.example.bookservice

import ch.tutteli.atrium.api.fluent.en_GB.its
import ch.tutteli.atrium.api.fluent.en_GB.toBeEmpty
import ch.tutteli.atrium.api.fluent.en_GB.toEqual
import ch.tutteli.atrium.api.fluent.en_GB.toHaveSize
import ch.tutteli.atrium.api.verbs.expect
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.AnnotationSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Suppress("unused")
@WebMvcTest(BookController::class)
@ContextConfiguration(classes = [BookAtriumControllerTest.TestConfig::class])
class BookAtriumControllerTest(var mockMvc: MockMvc, var bookService: BookService, var objectMapper: ObjectMapper) :
    AnnotationSpec() {

    @TestConfiguration
    class TestConfig {
        @Bean
        @Primary
        fun mockBookService(): BookService = mockk()
    }


    private val sampleBooks = listOf(
        Book(id = 1, title = "Test Book 1", author = "Author 1", genre = "Fiction"),
        Book(id = 2, title = "Test Book 2", author = "Author 2", genre = "Programming")
    )

    @BeforeEach
    fun setUp() {
        // Reset mocks before each test
        io.mockk.clearMocks(bookService)
    }

    @Test
    fun `should return all books with proper JSON structure`() {
        // Given
        every { bookService.getAllBooks() } returns sampleBooks

        // When & Then
        val result = mockMvc.perform(get("/books"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn()

        val responseBody = result.response.contentAsString
        val books = objectMapper.readValue(responseBody, Array<Book>::class.java).toList()

        expect(books).toHaveSize(2)
        expect(books[0].title).toEqual("Test Book 1")
        expect(books[0].author).toEqual("Author 1")
        expect(books[1].title).toEqual("Test Book 2")

        verify { bookService.getAllBooks() }
    }

    @Test
    fun `should return empty list when no books exist`() {
        // Given
        every { bookService.getAllBooks() } returns emptyList()

        // When & Then
        val result = mockMvc.perform(get("/books"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn()

        val responseBody = result.response.contentAsString
        val books = objectMapper.readValue(responseBody, Array<Book>::class.java).toList()

        expect(books).toBeEmpty()
        verify { bookService.getAllBooks() }
    }

    @Test
    fun `should accept and return book with 201 status`() {
        // Given
        val newBook = Book(title = "New Book", author = "New Author", genre = "New Genre")
        val savedBook = newBook.copy(id = 3)
        every { bookService.addBook(newBook) } returns savedBook

        // When & Then
        val result = mockMvc.perform(
            post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newBook))
        )
            .andExpect(status().isCreated)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn()

        val responseBody = result.response.contentAsString
        val returnedBook = objectMapper.readValue(responseBody, Book::class.java)

        expect(returnedBook.id).toEqual(3L)
        expect(returnedBook.title).toEqual("New Book")
        expect(returnedBook.author).toEqual("New Author")
        expect(returnedBook.genre).toEqual("New Genre")

        verify { bookService.addBook(newBook) }
    }

    @Test
    fun `should return 400 for invalid book data`() {
        // Given
        val invalidBook = Book(title = "", author = "Author", genre = "Genre")

        // When & Then
        mockMvc.perform(
            post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidBook))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should return book recommendation with 200 status`() {
        // Given
        val recommendedBook = sampleBooks[0]
        every { bookService.getRecommendation("Fiction") } returns recommendedBook

        // When & Then
        val result = mockMvc.perform(
            get("/books/recommendation")
                .param("genre", "Fiction")
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn()

        val responseBody = result.response.contentAsString
        val returnedBook = objectMapper.readValue(responseBody, Book::class.java)

        expect(returnedBook) {
            its { id }.toEqual(1L)
            its { title }.toEqual("Test Bok 1")
            its { genre }.toEqual("Fiction")
        }

        verify { bookService.getRecommendation("Fiction") }
    }

    @Test
    fun `should return 404 when no recommendation found`() {
        // Given
        every { bookService.getRecommendation("NonExistent") } returns null

        // When & Then
        mockMvc.perform(
            get("/books/recommendation")
                .param("genre", "NonExistent")
        )
            .andExpect(status().isNotFound)

        verify { bookService.getRecommendation("NonExistent") }
    }

    @Test
    fun `should handle recommendation without genre parameter`() {
        // Given
        val recommendedBook = sampleBooks[1]
        every { bookService.getRecommendation(null) } returns recommendedBook

        // When & Then
        val result = mockMvc.perform(get("/books/recommendation"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn()

        val responseBody = result.response.contentAsString
        val returnedBook = objectMapper.readValue(responseBody, Book::class.java)

        expect(returnedBook.id).toEqual(2L)
        expect(returnedBook.title).toEqual("Test Book 2")

        verify { bookService.getRecommendation(null) }
    }
}