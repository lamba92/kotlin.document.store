@file:Suppress("unused")

package io.github.lamba92.kotlin.document.store.tests.stores.browser

import io.github.lamba92.kotlin.document.store.core.DataStore
import io.github.lamba92.kotlin.document.store.stores.browser.BrowserStore
import io.github.lamba92.kotlin.document.store.tests.AbstractDeleteTests
import io.github.lamba92.kotlin.document.store.tests.AbstractDocumentDatabaseTests
import io.github.lamba92.kotlin.document.store.tests.AbstractFindTests
import io.github.lamba92.kotlin.document.store.tests.AbstractIndexTests
import io.github.lamba92.kotlin.document.store.tests.AbstractInsertTests
import io.github.lamba92.kotlin.document.store.tests.AbstractObjectCollectionTests
import io.github.lamba92.kotlin.document.store.tests.AbstractUpdateTests
import io.github.lamba92.kotlin.document.store.tests.DataStoreProvider

class BrowserDeleteTests : AbstractDeleteTests(BrowserStoreProvider)

class BrowserDocumentDatabaseTests : AbstractDocumentDatabaseTests(BrowserStoreProvider)

class BrowserIndexTests : AbstractIndexTests(BrowserStoreProvider)

class BrowserInsertTests : AbstractInsertTests(BrowserStoreProvider)

class BrowserUpdateTests : AbstractUpdateTests(BrowserStoreProvider)

class BrowserFindTests : AbstractFindTests(BrowserStoreProvider)

class BrowserObjectCollectionTests : AbstractObjectCollectionTests(BrowserStoreProvider)

object BrowserStoreProvider : DataStoreProvider {
    override suspend fun deleteDatabase(testName: String) {
        BrowserStore.DEFAULT.clearForTests()
    }

    override fun provide(testName: String): DataStore = BrowserStore.DEFAULT
}
