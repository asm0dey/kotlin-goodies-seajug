Project Development Guidelines (Advanced)

This document captures project-specific details to help you build, test, and evolve this Kotlin/Spring Boot application efficiently.

1. Build and Configuration
- Toolchain
  - JDK: 17 (configured via Gradle Toolchains).
  - Kotlin: 1.9.25 with kotlin("jvm") and kotlin("plugin.spring").
  - Spring Boot: 3.5.4 with io.spring.dependency-management 1.1.7.
  - Gradle: Use the provided wrapper (./gradlew) to ensure correct version alignment.
- Main dependencies
  - Spring: spring-boot-starter-web, spring-boot-starter-data-jdbc, spring-boot-starter-jdbc.
  - Database: H2 (runtimeOnly).
  - JSON: jackson-module-kotlin.
- Test stack (via JUnit Platform)
  - Kotest 5.9.1 (runner-junit5, assertions-core, property, spring extension).
  - MockK 1.14.5 for mocking.
  - Atrium 1.2.0 for fluent assertions in some tests.
  - Kotest HTML and JUnit XML reporters are enabled by ProjectConfig (see src/test/kotlin/ProjectConfig.kt).
- Build commands
  - Build without tests: ./gradlew assemble
  - Full build with tests: ./gradlew build
  - Run the app locally: ./gradlew bootRun
- Reports configuration
  - Gradle’s default test reports are disabled in build.gradle.kts (tasks.test { reports.html/xml = false }).
  - Kotest reporters still generate output:
    - JUnit XML: build/test-results/test (standard JUnit XML from kotest reporter).
    - Kotest HTML: build/reports/kotest/index.html (from HtmlReporter).

2. Runtime and Data Initialization
- In-memory DB: H2 (jdbc:h2:mem:testdb) is used for both dev and tests (src/main/resources/application.properties).
- SQL init is always on:
  - spring.sql.init.mode=always
  - schema: classpath:schema.sql
  - data: classpath:data.sql
- H2 console (dev aid):
  - Enabled at /h2-console
  - Driver: org.h2.Driver, user: sa, password: password (defaults from properties).

3. Testing
3.1 How tests are structured
- Framework: JUnit Platform with Kotest specs and plain JUnit 5 tests coexisting.
- Spring integration:
  - @SpringBootTest used in integration tests (random port) with TestRestTemplate.
  - Kotest’s SpringExtension is globally enabled via ProjectConfig (no per-spec wiring required).
- DB state & isolation:
  - schema.sql/data.sql load on context start.
  - Some tests use @Transactional and/or repository cleanup in beforeEach to ensure isolation (e.g., BookPropertyTest deletes all rows before each property run). H2 resets on context restart.

3.2 How to run tests
- Run all tests: ./gradlew test
  - Note: There is an intentionally failing property-based test (see below). Running all tests will fail due to that demonstration.
- Run a specific test class (recommended when you want green runs):
  - ./gradlew test --tests com.example.bookservice.BookServiceTest
  - ./gradlew test --tests com.example.bookservice.BookControllerTest
  - ./gradlew test --tests com.example.bookservice.BookServiceIntegrationTest
- Run multiple specific classes by repeating --tests flags:
  - ./gradlew test --tests com.example.bookservice.BookServiceTest --tests com.example.bookservice.BookControllerTest
- Run by simple pattern (Gradle filters by fully-qualified class names):
  - ./gradlew test --tests "com.example.bookservice.*"  # matches classes directly in that package (not subpackages)

3.3 Intentionally failing test (for property-based shrinking demo)
- File: src/test/kotlin/com/example/bookservice/property/BookPropertyTest.kt
- Spec: "FAILING TEST: Book titles should not exceed 50 characters (demonstrates shrinking)"
- Purpose: Showcases Kotest shrinking with a generated title that can exceed 50 chars, violating the constraint.
- Impact: ./gradlew test (with no filters) will fail. Use targeted class filters (above) to run only the tests you need.
- If you explicitly want to observe the shrinking output, run just this class:
  - ./gradlew test --tests com.example.bookservice.property.BookPropertyTest

3.4 Adding and running a new test (verified example)
- Minimal JUnit-platform-compatible test using Kotest matchers:
  - Example class (we created and validated locally by running only this class):
    - package com.example
      
      import org.junit.jupiter.api.Test
      import io.kotest.matchers.shouldBe
      
      class SanityTest {
          @Test
          fun `sanity addition works`() {
              (2 + 2) shouldBe 4
          }
      }
  - File location: src/test/kotlin/com/example/SanityTest.kt
  - Run just this test (works and was verified):
    - ./gradlew test --tests com.example.SanityTest
  - Clean-up: This file was only for demonstration and has been removed to keep the repo clean.

3.5 Writing tests in this project
- Prefer Kotest styles (DescribeSpec, StringSpec) already used in the repo. JUnit @Test works fine for SpringBootTest-style classes.
- Use MockK for unit-level tests that stub repositories/services; see BookServiceTest and BookControllerTest.
- For web layer tests, use @WebMvcTest with mocked dependencies and Spring’s MockMvc (see BookControllerTest).
- For integration tests, use @SpringBootTest(webEnvironment = RANDOM_PORT) with TestRestTemplate (see BookServiceIntegrationTest). These hit the in-memory H2 DB and real HTTP endpoints.
- For property-based testing, leverage io.kotest.property.Arb and checkAll with shrinking; see BookPropertyTest for complex generators and invariants.

4. Additional Development Notes
- Code style & conventions
  - Idiomatic Kotlin data classes and null-safety. Book uses nullable fields for id and some optional properties.
  - Prefer Kotest matchers for readability; Atrium is used where fluent English-like assertions help in MVC tests.
  - Keep tests deterministic except where randomness is explicitly part of the test (e.g., recommendations select a random book). In those cases, assert membership rather than exact equality.
- Running the application for manual testing
  - ./gradlew bootRun then call endpoints:
    - GET /books
    - POST /books (application/json)
    - GET /books/recommendation?genre=Programming
  - The DB is transient (H2 in-memory). Restarting the app resets data to schema.sql/data.sql.
- Debugging tests
  - Enable detailed Gradle logs: ./gradlew test --info --stacktrace
  - To observe Kotest shrinking output, run only the failing property test (see 3.3).
  - Spring test slices: prefer @WebMvcTest for controller-only tests to avoid booting the whole context.
- Reports
  - Kotest HTML: build/reports/kotest/index.html
  - JUnit XML: build/test-results/test (useful for CI tooling).
- CI considerations
  - Because of the intentional failing property test, CI pipelines should target specific suites or explicitly exclude that spec until the constraint becomes an actual requirement. Use Gradle test filters to include only stable suites.

5. Quick Commands Reference
- Build: ./gradlew build
- Run app: ./gradlew bootRun
- Run all tests (will fail due to demo property test): ./gradlew test
- Run selected stable tests:
  - ./gradlew test --tests com.example.bookservice.BookServiceTest
  - ./gradlew test --tests com.example.bookservice.BookControllerTest
  - ./gradlew test --tests com.example.bookservice.BookServiceIntegrationTest
- Observe Kotest HTML report: open build/reports/kotest/index.html

Notes
- No external services are required; everything runs locally against H2.
- The example test execution (SanityTest) was actually run and succeeded; the file was then removed to keep the repository unchanged except for this guidelines document.
