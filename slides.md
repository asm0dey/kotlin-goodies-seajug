---
# try also 'default' to start simple
theme: seriph
# random image from a curated Unsplash collection by Anthony
# like them? see https://unsplash.com/collections/94734566/slidev
# background: https://source.unsplash.com/collection/94734566/1920x1080
# apply any windi css classes to the current slide
class: "text-center"
# https://sli.dev/custom/highlighters.html
highlighter: shiki
# show line numbers in code blocks
lineNumbers: true
# persist drawings in exports and build
drawings:
  persist: false
# page transition
# transition: fade
# use UnoCSS
css: unocss
colorSchema: "dark"
background: black
download: "https://asm0dey.github.io/kotlin-goodies-seajug/index.pdf"
# aspect ratio for the slides
aspectRatio: 16/9
# real width of the canvas, unit in px
canvasWidth: 800
---

# Kotlin Goodies for Testing

## Pasha Finkelshteyn, BellSoft

---

## layout: two-cols

# `whoami`

- <logos-kotlin-icon /><logos-java /><logos-scala /><logos-clojure/> developer
- Average <logos-python /> enjoyer
- Developer <emojione-avocado /> at BellSoft
- Data engineer
- Speaker <twemoji-microphone />

::right::

![](/avatar.jpg)

---

layout: image
image: /glitched-slide.webp

---

---

## layout: center

# Who are you?

1. You write code/tests in Java or Kotlin
2. You wanna migrate to Kotlin, _or_
3. You wanna make your tests in Kotlin better, _or_
4. You wanna introduce Kotlin in your projects!

---

## layout: center

# How are we doing it?

1. Using DSLs (domain specific languages)
2. Structuring our code better
3. Using smart libraries

---

layout: image-right
image: /bdd.png

---

# BDD

1. Given
2. When
3. Then

---

## clicks: 3

# Cucumber

```gherkin {all|5}
Feature: An example
  Scenario: The example
    Given I have a username "pasha"
    When I type username in field "x"
    And Press OK button
    Then I get an error "Enter password"
```

<div v-click-hide="1">

What's the problem here?

</div>
<div v-click-hide="2">
<div v-click="1">

This line might intersect with **any** test

</div>
</div>
<div v-click="2">

Actually ****any**** line can

</div>
<div v-click="3">

Steps match by **name** O_o:

```java
@And("Press OK button")
void press_ok_button(){/*TODO*/}
```

</div>

---

## How can we do it in Kotlin?

Kotest: https://kotest.io
<br/>

- No obscure text files
- No string matching
- everything in one place

```kotlin {all|1|2|3,8|4,9|5,10|all}{maxHeight:'200px'}
class MyTests : BehaviorSpec({
    given("a broomstick") {
        `when`("I sit on it") {
            then("I should be able to fly") {
                // test code
            }
        }
        `when`("I throw it away") {
            then("it should come back") {
                // test code
            }
        }
    }
})
```

---

## Another flavor of Kotest

Kotest: https://kotest.io
<br/>

```kotlin {all|2|3|4-6|8-9}
class MyTests : DescribeSpec({
    describe("score") {
        it("start as zero") { /* test here */ }
        describe("with a strike") {
            it("adds ten") { /* test here */ }
            it("carries strike to the next frame") { /* test here */ }
        }
        describe("for the opposite team") {
            it("Should negate one score") { /* test here */ }
        }
    }
})
```

---

# Migrating from JUnit?

```kotlin {all|1|2|2-5|6-9|all}
class AnnotationSpecExample : AnnotationSpec() {
    @BeforeEach
    fun beforeTest() {
        println("Before each test")
    }
    @Test
    fun test1() {
        1 shouldBe 1
    }
    @Test
    fun test2() {
        3 shouldBe 3
    }
}
```

---

# explanation `shouldBe` better

Kotest has its own assertions

<v-clicks>

- kotest-assertions-core
- kotest-assertions-json
- kotlintest-assertions-jsoup
- And namy others

</v-clicks>

---

# Core Assertions

```kotlin {all|1-3|4-5|7,8|9,10}
1 shouldBe 1
1 shouldNotBe 2
"Hello" shouldBe "Hello"
listOf(1,2,3) shouldContain 2
mapOf("a" to 1, "b" to 2) shouldContainKey "a"
null shouldBe null
"abc" shouldStartWith "a"
"xyz" shouldEndWith "z"
5 shouldBeGreaterThan 3
2 shouldBeLessThan 5
```

---

# JSON Assertions

