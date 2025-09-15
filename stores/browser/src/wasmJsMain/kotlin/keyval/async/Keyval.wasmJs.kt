package keyval.async

import keyval.clear
import keyval.del
import keyval.delMany
import keyval.get
import keyval.keys
import keyval.set
import kotlinx.coroutines.await

public actual suspend fun get(key: String): String? =
    get(key.toJsString())
        .await<JsString?>()
        .toString()

public actual suspend fun keys(): List<String> =
    keys()
        .await<JsArray<JsString>>()
        .toList()
        .map { it.toString() }

public actual suspend fun delMany(keys: List<String>) {
    val jsKeys =
        keys
            .map { it.toJsString() }
            .toJsArray()
    delMany(jsKeys).await<Unit>()
}

public actual suspend fun del(key: String): Unit = del(key.toJsString()).await()

public actual suspend fun set(
    key: String,
    value: String,
): Unit = set(key.toJsString(), value.toJsString()).await()

public actual suspend fun clear(): Unit = clear().await()
