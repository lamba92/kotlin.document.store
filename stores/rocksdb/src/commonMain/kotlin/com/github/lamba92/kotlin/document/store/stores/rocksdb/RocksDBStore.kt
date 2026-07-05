package com.github.lamba92.kotlin.document.store.stores.rocksdb

import com.github.lamba92.kotlin.document.store.core.AbstractDataStore
import com.github.lamba92.kotlin.document.store.core.DataStore
import com.github.lamba92.kotlin.document.store.core.PersistentMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import maryk.rocksdb.RocksDB
import maryk.rocksdb.WriteBatch
import maryk.rocksdb.WriteOptions
import maryk.rocksdb.openRocksDB

/**
 * A [RocksDB] implementation of the [DataStore] for persistent storage.
 *
 * `RocksDBStore` uses RocksDB to provide a disk-backed, reliable, and high-performance
 * key-value storage system for managing named maps. It supports creating, retrieving,
 * and deleting persistent maps, with each map uniquely identified by a prefix.
 *
 * This implementation ensures thread-safe access to data operations using synchronization
 * mechanisms while leveraging RocksDB's [WriteBatch] for efficient map management.
 *
 * It is particularly suited for use cases that require fast sequential reads/writes
 * and efficient use of disk storage.
 */
public class RocksDBStore(
    private val delegate: RocksDB,
) : AbstractDataStore() {
    public companion object {
        /**
         * Opens a new [RocksDBStore] instance at the specified path.
         *
         * Intermediate directories are **NOT** created if they do not exist. The database will be created
         * at the specified path as a directory.
         *
         * @param path The file system path where the RocksDB database will be created or accessed.
         * @return A new `RocksDBStore` instance backed by the RocksDB database at the specified path.
         */
        public fun open(path: String): RocksDBStore = RocksDBStore(openRocksDB(path))
    }

    override suspend fun getMap(name: String): PersistentMap<String, String> =
        withStoreLock {
            RocksDBPersistentMap(
                delegate = delegate,
                prefix = name,
                mutex = getMutex(name),
            )
        }

    override suspend fun deleteMap(name: String): Unit =
        withStoreLock {
            lockAndRemoveMutex(name) { delegate.deletePrefix(name) }
        }

    override suspend fun close() {
        withContext(Dispatchers.IO) {
            delegate.close()
        }
    }
}

internal suspend fun RocksDB.deletePrefix(prefix: String) =
    withContext(Dispatchers.IO) {
        scanPrefix(prefix).use { scan ->
            batch {
                for ((key, _) in scan) {
                    delete(key)
                }
                delete("sizes.$prefix".encodeToByteArray())
            }
        }
    }

internal fun RocksDB.batch(block: WriteBatch.() -> Unit) {
    WriteBatch().use { batch ->
        batch.block()
        WriteOptions().use { opts -> write(opts, batch) }
    }
}
