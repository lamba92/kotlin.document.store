package com.github.lamba92.kotlin.document.store.tests.stores.rocksdb

actual val DB_PATH: String
    get() = System.getenv("DB_PATH") ?: error("DB_PATH not set")
