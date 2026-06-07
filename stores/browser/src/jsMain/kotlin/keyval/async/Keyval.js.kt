package keyval.async

import kotlinx.coroutines.await

public actual suspend fun get(key: String): String? = keyval.get(key).await()

public actual suspend fun keys(): List<String> = keyval.keys().await().toList()

public actual suspend fun delMany(keys: List<String>) {
    keyval.delMany(keys.toTypedArray()).await()
}

public actual suspend fun del(key: String) {
    keyval.del(key).await()
}

public actual suspend fun set(
    key: String,
    value: String,
) {
    keyval.set(key, value).await()
}

public actual suspend fun clear() {
    keyval.clear().await()
}
