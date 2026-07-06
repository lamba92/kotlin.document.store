package io.github.lamba92.kotlin.document.store.rocksdb

import io.github.lamba92.kotlin.document.store.stores.rocksdb.RocksDBStore
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories

/**
 * Opens a new [RocksDBStore] instance at the specified path.
 *
 * Intermediate directories are created if they do not exist. The database will be created
 * at the specified path as a directory.
 *
 * @param path The file system path where the RocksDB database will be created or accessed.
 * @return A new `RocksDBStore` instance backed by the RocksDB database at the specified path.
 */
public fun RocksDBStore.Companion.open(path: Path): RocksDBStore = open(path.createDirectories().absolutePathString())
