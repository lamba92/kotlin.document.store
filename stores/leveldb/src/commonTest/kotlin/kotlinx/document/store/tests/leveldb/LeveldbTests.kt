@file:Suppress("unused")

package kotlinx.document.store.tests.leveldb

import com.github.lamba92.leveldb.LevelDB
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.document.store.DataStore
import kotlinx.document.store.leveldb.LevelDBStore
import kotlinx.document.store.tests.AbstractDeleteTests
import kotlinx.document.store.tests.AbstractDocumentDatabaseTests
import kotlinx.document.store.tests.AbstractFindTests
import kotlinx.document.store.tests.AbstractIndexTests
import kotlinx.document.store.tests.AbstractInsertTests
import kotlinx.document.store.tests.AbstractObjectCollectionTests
import kotlinx.document.store.tests.AbstractUpdateTests
import kotlinx.document.store.tests.DataStoreProvider
import kotlinx.io.files.Path

class LevelDBDeleteTests : AbstractDeleteTests(LevelDBStoreProvider)

class LevelDBDocumentDatabaseTests : AbstractDocumentDatabaseTests(LevelDBStoreProvider)

class LevelDBIndexTests : AbstractIndexTests(LevelDBStoreProvider)

class LevelDBInsertTests : AbstractInsertTests(LevelDBStoreProvider)

class LevelDBUpdateTests : AbstractUpdateTests(LevelDBStoreProvider)

class LevelDBFindTests : AbstractFindTests(LevelDBStoreProvider)

class LevelDBObjectCollectionTests : AbstractObjectCollectionTests(LevelDBStoreProvider)

object LevelDBStoreProvider : DataStoreProvider {
    private fun getDbPath(testName: String) = Path(DB_PATH).resolve(testName)

    override suspend fun deleteDatabase(testName: String) =
        withContext(Dispatchers.IO) {
            deleteFolderRecursively(getDbPath(testName).toString())
        }

    override fun provide(testName: String): DataStore =
        LevelDBStore(
            LevelDB(
                getDbPath(testName)
                    .createDirectories()
                    .toString(),
            ),
        )
}

expect fun Path.resolve(path: String): Path

expect fun Path.createDirectories(): Path

expect fun deleteFolderRecursively(path: String)

expect val DB_PATH: String
