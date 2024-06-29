package kotlinx.document.database.maps

import com.github.lamba92.kotlin.db.PersistentMap
import com.github.lamba92.kotlin.db.SimpleEntry
import com.github.lamba92.kotlin.db.UpdateResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

fun PersistentMap<String, String>.asCollectionMap() = CollectionMap(this)

class CollectionMap(private val delegate: PersistentMap<String, String>) : PersistentMap<Long, String> {

    override suspend fun clear() =
        delegate.clear()

    override suspend fun size(): Long =
        delegate.size()

    override suspend fun isEmpty(): Boolean =
        delegate.isEmpty()

    override fun close() {
        delegate.close()
    }

    override suspend fun get(key: Long): String? =
        delegate.get(key.toString())

    override suspend fun put(key: Long, value: String): String? =
        delegate.put(key.toString(), value)

    override suspend fun remove(key: Long): String? =
        delegate.remove(key.toString())

    override suspend fun containsKey(key: Long): Boolean =
        delegate.containsKey(key.toString())

    override suspend fun update(key: Long, value: String, updater: (String) -> String): UpdateResult<String> =
        delegate.update(
            key = key.toString(),
            value = value,
            updater = { updater(it) }
        )

    override suspend fun getOrPut(key: Long, defaultValue: () -> String): String =
        delegate.getOrPut(
            key = key.toString(),
            defaultValue = { defaultValue() }
        )

    override fun entries(): Flow<Map.Entry<Long, String>> =
        delegate.entries().map { SimpleEntry(it.key.toLong(), it.value) }
}
