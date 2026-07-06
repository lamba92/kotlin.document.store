package io.github.lamba92.kotlin.document.store.tests.stores.rocksdb

import androidx.test.platform.app.InstrumentationRegistry

actual val DB_PATH: String
    get() =
        InstrumentationRegistry
            .getInstrumentation()
            .targetContext
            .filesDir
            .resolve("testdb")
            .absolutePath