```kotlin {1,2|3}
"{}".shouldBeValidJson()
"{}".shouldBeJsonObject()
a.shouldEqualJson(b, compareJsonOptions { arrayOrder = ArrayOrder.Strict })
```

Or even…

---

# Schema Validation

```kotlin {1|2|3|4-8|5-7|6|9|13|15|16|20-23|25-26}{maxHeight:'300px'}
val addressSchema = jsonSchema {
  obj {   // object is reserved, obj was chosen over jsonObject for brevity but could be changed ofc, or jsonObject could be added as alternative.
    withProperty("street", required = true) { string() }
    withProperty("zipCode", required = true) {
      integer {
        beEven() and beInRange(10000..99999)   // supports constructing a matcher that will be used to test values
      }
    }
    additionalProperties = false   // triggers failure if other properties are defined in actual
  }
}

val personSchema = jsonSchema {
  obj {
    withProperty("name", required = true) { string() }
    withProperty("address") { addressSchema() } // Schemas can re-use other schemas 🎉
  }
}

"{}" shouldMatchSchema personSchema

// fails with:
// $.name => Expected string, but was undefined

""" { "name": "Emil", "age": 34 } """
// Passes, since address isn't required and `additionalProperties` are allowed
```

---

# Jsoup

```kotlin
element.shouldHaveChildWithTag(tag)
element.shouldHaveText(text)
element.shouldHaveAttribute(name)
element.shouldHaveSrc(src)
```

---

# 50 shades of kotest

https://kotest.io

|                |                 |
| -------------: | --------------- |
|       Fun spec | String Spec     |
|    Should spec | Describe spec   |
| Behaviour spec | Word spec       |
|      Free spec | Feature spec    |
|    Expect spec | Annotation spec |

---

## layout: center

# How do I run the same tests with different inputs?

## Data-driven testing a.k.a Parametrized tests

---

# Parametrized tests? Java

```java {all|7-11|8|1-6|1|2|3|2-4|all}{maxHeight:'150px'}
static Stream<Arguments> stringIntAndListProvider() {
    return Stream.of(
        arguments("apple", 1, Arrays.asList("a", "b")),
        arguments("lemon", 2, Arrays.asList("x", "y"))
    );
}
@ParameterizedTest
@MethodSource("stringIntAndListProvider")
void testWithMultiArgMethodSource(String str, int num, List<String> list) {/
    // snip
}
```

<v-click>

`stringIntAndListProvider`, really?

</v-click>
<v-clicks>

- Becomes cluttered
- Not typed (enough)
- Bound by a string!

</v-clicks>

---

# Parametrized tests? Kotlin (with kotest)

```kotlin {all|1|2|3-6|7|8}
context("Pythag triples tests") {
  withData(
    PythagTriple(3, 4, 5),
    PythagTriple(6, 8, 10),
    PythagTriple(8, 15, 17),
    PythagTriple(7, 24, 25)
  ) { (a, b, c) ->
    isPythagTriple(a, b, c) shouldBe true
  }
}
```

---

layout: image
image: /confused.png

---

# What if

# I need to test things I don't know I need to test?

<style>
h1 {
    color: white !important;
    text-align: center;
}
</style>

---

## clicks: 5

# Never write your own hashing (unless…)

```kotlin {all|1|2-4|6|all}
withData(
    "love",
    "secret",
    "god", // Who will remember the reference?
) { input ->
    MD5.compute(input) shouldBe libMd5(input)
}
```

<p v-click="4">Launching…</p>
<div v-click="5">

1. <openmoji-check-mark/> love
1. <openmoji-check-mark/> secret
1. <openmoji-check-mark/> god

</div>

---

layout: image
image: /lens.png

---

# Time to investigate!

<style>
h1 {
    color: white !important;
    text-align: center;
}
</style>

---

# Property tests!

## To test things we can not even _imagine_

---

## layout: statement

# Demo

---

# Another example

```kotlin {all|1|2|3|4|6|8|9}
checkAll(
    Arb.string(
        codepoints = Arb.of(
            (Char.MIN_VALUE.code..Char.MAX_VALUE.code).map(::Codepoint)
        ),
        range = 0..52
    )
) { input ->
    MD5.compute(input) shouldBe libMd5(input)
}
```

---

layout: image
image: /explode.png

---

---

# Error!

```txt {all|1|3|5}
Property test failed for inputs

0) "꽪Ԉ㴨啣នഄ騿挋③ꩶ괖✷⃮㴧砑琶㋉뇛寵嚱땒箂醉℉ꫴ짥Ẩ鼏콋鹗맼㯖Ở㑾ᜫ綦講뼞仦ꄁ黺鄙ᾓ㤫川陝泴"

Caused by io.kotest.assertions.AssertionFailedError: expected:<"90b23a970ed1b799850a24140ae8ad39"> but was:<"5a430f76b3d544a13bf4b80710a174a0"> at
```

