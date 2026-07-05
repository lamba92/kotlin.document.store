package com.github.lamba92.kotlin.document.store.stores.rocksdb

import com.github.lamba92.kotlin.document.store.core.PersistentMap
import com.github.lamba92.kotlin.document.store.core.SerializableEntry
import com.github.lamba92.kotlin.document.store.core.UpdateResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import maryk.rocksdb.RocksDB

/**
 * A [PersistentMap] implementation backed by RocksDB.
 *
 * `RocksDBPersistentMap` provides a disk-based key-value store where each entry
 * is prefixed with a unique namespace (prefix) to distinguish multiple maps stored
 * in the same RocksDB instance.
 */
public class RocksDBPersistentMap(
    private val delegate: RocksDB,
    private val prefix: String,
    private val mutex: Mutex,
) : PersistentMap<String, String> {
    private fun String.prefixed() = "$prefix.$this"

    override suspend fun get(key: String): String? =
        withContext(Dispatchers.IO) {
            delegate.getString(key.prefixed())
        }

    override suspend fun put(
        key: String,
        value: String,
    ): String? =
        mutex.withLock {
            val previousValue = delegate.getString(key.prefixed())
            val previousSize = delegate.getString("sizes.$prefix")?.toLong()
            delegate.batch {
                put(key.prefixed().encodeToByteArray(), value.encodeToByteArray())
                val nextSize =
                    when (previousSize) {
                        null -> "1"
                        else -> (previousSize + 1).toString()
                    }
                put("sizes.$prefix".encodeToByteArray(), nextSize.encodeToByteArray())
            }
            previousValue
        }

    override suspend fun remove(key: String): String? =
        withContext(Dispatchers.IO) {
            mutex.withLock {
                val prefixed = key.prefixed()
                val previous = delegate.getString(prefixed)
                delegate.delete(prefixed.encodeToByteArray())
                delegate
                    .getString("sizes.$prefix")
                    ?.toLong()
                    ?.let { delegate.putString("sizes.$prefix", (it - 1).toString()) }
                previous
            }
        }

    override suspend fun containsKey(key: String): Boolean = get(key) != null

    override suspend fun clear(): Unit = delegate.deletePrefix(prefix)

    override suspend fun size(): Long =
        withContext(Dispatchers.IO) {
            delegate.getString("sizes.$prefix")?.toLong() ?: 0L
        }

    override suspend fun isEmpty(): Boolean = size() == 0L

    override fun entries(): Flow<Map.Entry<String, String>> {
        val scan = delegate.scanPrefix("$prefix.")
        return scan
            .map { (k, v) ->
                SerializableEntry(
                    k.decodeToString().removePrefix("$prefix."),
                    v.decodeToString(),
                )
            }.asFlow()
            .onCompletion { scan.close() }
    }

    override suspend fun getOrPut(
        key: String,
        defaultValue: () -> String,
    ): String =
        mutex.withLock {
            withContext(Dispatchers.IO) {
                delegate.getString(key.prefixed())
                    ?: defaultValue().also { delegate.putString(key.prefixed(), it) }
            }
        }

    override suspend fun update(
        key: String,
        value: String,
        updater: (String) -> String,
    ): UpdateResult<String> =
        mutex.withLock {
            withContext(Dispatchers.IO) {
                val previous = delegate.getString(key.prefixed())
                val newValue = previous?.let(updater) ?: value
                delegate.putString(key.prefixed(), newValue)
                UpdateResult(previous, newValue)
            }
        }
}
