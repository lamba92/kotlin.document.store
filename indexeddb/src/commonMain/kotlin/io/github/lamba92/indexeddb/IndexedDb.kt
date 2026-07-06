package io.github.lamba92.indexeddb

/**
 * Thin Kotlin facade over the browser's IndexedDB API, commonized across the `js` and
 * `wasmJs` targets. The common surface speaks only Kotlin types — the per-target actuals
 * hold the hand-written `external` bindings, so no `JsAny`/`dynamic` leaks above this seam.
 *
 * Values are typed by store view ([IdbByteStore] for binary, [IdbTextStore] for strings)
 * rather than a single discriminated value, so the read path never has to inspect a JS
 * value's runtime type — each view's request binding already knows what it returns.
 *
 * Every store operation runs in its own short-lived transaction; the facade deliberately
 * does NOT expose multi-operation transactions. Read-modify-write atomicity is the caller's
 * responsibility (a `Mutex`), which sidesteps the way IndexedDB's auto-commit interacts
 * badly with coroutine dispatch.
 */
public expect suspend fun openIdb(
    name: String,
    version: Int = 1,
    stores: Set<String>,
): IdbDatabase

/** An open IndexedDB database handle. Obtain typed views over its object stores; [close] when done. */
public expect class IdbDatabase : AutoCloseable {
    /** A binary-valued view over the object store [store] (which must have been declared in [openIdb]). */
    public fun bytes(store: String): IdbByteStore

    /** A string-valued view over the object store [store] (which must have been declared in [openIdb]). */
    public fun text(store: String): IdbTextStore

    override fun close()
}

/** A single object store, viewed as holding `ByteArray` values keyed by `String`. */
public expect class IdbByteStore {
    public suspend fun get(key: String): ByteArray?

    public suspend fun put(
        key: String,
        value: ByteArray,
    )

    public suspend fun delete(key: String)

    public suspend fun clear()

    public suspend fun keys(): List<String>

    public suspend fun count(): Long

    /** Byte length of the value at [key], or `null` if absent — without copying the bytes into Kotlin. */
    public suspend fun size(key: String): Long?
}

/** A single object store, viewed as holding `String` values keyed by `String`. */
public expect class IdbTextStore {
    public suspend fun get(key: String): String?

    public suspend fun put(
        key: String,
        value: String,
    )

    public suspend fun delete(key: String)

    public suspend fun clear()

    public suspend fun keys(): List<String>

    public suspend fun count(): Long

    public suspend fun entries(): List<Pair<String, String>>
}
