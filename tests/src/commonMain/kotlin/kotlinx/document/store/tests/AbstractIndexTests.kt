@file:Suppress("FunctionName")

package kotlinx.document.store.tests

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestResult
import kotlinx.document.store.core.getObjectCollection
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Abstract base class for testing index functionality within a document store.
 *
 * It extends [BaseTest] and includes a set of tests to validate the behavior of indexes,
 * such as their creation before or after inserts and updates to the collections. These tests
 * ensure proper indexing mechanisms for ensuring optimized search and retrieval operations.
 *
 * This class is intended to be extended to implement platform-specific behaviors for `DataStoreProvider`.
 */
public abstract class AbstractIndexTests(store: DataStoreProvider) : BaseTest(store) {
    public companion object {
        public val json: Json =
            Json {
                prettyPrint = true
                allowStructuredMapKeys = true
            }

        public const val TEST_NAME_1: String = "id_is_correctly_increased_after_insert"
        public const val TEST_NAME_2: String = "index_is_correctly_created_after_insert"
        public const val TEST_NAME_3: String = "index_is_correctly_created_before_insert"
        public const val TEST_NAME_4: String = "index_is_correctly_created_after_insert_and_update"
        public const val TEST_NAME_5: String = "object_index_is_created_before_insert_test"
    }

    @Test
    public fun idIsCorrectlyIncreasedAfterInsert(): TestResult =
        runDatabaseTest(TEST_NAME_1) { db ->
            val collection = db.getObjectCollection<TestUser>("test")
            collection.insert(TestUser.Mario)

            val marioId =
                collection.jsonCollection.iterateAll()
                    .first { it["name"]?.jsonPrimitive?.content == TestUser.Mario.name }["_id"]
                    ?.jsonPrimitive
                    ?.long
                    ?: error("No id found")

            collection.insert(TestUser.Luigi)

            val luigiId =
                collection.jsonCollection.iterateAll()
                    .first { it["name"]?.jsonPrimitive?.content == TestUser.Luigi.name }["_id"]
                    ?.jsonPrimitive
                    ?.long
                    ?: error("No id found")

            println(json.encodeToString(db.databaseDetails()))
            assertTrue(luigiId > marioId, "Luigi's id should be greater than Mario's id")
        }

    @Test
    public fun indexIsCorrectlyCreatedAfterInsert(): TestResult =
        runDatabaseTest(TEST_NAME_2) { db ->
            val collection = db.getObjectCollection<TestUser>("test")
            val userId = collection.insert(TestUser.Mario).id ?: error("No id found")
            collection.createIndex("name")

            println(json.encodeToString(db.databaseDetails()))
            assertEquals(
                expected = userId,
                actual = collection.getIndex("name")?.get(JsonPrimitive(TestUser.Mario.name))?.single(),
                message = "Index should have 1 element",
            )
        }

    @Test
    public fun indexIsCorrectlyCreatedBeforeInsert(): TestResult =
        runDatabaseTest(TEST_NAME_3) { db ->
            val collection = db.getObjectCollection<TestUser>("test")
            collection.createIndex("name")
            val userId = collection.insert(TestUser.Mario).id ?: error("No id found")

            assertEquals(
                expected = userId,
                actual = collection.getIndex("name")?.get(JsonPrimitive(TestUser.Mario.name))?.single(),
                message = "Index should have 1 element",
            )
        }

    @Test
    public fun indexIsCorrectlyCreatedAfterInsertAndUpdate(): TestResult =
        runDatabaseTest(TEST_NAME_4) { db ->
            val collection = db.getObjectCollection<TestUser>("test")
            val marioId = collection.insert(TestUser.Mario).id ?: error("No id found")
            collection.createIndex("name")

            assertEquals(
                expected = marioId,
                actual = collection.getIndex("name")?.get(JsonPrimitive(TestUser.Mario.name))?.single(),
                message = "Index should have 1 element",
            )
            collection.removeById(marioId)
            val luigiId = collection.insert(TestUser.Luigi).id ?: error("No id found")
            println(json.encodeToString(db.databaseDetails()))

            val actual = collection.getIndex("name")?.get(JsonPrimitive(TestUser.Luigi.name))?.single()
            assertEquals(
                expected = luigiId,
                actual = actual,
                message = "Index should have 1 element",
            )
        }

    @Test
    public fun objectIndexIsCreatedBeforeInsertTest(): TestResult =
        runDatabaseTest(TEST_NAME_5) { db ->
            val collection = db.getObjectCollection<TestUser>("test")
            collection.createIndex("addresses.$0")
            collection.createIndex("addresses.$1")
            collection.createIndex("addresses.$2")

            val marioId = collection.insert(TestUser.Mario).id ?: error("No id found")
            val luigiId = collection.insert(TestUser.Luigi).id ?: error("No id found")

            val collectionDetails = collection.details()
            println(json.encodeToString(collectionDetails))
            assertEquals(3, collectionDetails.indexes.size)
            assertEquals(2, collectionDetails.indexes["addresses.$0"]?.size)
            assertEquals(2, collectionDetails.indexes["addresses.$1"]?.size)
            assertEquals(1, collectionDetails.indexes["addresses.$2"]?.size)

            assertEquals(
                expected = marioId,
                actual =
                    collectionDetails.indexes
                        .getValue("addresses.$0")
                        .getValue(collection.json.encodeToJsonElement(TestUser.Mario.addresses[0]))
                        .first(),
                message = "Index should have 1 element",
            )

            assertEquals(
                expected = luigiId,
                actual =
                    collectionDetails.indexes
                        .getValue("addresses.$0")
                        .getValue(collection.json.encodeToJsonElement(TestUser.Luigi.addresses[0]))
                        .first(),
                message = "Index should have 1 element",
            )
        }
}
