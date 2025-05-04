package types

public external interface IDBRequest {
    public var oncomplete: (() -> Unit)?
    public var onsuccess: (() -> Unit)?
    public var onabort: (() -> Unit)?
    public var onerror: (() -> Unit)?
    public val result: JsAny?
    public val error: DOMException?
}

public external interface IDBTransaction {
    public var oncomplete: (() -> Unit)?
    public var onsuccess: (() -> Unit)?
    public var onabort: (() -> Unit)?
    public var onerror: (() -> Unit)?
    public val result: JsAny?
    public val error: DOMException?
}

public external interface IDBObjectStore {
    public fun put(
        value: JsAny?,
        key: String,
    ): IDBRequest

    public fun get(key: String): IDBRequest

    public fun delete(key: String): IDBRequest

    public fun clear(): IDBRequest

    public fun openCursor(): IDBRequest

    public fun getAll(): IDBRequest

    public fun getAllKeys(): IDBRequest

    public val transaction: IDBTransaction
}

public external interface IDBCursorWithValue {
    public val key: String
    public val value: String

    @JsName("continue")
    public fun next()
}

public external class DOMException(
    message: String = definedExternally,
    name: String = definedExternally,
) {
    public val name: String
    public val message: String
    public val code: Short

    public companion object {
        public val INDEX_SIZE_ERR: Short
        public val DOMSTRING_SIZE_ERR: Short
        public val HIERARCHY_REQUEST_ERR: Short
        public val WRONG_DOCUMENT_ERR: Short
        public val INVALID_CHARACTER_ERR: Short
        public val NO_DATA_ALLOWED_ERR: Short
        public val NO_MODIFICATION_ALLOWED_ERR: Short
        public val NOT_FOUND_ERR: Short
        public val NOT_SUPPORTED_ERR: Short
        public val INUSE_ATTRIBUTE_ERR: Short
        public val INVALID_STATE_ERR: Short
        public val SYNTAX_ERR: Short
        public val INVALID_MODIFICATION_ERR: Short
        public val NAMESPACE_ERR: Short
        public val INVALID_ACCESS_ERR: Short
        public val VALIDATION_ERR: Short
        public val TYPE_MISMATCH_ERR: Short
        public val SECURITY_ERR: Short
        public val NETWORK_ERR: Short
        public val ABORT_ERR: Short
        public val URL_MISMATCH_ERR: Short
        public val QUOTA_EXCEEDED_ERR: Short
        public val TIMEOUT_ERR: Short
        public val INVALID_NODE_TYPE_ERR: Short
        public val DATA_CLONE_ERR: Short
    }
}
