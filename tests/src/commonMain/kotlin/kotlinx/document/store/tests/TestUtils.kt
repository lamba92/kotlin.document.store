package kotlinx.document.store.tests

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.days

@Serializable
public data class TestUser(
    val name: String,
    val age: Int,
    val isAdult: Boolean = true,
    val birthDate: Instant = if (isAdult) Clock.System.now() - (365 * 20).days else Clock.System.now(),
    val addresses: List<Address> = emptyList(),
    @SerialName("_id") val id: Long? = null,
) {
    public companion object {
        public val Mario: TestUser =
            TestUser(
                name = "mario",
                age = 20,
                addresses =
                    listOf(
                        Address("Mushroom Kingdom", 1),
                        Address("Peach's Castle", 2),
                        Address("New York", 3),
                    ),
            )
        public val Luigi: TestUser =
            TestUser(
                name = "luigi",
                age = 20,
                addresses =
                    listOf(
                        Address("Sarasaland", 1),
                        Address("Daisy's Castle", 2),
                        Address("New York", 3),
                    ),
            )

        public fun generateUsers(count: Int): Sequence<TestUser> =
            sequence {
                var counter = 0
                while (true) {
                    yield(TestUser("user$counter", counter))
                    counter++
                }
            }.take(count)
    }
}

@Serializable
public data class Address(
    val street: String,
    val number: Int,
)