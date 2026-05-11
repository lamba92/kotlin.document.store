@file:Suppress("unused")

package com.github.lamba92.kotlin.document.store.tests.stores.rocksdb

import com.github.lamba92.kotlin.document.store.core.DataStore
import com.github.lamba92.kotlin.document.store.stores.rocksdb.RocksDBStore
import com.github.lamba92.kotlin.document.store.tests.AbstractDeleteTests
import com.github.lamba92.kotlin.document.store.tests.AbstractDocumentDatabaseTests
import com.github.lamba92.kotlin.document.store.tests.AbstractFindTests
import com.github.lamba92.kotlin.document.store.tests.AbstractIndexTests
import com.github.lamba92.kotlin.document.store.tests.AbstractInsertTests
import com.github.lamba92.kotlin.document.store.tests.AbstractObjectCollectionTests
import com.github.lamba92.kotlin.document.store.tests.AbstractUpdateTests
import com.github.lamba92.kotlin.document.store.tests.DataStoreProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import maryk.rocksdb.openRocksDB

class RocksDBDeleteTests : AbstractDeleteTests(RocksDBStoreProvider)

class RocksDBDocumentDatabaseTests : AbstractDocumentDatabaseTests(RocksDBStoreProvider)

class RocksDBIndexTests : AbstractIndexTests(RocksDBStoreProvider)

class RocksDBInsertTests : AbstractInsertTests(RocksDBStoreProvider)

class RocksDBUpdateTests : AbstractUpdateTests(RocksDBStoreProvider)

class RocksDBFindTests : AbstractFindTests(RocksDBStoreProvider)

class RocksDBObjectCollectionTests : AbstractObjectCollectionTests(RocksDBStoreProvider)

object RocksDBStoreProvider : DataStoreProvider {
    private fun getDbPath(testName: String) = Path(DB_PATH).resolve(testName)

    override suspend fun deleteDatabase(testName: String) =
        withContext(Dispatchers.IO) {
            getDbPath(testName).deleteRecursively()
        }

    override fun provide(testName: String): DataStore =
        RocksDBStore(
            openRocksDB(
                getDbPath(testName)
                    .createDirectories()
                    .toString(),
            ),
        )
}

fun Path.resolve(vararg other: String): Path = Path(this, *other)

fun Path.createDirectories(): Path {
    SystemFileSystem.createDirectories(this, false)
    return this
}

expect val DB_PATH: String

val Path.isFile: Boolean
    get() = SystemFileSystem.metadataOrNull(this)?.isRegularFile == true

val Path.isDirectory
    get() = SystemFileSystem.metadataOrNull(this)?.isDirectory == true

fun Path.delete() = SystemFileSystem.delete(this, false)

fun Path.deleteRecursively() {
    if (isDirectory) {
        SystemFileSystem.list(this@deleteRecursively)
            .forEach {
                when {
                    it.isFile -> it.delete()
                    it.isDirectory -> it.deleteRecursively()
                }
            }
    }
    delete()
}
