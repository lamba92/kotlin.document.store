package keyval.async

import keyval.clear
import keyval.del
import keyval.delMany
import keyval.get
import keyval.keys
import keyval.set
import kotlinx.coroutines.await

public actual suspend fun get(key: String): String? = get(key).await()

public actual suspend fun keys(): List<String> = keys().await().toList()

public actual suspend fun delMany(keys: List<String>): Unit = delMany(keys.toTypedArray()).await()

public actual suspend fun del(key: String): Unit = del(key).await()

public actual suspend fun set(
    key: String,
    value: String,
): Unit = set(key, value).await()

public actual suspend fun clear(): Unit = clear().await()
