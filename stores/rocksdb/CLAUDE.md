# `:stores:rocksdb` — RocksDB-backed `DataStore`

Replaces the older `:stores:leveldb` module. Same `AbstractDataStore` /
`PersistentMap<String,String>` contract — only the engine changes.

## Engine

`io.maryk.rocksdb:rocksdb-multiplatform` — `expect`/`actual` bindings over
`org.rocksdb:rocksdbjni` on JVM/Android and the prebuilt RocksDB native
libraries on K/Native.

Pinned in `kotlin.document.store/gradle/libs.versions.toml` under
`rocksdb-multiplatform`.

## First-build network dependency

The native binaries used by the K/Native targets are **downloaded at build
time** from `github.com/marykdb/build-rocksdb/releases` — they are not
bundled inside the `rocksdb-multiplatform` artifact. SHAs are pinned upstream.

Override via Gradle properties if you ever need to mirror them:

- `rocksdbPrebuiltBaseUrl` — base URL for the prebuilt zips.
- `rocksdbPrebuiltVersion` — pinned binary version.

A first build on a fresh CI runner or air-gapped machine will hit GitHub.

## Targets dropped vs the old LevelDB module

`rocksdb-multiplatform` does not publish artifacts for `iosX64`, `watchosX64`,
or `tvosX64` (Intel-Mac-only simulator targets). Those targets are dropped from
this module's `build.gradle.kts`. iOS Simulator on Apple Silicon is covered by
`iosSimulatorArm64`, which every current dev machine and CI runner uses.

## API differences vs `kotlin-leveldb`

`kotlin-leveldb` exposed string-keyed `get`/`put`/`delete` and a
`batch { }` DSL. `rocksdb-multiplatform` is `ByteArray`-based and has no
batch DSL. The thin glue lives in `RocksDBExtensions.kt`:

- `RocksDB.getString` / `putString` — UTF-8 wrappers.
- `RocksDB.scanPrefix(prefix)` — returns a `PrefixScan` (`Sequence` +
  `AutoCloseable`) that owns the underlying `RocksIterator`. Always
  `.use { }` it; the iterator will leak otherwise.
- `RocksDB.batch { put(...); delete(...) }` — opens a `WriteBatch` +
  `WriteOptions`, runs the block, calls `db.write()`, closes both. Mirrors
  the old `LevelDB.batch { }` shape so callers don't change.

## Resource lifecycle

`RocksObject` subclasses (`Options`, `WriteBatch`, `WriteOptions`,
`RocksIterator`) hold native handles and **must be closed**. The store API
keeps this hidden: `RocksDBStore.open(path)` uses `openRocksDB(path)` which
allocates default `Options` internally; the store's `close()` releases the
DB handle. Helpers (`scanPrefix`, `batch`) wrap their own resources with
`use { }`. Callers see only `String`-typed APIs and a single `close()`.
