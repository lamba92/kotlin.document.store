package keyval.async

public expect suspend fun get(key: String): String?

public expect suspend fun keys(): List<String>

public expect suspend fun delMany(keys: List<String>)

public expect suspend fun del(key: String)

public expect suspend fun set(
    key: String,
    value: String,
)

public expect suspend fun clear()
