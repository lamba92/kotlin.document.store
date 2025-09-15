@file:JsModule("idb-keyval")
@file:Suppress("unused")

package keyval

import types.IDBObjectStore
import kotlin.js.Promise

public external interface UseStore {
    public operator fun invoke(
        txMode: String,
        callback: (store: IDBObjectStore) -> JsAny?,
    ): Promise<JsAny?>
}

public external fun promisifyRequest(request: JsAny?): Promise<JsAny?>

public external fun createStore(
    dbName: String,
    storeName: String,
): UseStore

public external fun get(
    key: JsString?,
    customStore: UseStore = definedExternally,
): Promise<JsString?>

public external fun set(
    key: JsString,
    value: JsAny?,
    customStore: UseStore = definedExternally,
): Promise<JsAny?>

public external fun setMany(
    entries: JsArray<JsArray<JsAny?>>,
    customStore: UseStore = definedExternally,
): Promise<JsAny?>

public external fun getMany(
    keys: JsArray<JsString>,
    customStore: UseStore = definedExternally,
): Promise<JsArray<JsAny?>>

public external fun update(
    key: String,
    updater: (oldValue: JsAny?) -> JsAny?,
    customStore: UseStore = definedExternally,
): Promise<JsAny?>

public external fun del(
    key: JsString,
    customStore: UseStore = definedExternally,
): Promise<JsAny?>

public external fun delMany(
    keys: JsArray<JsString>,
    customStore: UseStore = definedExternally,
): Promise<JsAny?>

public external fun clear(customStore: UseStore = definedExternally): Promise<JsAny?>

public external fun keys(customStore: UseStore = definedExternally): Promise<JsArray<JsString>>

public external fun values(customStore: UseStore = definedExternally): Promise<JsArray<JsString>>

public external fun entries(customStore: UseStore = definedExternally): Promise<JsArray<JsArray<JsString>>>
