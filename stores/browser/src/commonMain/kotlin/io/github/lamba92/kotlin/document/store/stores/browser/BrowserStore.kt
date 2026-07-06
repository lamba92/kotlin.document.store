package io.github.lamba92.kotlin.document.store.stores.browser

import io.github.lamba92.indexeddb.IdbDatabase
import io.github.lamba92.indexeddb.IdbTextStore
import io.github.lamba92.indexeddb.openIdb
import io.github.lamba92.kotlin.document.store.core.AbstractDataStore
import io.github.lamba92.kotlin.document.store.core.DataStore
import io.github.lamba92.kotlin.document.store.core.PersistentMap

/**
 * Implementation of the [DataStore] for use in web browsers.
 *
 * `BrowserStore` uses `IndexedDB` as the underlying storage mechanism, providing
 * persistent key-value storage in the user's browser. It is designed for use in
 * web applications that require durable storage across browser sessions.
 *
 * Each instance is bound to one IndexedDB database ([databaseName]); all of that database's
 * named maps share a single object store ([STORE]) and are namespaced per map by an
 * [IndexedDBMap] key prefix. Use [DEFAULT] for the conventional single-database singleton, or
 * construct one with an explicit name to keep separate document stores isolated in the browser.
 */
public class BrowserStore(
    private val databaseName: String = DEFAULT_DATABASE_NAME,
) : AbstractDataStore() {
    private var database: IdbDatabase? = null

    private suspend fun documents(): IdbTextStore {
        val db = database ?: openIdb(databaseName, stores = setOf(STORE)).also { database = it }
        return db.text(STORE)
    }

    override suspend fun getMap(name: String): PersistentMap<String, String> =
        withStoreLock { IndexedDBMap(name, documents(), getMutex(name)) }

    override suspend fun deleteMap(name: String): Unit =
        withStoreLock {
            lockAndRemoveMutex(name) {
                val store = documents()
                store
                    .keys()
                    .filter { it.startsWith(IndexedDBMap.buildPrefix(name)) }
                    .forEach { store.delete(it) }
            }
        }

    override suspend fun close(): Unit =
        withStoreLock {
            database?.close()
            database = null
        }

    internal suspend fun clearForTests(): Unit = withStoreLock { documents().clear() }

    public companion object {
        private const val DEFAULT_DATABASE_NAME = "kotlin-document-store"
        private const val STORE = "documents"

        /** The conventional single-database `BrowserStore`, equivalent to the former singleton. */
        public val DEFAULT: BrowserStore = BrowserStore()
    }
}
