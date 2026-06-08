package com.github.lamba92.kotlin.document.store.stores.browser

import com.github.lamba92.indexeddb.IdbTextStore
import com.github.lamba92.kotlin.document.store.core.PersistentMap
import com.github.lamba92.kotlin.document.store.core.SerializableEntry
import com.github.lamba92.kotlin.document.store.core.UpdateResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * A browser-based implementation of `PersistentMap` backed by IndexedDB.
 *
 * Every named map shares a single IndexedDB object store ([store]); entries are namespaced by
 * a `<name>.` key prefix so distinct maps coexist without colliding. IndexedDB object stores
 * can only be created inside a `versionchange` upgrade, so a store-per-map layout would force a
 * version bump on each new collection — prefixing within one store avoids that at the cost of
 * prefix-scanning for whole-map operations ([size], [clear], [entries]).
 */
public class IndexedDBMap(
    private val name: String,
    private val store: IdbTextStore,
    private val mutex: Mutex,
) : PersistentMap<String, String> {
    public companion object {
        private const val SEPARATOR = "."

        internal fun buildPrefix(name: String) = "$name$SEPARATOR"
    }

    private val prefixed
        get() = buildPrefix(name)

    private fun String.prefixed() = "$prefixed$this"

    override suspend fun clear(): Unit =
        store
            .keys()
            .filter { it.startsWith(prefixed) }
            .forEach { store.delete(it) }

    override suspend fun size(): Long =
        store
            .keys()
            .count { it.startsWith(prefixed) }
            .toLong()

    override suspend fun isEmpty(): Boolean = size() == 0L

    override suspend fun get(key: String): String? = store.get(key.prefixed())

    override suspend fun put(
        key: String,
        value: String,
    ): String? = mutex.withLock { unsafePut(key, value) }

    private suspend fun unsafePut(
        key: String,
        value: String,
    ): String? {
        val previous = store.get(key.prefixed())
        store.put(key.prefixed(), value)
        return previous
    }

    override suspend fun remove(key: String): String? =
        mutex.withLock {
            val previous = store.get(key.prefixed())
            store.delete(key.prefixed())
            previous
        }

    override suspend fun containsKey(key: String): Boolean = get(key) != null

    override suspend fun update(
        key: String,
        value: String,
        updater: (String) -> String,
    ): UpdateResult<String> =
        mutex.withLock {
            val oldValue = store.get(key.prefixed())
            val newValue = oldValue?.let(updater) ?: value
            store.put(key.prefixed(), newValue)
            UpdateResult(oldValue, newValue)
        }

    override suspend fun getOrPut(
        key: String,
        defaultValue: () -> String,
    ): String =
        mutex.withLock {
            store.get(key.prefixed()) ?: defaultValue().also { unsafePut(key, it) }
        }

    override fun entries(): Flow<Map.Entry<String, String>> =
        flow {
            store
                .keys()
                .asFlow()
                .filter { it.startsWith(prefixed) }
                .collect { key ->
                    store.get(key)?.let { value ->
                        emit(SerializableEntry(key.removePrefix(prefixed), value))
                    }
                }
        }
}
