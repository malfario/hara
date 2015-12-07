(ns documentation.hara-common)

[[:chapter {:title "Introduction"}]]

"
[hara.common](https://github.com/zcaudate/hara/blob/master/src/hara/common.clj) are a set of primitive declarations and functions that extend on top of `clojure.core` and are used by many of the other namespaces in the `hara` ecosystem."

[[:section {:title "Installation"}]]

"Add to `project.clj` dependencies:

    [im.chit/hara.common \"{{PROJECT.version}}\"]

Individual namespaces can be added seperately:

    [im.chit/hara.common.checks \"{{PROJECT.version}}\"]
    [im.chit/hara.common.error \"{{PROJECT.version}}\"]
    [im.chit/hara.common.hash \"{{PROJECT.version}}\"]
    [im.chit/hara.common.primitives \"{{PROJECT.version}}\"]
    [im.chit/hara.common.state \"{{PROJECT.version}}\"]
    [im.chit/hara.common.string \"{{PROJECT.version}}\"]
    [im.chit/hara.common.watch \"{{PROJECT.version}}\"]
"

[[:chapter {:title "API - checks"}]]

"[hara.common.checks](https://github.com/zcaudate/hara/blob/master/src/hara/common/checks.clj) contain type predicates that are missing from the `clojure.core`:"

[[:api {:namespace "hara.common.checks"}]]

[[:chapter {:title "API - errors"}]]

"[hara.common.error](https://github.com/zcaudate/hara/blob/master/src/hara/common/error.clj) contain simple macros for throwing and processing errors:"

[[:api {:namespace "hara.common.error"}]]

[[:chapter {:title "API - hash"}]]

"[hara.common.hash](https://github.com/zcaudate/hara/blob/master/src/hara/common/hash.clj) contain methods for working with object hashes:"

[[:api {:namespace "hara.common.hash"}]]

[[:chapter {:title "API - primitives"}]]

"[hara.common.primitives](https://github.com/zcaudate/hara/blob/master/src/hara/common/primitives.clj) contain contructs that are missing from `clojure.core`:"

[[:api {:namespace "hara.common.primitives"}]]

[[:chapter {:title "API - state"}]]

"[hara.common.state](https://github.com/zcaudate/hara/blob/master/src/hara/common/state.clj) contain extensible methods for manipulating stateful datastructures:"

[[:api {:namespace "hara.common.state"}]]

[[:chapter {:title "API - string"}]]

"[hara.common.string](https://github.com/zcaudate/hara/blob/master/src/hara/common/string.clj) contain methods for string manipulation:"

[[:api {:namespace "hara.common.string"}]]

[[:chapter {:title "API - watch"}]]

"[hara.common.watch](https://github.com/zcaudate/hara/blob/master/src/hara/common/watch.clj) contain extensible methods for observation of state:"

[[:api {:namespace "hara.common.watch"}]]
