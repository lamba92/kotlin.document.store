@file:OptIn(ExperimentalWasmJsInterop::class)

package com.github.lamba92.indexeddb

import kotlinx.coroutines.suspendCancellableCoroutine
import org.khronos.webgl.Int8Array
import org.khronos.webgl.get
import org.khronos.webgl.set
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal external interface IDBRequest : JsAny {
    var onsuccess: ((JsAny) -> Unit)?
    var onerror: ((JsAny) -> Unit)?
}

internal external interface IDBOpenDBRequest : IDBRequest {
    val result: IDBDatabaseHandle
    var onupgradeneeded: ((JsAny) -> Unit)?
}

internal external interface IDBFactory : JsAny {
    fun open(
        name: String,
        version: Int,
    ): IDBOpenDBRequest
}

internal external interface IDBDatabaseHandle : JsAny {
    fun createObjectStore(name: String): JsAny

    fun transaction(
        storeNames: String,
        mode: String,
    ): IDBTransaction

    fun close()
}

internal external interface IDBTransaction : JsAny {
    fun objectStore(name: String): IDBObjectStore
}

internal external interface IDBObjectStore : JsAny {
    fun get(key: JsAny?): IDBRequest

    fun put(
        value: JsAny?,
        key: JsAny?,
    ): IDBRequest

    fun delete(key: JsAny?): IDBRequest

    fun clear(): IDBRequest

    fun getAllKeys(): IDBRequest

    fun count(): IDBRequest
}

private fun idbFactory(): IDBFactory = js("indexedDB")

private fun hasStore(
    db: IDBDatabaseHandle,
    name: String,
): Boolean = js("db.objectStoreNames.contains(name)")

private fun int8Result(req: IDBRequest): Int8Array? = js("req.result")

private fun stringResult(req: IDBRequest): JsString? = js("req.result")

private fun keysResult(req: IDBRequest): JsArray<JsString> = js("req.result")

private fun intResult(req: IDBRequest): Int = js("req.result")

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
                stores.forEach { if (!hasStore(db, it)) db.createObjectStore(it) }
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
        val request = store("readonly").get(key.toJsString())
        awaitRequest(request)
        return int8Result(request)?.toByteArray()
    }

    public actual suspend fun put(
        key: String,
        value: ByteArray,
    ): Unit = awaitRequest(store("readwrite").put(value.toInt8Array(), key.toJsString()))

    public actual suspend fun delete(key: String): Unit = awaitRequest(store("readwrite").delete(key.toJsString()))

    public actual suspend fun clear(): Unit = awaitRequest(store("readwrite").clear())

    public actual suspend fun keys(): List<String> {
        val request = store("readonly").getAllKeys()
        awaitRequest(request)
        return keysResult(request).toList().map { it.toString() }
    }

    public actual suspend fun count(): Long {
        val request = store("readonly").count()
        awaitRequest(request)
        return intResult(request).toLong()
    }

    public actual suspend fun size(key: String): Long? {
        val request = store("readonly").get(key.toJsString())
        awaitRequest(request)
        return int8Result(request)?.length?.toLong()
    }
}

public actual class IdbTextStore internal constructor(
    private val handle: IDBDatabaseHandle,
    private val store: String,
) {
    private fun store(mode: String): IDBObjectStore = handle.transaction(store, mode).objectStore(store)

    public actual suspend fun get(key: String): String? {
        val request = store("readonly").get(key.toJsString())
        awaitRequest(request)
        return stringResult(request)?.toString()
    }

    public actual suspend fun put(
        key: String,
        value: String,
    ): Unit = awaitRequest(store("readwrite").put(value.toJsString(), key.toJsString()))

    public actual suspend fun delete(key: String): Unit = awaitRequest(store("readwrite").delete(key.toJsString()))

    public actual suspend fun clear(): Unit = awaitRequest(store("readwrite").clear())

    public actual suspend fun keys(): List<String> {
        val request = store("readonly").getAllKeys()
        awaitRequest(request)
        return keysResult(request).toList().map { it.toString() }
    }

    public actual suspend fun count(): Long {
        val request = store("readonly").count()
        awaitRequest(request)
        return intResult(request).toLong()
    }

    public actual suspend fun entries(): List<Pair<String, String>> = keys().mapNotNull { key -> get(key)?.let { key to it } }
}

private suspend fun awaitRequest(request: IDBRequest): Unit =
    suspendCancellableCoroutine { continuation ->
        request.onsuccess = { continuation.resume(Unit) }
        request.onerror = { continuation.resumeWithException(IllegalStateException("IndexedDB request failed")) }
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
