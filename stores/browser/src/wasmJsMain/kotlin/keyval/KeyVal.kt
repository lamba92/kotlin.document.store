@file:JsModule("idb-keyval")

package keyval

import kotlin.js.Promise

public external fun get(key: String): Promise<JsString?>

public external fun set(
    key: String,
    value: String,
): Promise<JsAny?>

public external fun del(key: String): Promise<JsAny?>

public external fun delMany(keys: JsArray<JsString>): Promise<JsAny?>

public external fun clear(): Promise<JsAny?>

public external fun keys(): Promise<JsArray<JsString>>