Wow, it was a complex sample!

But there is more…

---

# Error!

```txt {all|1|2|3|4|5|6|7|8|9}
Attempting to shrink arg "꽪Ԉ㴨啣នഄ騿挋③ꩶ괖✷⃮㴧砑琶㋉뇛寵嚱땒箂醉℉ꫴ짥Ẩ鼏콋鹗맼㯖Ở㑾ᜫ綦講뼞仦ꄁ黺鄙ᾓ㤫川陝泴"
Shrink #1: "꽪Ԉ㴨啣នഄ騿挋③ꩶ괖✷⃮㴧砑琶㋉뇛寵嚱땒箂醉℉" fail
Shrink #2: "꽪Ԉ㴨啣នഄ騿挋③ꩶ괖✷" fail
Shrink #3: "꽪Ԉ㴨啣នഄ" fail
Shrink #4: "꽪Ԉ㴨" fail
Shrink #5: "꽪Ԉ" fail
Shrink #6: "꽪" fail
Shrink #7: <empty string> pass
Shrink result (after 7 shrinks) => "꽪"
```

And there is more!

---

# Error!

```txt {all|1|3|5}
Property failed after 1 attempts

	Arg 0: "꽪" (shrunk from 꽪Ԉ㴨啣នഄ騿挋③ꩶ괖✷⃮㴧砑琶㋉뇛寵嚱땒箂醉℉ꫴ짥Ẩ鼏콋鹗맼㯖Ở㑾ᜫ綦講뼞仦ꄁ黺鄙ᾓ㤫川陝泴)

Repeat this test by using seed 4919313423746630853
```

---

# Repeat? Yes!

```kotlin {all|1,2}
checkAll(
    PropTestConfig(seed = 4919313423746630853),
    Arb.string(
        codepoints = Arb.of(
            ('a'.code..Char.MAX_HIGH_SURROGATE.code)
                .map(::Codepoint)
        ),
    )
) { input ->
    MD5.compute(input) shouldBe libMd5(input)
}
```

- reproduce the test **exactly** like in the failed instance
- debug it
- fix it
- PROFIT!!

---

layout: image
image: /mockingbird.png

---

<h1 v-click="1">Time to mock!</h1>

<style>
h1 {
    color: white !important;
    text-align: center;
}
</style>

---

# Disclaimer

I believe one should mock, not fake. Because
<br/>

1. Granular control
2. Can check that something was NOT called

---

# Time to mock

#### Setup

```kotlin {all|1-3|1|2|5-7|5|6|9}
class UserService(val repo: UserRepository) {
    fun getAllUsers() = repo.allUsers()
}

class UserRepository {
    fun allUsers(): List<User> { TODO("demo") }
}

class User(val name: String, val age: Int)
```

---

# How the test looks in Java

```java {all|1|3,4|6,7|9,10|12,13|15,16}
UserRepository mockRepository = Mockito.mock(UserRepository.class);

// Create some sample user data
List<User> users = Arrays.asList(new User("Alice", 25), new User("Bob", 30), new User("Charlie", 35));

// Define the expected behavior of the mock repository
Mockito.when(mockRepository.allUsers()).thenReturn(users);

// Create an instance of the UserService with the mock repository
UserService userService = new UserService(mockRepository);

// Call the method under test
List<User> result = userService.getAllUsers();

// Verify the result
Assertions.assertEquals(users, result);
```

---

# Time to mock! (MockK edition)

```kotlin {all|1|2|3|5|6}
val users = listOf(User("Alice", 25), User("Bob", 30), User("Charlie", 35))
val repo = mockk<UserRepository> {
    every { allUsers() } returns users
}
val service = UserService(repo)
service.getAllUsers() shouldBe users
```

<div v-click>

Nice DSL

Code describing `UserRepository` is inside `{}`

What if I want to mock users too?

</div>

---

# Time to mock users! (mockk edition)

```kotlin {all|1|2|3-5|6-8|11,12}
val repo = mockk<UserRepository> {
    every { allUsers() } returns listOf(
        mockk {
            every { name } returns "Pasha"
        },
        mockk {
            every { name } returns "Alexandra"
        },
    )
}
val service = UserService(repo)
service.getAllUsers()[1].name shouldBe "Alexandra"
```

<div v-click>

Users are described inside `User` mocks!

</div>

---

# Atrium expectations!

https://docs.atriumlib.org/

