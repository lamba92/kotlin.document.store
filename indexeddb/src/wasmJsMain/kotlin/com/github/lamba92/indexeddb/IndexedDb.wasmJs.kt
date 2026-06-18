@file:OptIn(ExperimentalWasmJsInterop::class)
@file:Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE", "UNCHECKED_CAST")

package com.github.lamba92.indexeddb

import kotlinx.coroutines.suspendCancellableCoroutine
import org.khronos.webgl.Int8Array
import org.khronos.webgl.get
import org.khronos.webgl.set
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private external val indexedDB: IDBFactory

internal external interface IDBRequest : JsAny {
    var onsuccess: ((JsAny) -> Unit)?
    var onerror: ((JsAny) -> Unit)?
}

internal external interface IDBOpenDBRequest : IDBRequest {
    val result: IDBDatabaseHandle
    var onupgradeneeded: ((JsAny) -> Unit)?
}

// `IDBRequest.result` is `any` in the DOM. [IDBValueRequest] types it generically for the object
// results (get / getAllKeys); `count`'s result is a primitive `Int`, not a `JsAny`, so it can't ride
// the `T : JsAny` generic and keeps its own [IDBCountRequest]. The call site reads `result` through
// these external surfaces (an unchecked downcast) rather than a hand-written `js()`.
internal external interface IDBValueRequest<out T : JsAny> : IDBRequest {
    val result: T?
}

internal external interface IDBCountRequest : IDBRequest {
    val result: Int
}

internal external interface IDBFactory : JsAny {
    fun open(
        name: String,
        version: Int,
    ): IDBOpenDBRequest
}

internal external interface IDBDatabaseHandle : JsAny {
    val objectStoreNames: DOMStringList

    fun createObjectStore(name: String): JsAny

    fun transaction(
        storeNames: String,
        mode: String,
    ): IDBTransaction

    fun close()
}

internal external interface DOMStringList : JsAny {
    fun contains(string: String): Boolean
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

    fun getAllKeys(): IDBValueRequest<JsArray<JsString>>

    fun count(): IDBCountRequest
}

public actual suspend fun openIdb(
    name: String,
    version: Int,
    stores: Set<String>,
): IdbDatabase {
    val request = indexedDB.open(name, version)
    val handle =
        suspendCancellableCoroutine { continuation ->
            request.onupgradeneeded = {
                val db = request.result
                stores.forEach { if (!db.objectStoreNames.contains(it)) db.createObjectStore(it) }
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
        return (request as IDBValueRequest<Int8Array>).result?.toByteArray()
    }

    public actual suspend fun put(
        key: String,
        value: ByteArray,
    ): Unit = put(key, value.toInt8Array())

    /**
     * wasmJs-only fast path: store an already-built [Int8Array] directly, skipping the
     * `ByteArray -> Int8Array` re-copy the [ByteArray] overload pays. Lets a wasm caller that
     * produces bytes straight into a typed array (e.g. a blob store draining a `kotlinx.io`
     * buffer) avoid the intermediate `ByteArray`. Not on the common `expect` — `Int8Array` is
     * a wasm/js type that can't appear in the commonized facade surface.
     */
    public suspend fun put(
        key: String,
        value: Int8Array,
    ): Unit = awaitRequest(store("readwrite").put(value, key.toJsString()))

    public actual suspend fun delete(key: String): Unit = awaitRequest(store("readwrite").delete(key.toJsString()))

    public actual suspend fun clear(): Unit = awaitRequest(store("readwrite").clear())

    public actual suspend fun keys(): List<String> {
        val request = store("readonly").getAllKeys()
        awaitRequest(request)
        val keys = request.result ?: return emptyList()
        return keys.toList().map { it.toString() }
    }

    public actual suspend fun count(): Long {
        val request = store("readonly").count()
        awaitRequest(request)
        return request.result.toLong()
    }

    public actual suspend fun size(key: String): Long? {
        val request = store("readonly").get(key.toJsString())
        awaitRequest(request)
        return (request as IDBValueRequest<Int8Array>).result?.length?.toLong()
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
        return (request as IDBValueRequest<JsString>).result?.toString()
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
        val keys = request.result ?: return emptyList()
        return keys.toList().map { it.toString() }
    }

    public actual suspend fun count(): Long {
        val request = store("readonly").count()
        awaitRequest(request)
        return request.result.toLong()
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
