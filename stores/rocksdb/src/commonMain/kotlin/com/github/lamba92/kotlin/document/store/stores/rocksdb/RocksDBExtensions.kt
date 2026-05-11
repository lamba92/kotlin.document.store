package com.github.lamba92.kotlin.document.store.stores.rocksdb

import maryk.rocksdb.RocksDB
import maryk.rocksdb.RocksIterator

internal fun RocksDB.getString(key: String): String? = get(key.encodeToByteArray())?.decodeToString()

internal fun RocksDB.putString(
    key: String,
    value: String,
) = put(key.encodeToByteArray(), value.encodeToByteArray())

internal fun RocksDB.scanPrefix(prefix: String): PrefixScan = PrefixScan(newIterator(), prefix.encodeToByteArray())

internal class PrefixScan(
    private val source: RocksIterator,
    private val prefix: ByteArray,
) : Sequence<Pair<ByteArray, ByteArray>>, AutoCloseable {
    init {
        source.seek(prefix)
    }

    override fun iterator(): Iterator<Pair<ByteArray, ByteArray>> =
        iterator {
            while (source.isValid()) {
                val k = source.key()
                if (!k.startsWith(prefix)) break
                yield(k to source.value())
                source.next()
            }
        }

    override fun close() {
        source.close()
    }
}

internal fun ByteArray.startsWith(other: ByteArray): Boolean {
    if (size < other.size) return false
    for (i in other.indices) {
        if (this[i] != other[i]) return false
    }
    return true
}
