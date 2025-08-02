# Book Recommendation Service - Implementation Tasks

This document contains actionable tasks to implement the Book Recommendation Service demo project as described in `concept.md`. The project demonstrates modern Kotlin testing practices using Atrium, MockK, and Kotest in a Spring Boot environment.

## 1. Project Setup & Dependencies

### 1.1 Configure Build Dependencies
- [ ] Add Spring Boot Web starter dependency
- [ ] Add Spring Boot Test starter dependency
- [ ] Add Kotest dependencies (kotest-runner-junit5, kotest-assertions-core, kotest-property)
- [ ] Add MockK dependency (mockk)
- [ ] Add Atrium dependency (atrium-fluent-en_GB)
- [ ] Configure Kotlin compiler options for Spring
- [ ] Configure JUnit 5 test engine

**Acceptance Criteria:**
- All required dependencies are added to `build.gradle.kts`
- Project builds successfully
- Test framework is properly configured

### 1.2 Update Package Structure
- [ ] Rename main package from `com.github.asm0dey.testingkotlin` to `com.example.bookservice`
- [ ] Update `TestingKotlinApplication.kt` to `BookServiceApplication.kt`
- [ ] Update test package structure accordingly

**Acceptance Criteria:**
- Package structure matches the concept design
- Application starts successfully
- No compilation errors

## 2. Core Data Model

### 2.1 Implement Book Data Class
- [ ] Create `Book.kt` data class with properties:
  - `id: Long?` (nullable for new books)
  - `title: String`
  - `author: String`
  - `genre: String`
  - `isbn: String?` (optional)
  - `publishedYear: Int?` (optional)

**Acceptance Criteria:**
- Book data class is properly defined
- All required properties are included
- Data class follows Kotlin conventions

## 3. Repository Layer

### 3.1 Create BookRepository Interface
- [ ] Create `BookRepository.kt` interface with methods:
  - `findAll(): List<Book>`
  - `save(book: Book): Book`
  - `findByGenre(genre: String): List<Book>`
  - `findById(id: Long): Book?`

**Acceptance Criteria:**
- Repository interface is properly defined
- All CRUD operations are covered
- Methods return appropriate types

### 3.2 Create In-Memory Repository Implementation
- [ ] Create `InMemoryBookRepository.kt` implementing `BookRepository`
- [ ] Use mutable list to store books
- [ ] Implement auto-incrementing ID generation
- [ ] Add `@Repository` annotation
- [ ] Pre-populate with sample data

**Acceptance Criteria:**
- Repository implementation works correctly
- Sample data is available for testing
- All interface methods are implemented

## 4. Service Layer

### 4.1 Implement BookService
- [ ] Create `BookService.kt` with `@Service` annotation
- [ ] Inject `BookRepository` dependency
- [ ] Implement methods:
  - `getAllBooks(): List<Book>`
  - `addBook(book: Book): Book`
  - `getRecommendation(genre: String?): Book?`
  - `getBooksByGenre(genre: String): List<Book>`

**Acceptance Criteria:**
- Service class is properly annotated
- Business logic is implemented correctly
- Dependency injection works
- Recommendation logic handles edge cases (empty genre, no books found)

## 5. Controller Layer

### 5.1 Implement BookController
- [ ] Create `BookController.kt` with `@RestController` annotation
- [ ] Inject `BookService` dependency
- [ ] Implement REST endpoints:
  - `GET /books` - returns all books
  - `POST /books` - adds a new book
  - `GET /recommendation?genre=xyz` - returns random book recommendation

**Acceptance Criteria:**
- All endpoints are properly mapped
- Request/response handling works correctly
- HTTP status codes are appropriate
- Query parameters are handled properly

### 5.2 Add Request/Response Validation
- [ ] Add input validation for POST /books endpoint
- [ ] Add proper error handling and HTTP status codes
- [ ] Add request/response DTOs if needed

**Acceptance Criteria:**
- Invalid requests return appropriate error responses
- Validation messages are clear
- HTTP status codes follow REST conventions

## 6. Testing Implementation

### 6.1 BookService Tests with MockK
- [ ] Create `BookServiceTest.kt` using Kotest FunSpec or DescribeSpec
- [ ] Mock `BookRepository` using MockK
- [ ] Test scenarios:
  - Getting all books
  - Adding a new book
  - Getting recommendation with valid genre
  - Getting recommendation with invalid/empty genre
  - Getting books by genre

**Acceptance Criteria:**
- All service methods are tested
- MockK is used to mock repository
- Edge cases are covered
- Tests use Kotest spec style

