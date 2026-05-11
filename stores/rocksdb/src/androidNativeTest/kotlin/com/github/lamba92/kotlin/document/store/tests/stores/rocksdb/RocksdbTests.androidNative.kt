package com.github.lamba92.kotlin.document.store.tests.stores.rocksdb

actual val DB_PATH: String
    get() = error()

private fun error(): Nothing = error("Kotlin/Native has no tests suite for Android Native")
