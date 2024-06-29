package kotlinx.document.database

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.h2.mvstore.MVMap

class MVPersistentMap<K, V>(
    private val delegate: MVMap<K, V>,
) : PersistentMap<K, V> {

    override suspend fun get(key: K): V? =
        withContext(Dispatchers.IO) { delegate[key] }

    override suspend fun put(key: K, value: V) =
        withContext(Dispatchers.IO) { delegate.put(key, value) }

    override suspend fun remove(key: K): V? =
        withContext(Dispatchers.IO) { delegate.remove(key) }

    override suspend fun containsKey(key: K) =
        withContext(Dispatchers.IO) { delegate.containsKey(key) }

    override suspend fun clear() =
        withContext(Dispatchers.IO) { delegate.clear() }

    override suspend fun size(): Long =
        withContext(Dispatchers.IO) { delegate.sizeAsLong() }

    override suspend fun isEmpty() =
        withContext(Dispatchers.IO) { delegate.isEmpty() }

    override fun entries(): Flow<Map.Entry<K, V>> =
        delegate.entries
            .asFlow()
            .flowOn(Dispatchers.IO)

    private val mutex = Mutex()

    override suspend fun getOrPut(key: K, defaultValue: () -> V): V =
        withContext(Dispatchers.IO) {
            mutex.withLock {
                delegate.getOrPut(key, defaultValue)
            }
        }

    override suspend fun update(key: K, value: V, updater: (V) -> V): UpdateResult<V> =
        withContext(Dispatchers.IO) {
            mutex.withLock {
                val oldValue = delegate[key]
                val newValue = oldValue?.let(updater) ?: value
                delegate[key] = newValue
                UpdateResult(oldValue, newValue)
            }
        }

    override fun close() {}

}