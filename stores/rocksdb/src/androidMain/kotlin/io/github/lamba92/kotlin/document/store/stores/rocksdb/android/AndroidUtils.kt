package io.github.lamba92.kotlin.document.store.stores.rocksdb.android

import android.content.Context
import io.github.lamba92.kotlin.document.store.stores.rocksdb.RocksDBStore
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories

/**
 * Opens a new [RocksDBStore] instance in the storage directory of the app.
 *
 * Intermediate directories are created if they do not exist.
 *
 * @param name The name of the database directory (default is `"rocksdb"`).
 * @return A new [RocksDBStore] instance backed by the RocksDB database at the specified path.
 */
public fun Context.openRocksDBStore(name: String = "rocksdb"): RocksDBStore =
    Path(getDatabasePath(name).path)
        .createDirectories()
        .let { RocksDBStore.open(it.absolutePathString()) }
