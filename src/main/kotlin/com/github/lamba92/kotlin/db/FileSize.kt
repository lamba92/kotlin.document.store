package com.github.lamba92.kotlin.db

@JvmInline
value class FileSize internal constructor(val size: Long)

val Number.bytes get() = FileSize(toLong())
val Number.kilobytes get() = FileSize(toLong() * 1024)
val Number.megabytes get() = FileSize(toLong() * 1024 * 1024)
val Number.gigabytes get() = FileSize(toLong() * 1024 * 1024 * 1024)

val FileSize.bytes
    get() = size
val FileSize.kilobytes
    get() = size.toDouble() / 1024
val FileSize.megabytes
    get() = size.toDouble() / 1024 / 1024
val FileSize.gigabytes
    get() = size.toDouble() / 1024 / 1024 / 1024