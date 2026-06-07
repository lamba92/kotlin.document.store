package keyval.async

import kotlinx.coroutines.await

public actual suspend fun get(key: String): String? = keyval.get(key).await<JsString?>()?.toString()

public actual suspend fun keys(): List<String> =
    keyval
        .keys()
        .await<JsArray<JsString>>()
        .toList()
        .map { it.toString() }

public actual suspend fun delMany(keys: List<String>) {
    keyval.delMany(keys.map { it.toJsString() }.toJsArray()).await<JsAny?>()
}

public actual suspend fun del(key: String) {
    keyval.del(key).await<JsAny?>()
}

public actual suspend fun set(
    key: String,
    value: String,
) {
    keyval.set(key, value).await<JsAny?>()
}

public actual suspend fun clear() {
    keyval.clear().await<JsAny?>()
}