```kotlin {all|1|3|3-4|7-8}
expect(service.getAllUsers()) toContain o inAny order but only the entries(
    {
        its { name } toEqual "Pash"
        its { age } toEqual 39
    },
    {
        its { name } toEqual "Mark"
        its { age } toEqual 16
    }
)
```

<div v-click="2">

I've made a mistake to show the error message

</div>

---

# Error report

```txt {all|1|3|13-14|4-5|all}
I expected subject: [User(#3), User(#4)]        (java.util.Arrays.ArrayList <315506631>)
◆ to contain only, in any order:
  ✘ an element which needs:
      » ▶ its.definedIn(MockTest.kt:42):
          ◾ to equal: "Pash"        <2026086646>
      » ▶ its.definedIn(MockTest.kt:43):
          ◾ to equal: 36        (kotlin.Int <116289638>)
  ✔ an element which needs:
      » ▶ its.definedIn(MockTest.kt:46):
          ◾ to equal: "Mark"        <574921197>
      » ▶ its.definedIn(MockTest.kt:47):
          ◾ to equal: 13        (kotlin.Int <453296736>)
  ❗❗ following elements were mismatched:
     ⚬ User(#3)        (User <1041341319>)
```

<div v-click>

- Readable as a text
- Shows the errors

</div>

---

# Dessert

Now, when we saw different nice DSL-using capabilities…

```kotlin
ideaTest<App>("Create Kafka connection") {
    showBDTPanel {
        createBaseConnection("Kafka") {
            fillCredentialsForKafka(connectionName)
            checkSuccessfulMessage()
            ok()
        }
        checkMonitoringConnection(monitoringToolWindow)
    }
}
```

---

layout: image
image: /magick.png

---

<h1 style="color:white">How does it work? Magick!</h1>

<Link to="39">Dive deeper</Link>

---

layout: image
image: /question.png

---

# What did we learn?

<style>
h1 {
    color: darkorange;
    text-align: center;
}
</style>

---

# What did we learn?

1. Tests should be readable
2. Testing is not "just unit tests" sometimes
3. Kotest gives good structure to the tests.
4. Kotest has built-in property tests
5. Atrium gives us assertions DSL

---

layout: cover
background: /monk.png

---

# Thank you! Time for questions 😄

<div class="text-left">
<p><ph-twitter-logo-fill/> asm0di0</p>
<p><ph-envelope-simple-open-fill/> me@asm0dey.site</p>
<p><simple-icons-bluesky/> @asm0dey.site</p>
<p><ph-github-logo-fill/><ph-instagram-logo-fill/><ph-linkedin-logo-fill/><ph-telegram-logo-fill/> asm0dey</p>
<p><ph-globe-duotone/><a href="https://linktr.ee/asm0dey">https://linktr.ee/asm0dey</a></p>
</div>

---

## layout: end

---

# Deeper dive into JetBrains tests

```kotlin
ideaTest<App>("Create Kafka connection") {
```

really means:

```kotlin {all|1|4|6,7}
fun <T : App> Group.ideaTest(title: String,
                             enabled: Boolean = true,
                             isMuted: Boolean = false,
                             testBody: T.(ctx: IdeaNodeContext<T>) -> Unit) {
    IdeaNodeContext<T>(scenarioContext).apply {
        test(title, enabled, isMuted) {
            app.testBody(this)
        }
    }
}
```

---

# Deeper dive into JetBrains tests

```kotlin
    showBDTPanel {
```

really means:

```kotlin {all|1|3|5|6|9-11}
fun App.showBDTPanel(block: BDTPanel.() -> Unit) {
    val app = this
    ideaFrame {
        val frame = this
        step("Open Big Data Tools Panel") {
            openBDTPanel(app)
            if (isKafka()) {
                try {
                    container(byXpath("//div[@accessiblename='Kafka Tool Window' and @class='InternalDecoratorImpl']")) {
                        BDTPanel(app, frame, this).block()
                    }
```

---

# Deeper dive into JetBrains tests

```kotlin
        createBaseConnection("Kafka") {
```

really means:

```kotlin {all|1|2|4|5}
fun createBaseConnection(typeOfConnection: String, block: ConnectionDialog.() -> Unit) {
    step("Open connection dialog") {
        if (app.isKafka()) {
            frame.find<ComponentFixture>(byXpath("//div[contains(@tooltiptext.key, 'toolwindow.tab.add')]")).click()
            clickTypeOfConnection(typeOfConnection, block)
            pause(5000)
```

---

## layout: center

# And this is the abyss!

But you can do the same!

<Link to="42">Go back to summary</Link>

---

## layout: end
