package mx.atriz

import kotlin.test.Test
import kotlin.test.assertTrue

class GreetingTest {
    @Test
    fun greetReturnsNonEmptyString() {
        val greeting = Greeting().greet()
        assertTrue(greeting.isNotEmpty())
        assertTrue(greeting.contains("Hello"))
    }
}
