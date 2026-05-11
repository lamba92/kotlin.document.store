package com.github.lamba92.kotlin.document.store.tests.stores.rocksdb

import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL

actual val DB_PATH: String
    get() =
        createTestFilePath()
            ?: error("Failed to create test file for database")

fun createTestFilePath(): String? {
    val tmpDir = NSURL.fileURLWithPath(NSTemporaryDirectory())

    val testDir =
        tmpDir.URLByAppendingPathComponent("testDir")
            ?: error("Failed to create test directory")
    NSFileManager.defaultManager.createDirectoryAtURL(
        url = testDir,
        withIntermediateDirectories = true,
        attributes = null,
        error = null,
    )

    return testDir.path
}