### 6.2 BookController Tests with Atrium
- [ ] Create `BookControllerTest.kt` using Kotest
- [ ] Use `@WebMvcTest` for controller testing
- [ ] Mock `BookService` using MockK
- [ ] Use Atrium assertions for response validation
- [ ] Test all REST endpoints:
  - GET /books returns proper JSON structure
  - POST /books accepts and returns book
  - GET /recommendation returns book or 404

**Acceptance Criteria:**
- All controller endpoints are tested
- Atrium assertions are used for response validation
- HTTP status codes are verified
- JSON structure is validated

### 6.3 Kotest Assertions Demonstration
- [ ] Create examples using Kotest matchers in existing tests:
  - `shouldBe` for exact matches
  - `shouldContain` for collection assertions
  - `shouldNotBeNull` for null checks
  - `shouldHaveSize` for collection size
  - Custom matchers for Book validation

**Acceptance Criteria:**
- Various Kotest matchers are demonstrated
- Assertions are readable and expressive
- Custom matchers are implemented where appropriate

### 6.4 Property-Based Testing
- [ ] Create `BookPropertyTest.kt` in `test/kotlin/com/example/bookservice/property/`
- [ ] Use Kotest property testing to generate random Book instances
- [ ] Test invariants:
  - Book title is never blank
  - Book author is never blank
  - Genre is never blank
  - Service always returns valid books
  - Repository operations maintain data integrity

**Acceptance Criteria:**
- Property-based tests are implemented
- Random data generation works correctly
- Business invariants are tested
- Tests run with multiple iterations

### 6.5 Integration Tests
- [ ] Create `BookServiceIntegrationTest.kt`
- [ ] Use `@SpringBootTest` for full application context
- [ ] Test complete request/response flow
- [ ] Use TestRestTemplate or WebTestClient
- [ ] Combine Atrium and Kotest assertions

**Acceptance Criteria:**
- Full application integration is tested
- Real HTTP requests/responses are validated
- Database operations work end-to-end
- Multiple testing frameworks work together

## 7. Advanced Testing Features

### 7.1 Custom Assertions
- [ ] Create custom Atrium assertions for Book validation
- [ ] Create custom Kotest matchers for business rules
- [ ] Examples:
  - `toBeValidBook()` assertion
  - `toHaveValidIsbn()` matcher
  - `toBeRecommendableFor(genre)` assertion

**Acceptance Criteria:**
- Custom assertions are reusable
- Assertions provide clear error messages
- Business rules are encapsulated in assertions

### 7.2 Test Data Builders
- [ ] Create test data builders for Book instances
- [ ] Use builder pattern for flexible test data creation
- [ ] Support for generating books with specific genres
- [ ] Integration with property-based testing

**Acceptance Criteria:**
- Test data creation is simplified
- Builders are flexible and reusable
- Integration with existing tests works

## 8. Documentation & Examples

### 8.1 Update README
- [ ] Add project description and setup instructions
- [ ] Document API endpoints with examples
- [ ] Add testing framework usage examples
- [ ] Include build and run instructions

**Acceptance Criteria:**
- README is comprehensive and clear
- Setup instructions work for new developers
- API documentation is complete

### 8.2 Add Code Comments
- [ ] Add KDoc comments to public APIs
- [ ] Document testing strategies in test files
- [ ] Add inline comments for complex business logic
- [ ] Document testing framework usage patterns

**Acceptance Criteria:**
- Code is well-documented
- Testing patterns are explained
- New developers can understand the codebase

## 9. Quality Assurance

### 9.1 Test Coverage
- [ ] Ensure all public methods are tested
- [ ] Verify edge cases are covered
- [ ] Check integration between components
- [ ] Validate error handling scenarios

**Acceptance Criteria:**
- High test coverage (>80%)
- All critical paths are tested
- Error scenarios are handled

### 9.2 Code Quality
- [ ] Run static analysis tools
- [ ] Ensure consistent code formatting
- [ ] Verify Kotlin idioms are followed
- [ ] Check for potential bugs or issues

**Acceptance Criteria:**
- Code follows Kotlin best practices
- No critical issues in static analysis
- Consistent formatting throughout

## 10. Final Validation

### 10.1 End-to-End Testing
- [ ] Start the application
- [ ] Test all endpoints manually or with Postman
- [ ] Verify all testing frameworks work together
- [ ] Run complete test suite

**Acceptance Criteria:**
- Application starts without errors
- All endpoints work as expected
- All tests pass
- Testing frameworks demonstrate their features effectively

### 10.2 Demo Preparation
- [ ] Prepare sample requests for each endpoint
- [ ] Create test scenarios that showcase each testing framework
- [ ] Document the testing approach and benefits
- [ ] Prepare presentation materials if needed

**Acceptance Criteria:**
- Demo scenarios are ready
- Each testing framework's benefits are clear
- Project effectively demonstrates modern Kotlin testing practices