package com.github.lamba92.indexeddb

import kotlinx.coroutines.suspendCancellableCoroutine
import org.khronos.webgl.Int8Array
import org.khronos.webgl.get
import org.khronos.webgl.set
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal external interface IDBRequest {
    var onsuccess: (() -> Unit)?
    var onerror: (() -> Unit)?
    val result: Any?
}

internal external interface IDBOpenDBRequest : IDBRequest {
    override val result: IDBDatabaseHandle
    var onupgradeneeded: (() -> Unit)?
}

internal external interface IDBFactory {
    fun open(
        name: String,
        version: Int,
    ): IDBOpenDBRequest
}

internal external interface IDBStringList {
    fun contains(value: String): Boolean
}

internal external interface IDBDatabaseHandle {
    val objectStoreNames: IDBStringList

    fun createObjectStore(name: String): IDBObjectStore

    fun transaction(
        storeNames: String,
        mode: String,
    ): IDBTransaction

    fun close()
}

internal external interface IDBTransaction {
    fun objectStore(name: String): IDBObjectStore
}

internal external interface IDBObjectStore {
    fun get(key: String): IDBRequest

    fun put(
        value: Any?,
        key: String,
    ): IDBRequest

    fun delete(key: String): IDBRequest

    fun clear(): IDBRequest

    fun getAllKeys(): IDBRequest

    fun count(): IDBRequest
}

private fun idbFactory(): IDBFactory = js("indexedDB")

public actual suspend fun openIdb(
    name: String,
    version: Int,
    stores: Set<String>,
): IdbDatabase {
    val request = idbFactory().open(name, version)
    val handle =
        suspendCancellableCoroutine { continuation ->
            request.onupgradeneeded = {
                val db = request.result
                stores.forEach { store -> if (!db.objectStoreNames.contains(store)) db.createObjectStore(store) }
            }
            request.onsuccess = { continuation.resume(request.result) }
            request.onerror = {
                continuation.resumeWithException(IllegalStateException("Failed to open IndexedDB '$name'"))
            }
        }
    return IdbDatabase(handle)
}

public actual class IdbDatabase internal constructor(
    private val handle: IDBDatabaseHandle,
) : AutoCloseable {
    public actual fun bytes(store: String): IdbByteStore = IdbByteStore(handle, store)

    public actual fun text(store: String): IdbTextStore = IdbTextStore(handle, store)

    actual override fun close(): Unit = handle.close()
}

public actual class IdbByteStore internal constructor(
    private val handle: IDBDatabaseHandle,
    private val store: String,
) {
    private fun store(mode: String): IDBObjectStore = handle.transaction(store, mode).objectStore(store)

    public actual suspend fun get(key: String): ByteArray? {
        val request = store("readonly").get(key)
        request.await()
        return (request.result as? Int8Array)?.toByteArray()
    }

    public actual suspend fun put(
        key: String,
        value: ByteArray,
    ): Unit = store("readwrite").put(value.toInt8Array(), key).await()

    public actual suspend fun delete(key: String): Unit = store("readwrite").delete(key).await()

    public actual suspend fun clear(): Unit = store("readwrite").clear().await()

    public actual suspend fun keys(): List<String> {
        val request = store("readonly").getAllKeys()
        request.await()
        @Suppress("UNCHECKED_CAST")
        return (request.result as Array<String>).toList()
    }

    public actual suspend fun count(): Long {
        val request = store("readonly").count()
        request.await()
        return (request.result as Int).toLong()
    }

    public actual suspend fun size(key: String): Long? {
        val request = store("readonly").get(key)
        request.await()
        return (request.result as? Int8Array)?.length?.toLong()
    }
}

public actual class IdbTextStore internal constructor(
    private val handle: IDBDatabaseHandle,
    private val store: String,
) {
    private fun store(mode: String): IDBObjectStore = handle.transaction(store, mode).objectStore(store)

    public actual suspend fun get(key: String): String? {
        val request = store("readonly").get(key)
        request.await()
        return request.result as? String
    }

    public actual suspend fun put(
        key: String,
        value: String,
    ): Unit = store("readwrite").put(value, key).await()

    public actual suspend fun delete(key: String): Unit = store("readwrite").delete(key).await()

    public actual suspend fun clear(): Unit = store("readwrite").clear().await()

    public actual suspend fun keys(): List<String> {
        val request = store("readonly").getAllKeys()
        request.await()
        @Suppress("UNCHECKED_CAST")
        return (request.result as Array<String>).toList()
    }

    public actual suspend fun count(): Long {
        val request = store("readonly").count()
        request.await()
        return (request.result as Int).toLong()
    }

    public actual suspend fun entries(): List<Pair<String, String>> = keys().mapNotNull { key -> get(key)?.let { key to it } }
}

private suspend fun IDBRequest.await(): Unit =
    suspendCancellableCoroutine { continuation ->
        onsuccess = { continuation.resume(Unit) }
        onerror = { continuation.resumeWithException(IllegalStateException("IndexedDB request failed")) }
    }

private fun ByteArray.toInt8Array(): Int8Array {
    val array = Int8Array(size)
    for (i in indices) array[i] = this[i]
    return array
}

private fun Int8Array.toByteArray(): ByteArray {
    val bytes = ByteArray(length)
    for (i in 0 until length) bytes[i] = this[i]
    return bytes
}
