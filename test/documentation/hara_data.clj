(ns documentation.hara-data)

[[:chapter {:title "Introduction"}]]

"
[hara.data](https://github.com/zcaudate/hara/blob/master/src/hara/data.clj) are a set of functions that act on clojure hash-maps and map-like representations of data. The level of complexity needed for working with data increases as it becomes nested and then relational. The functions of `hara.data` get increasingly sophisticated, with the `hara.data.complex` namespace built specifically for working with datomic datastructures."

[[:section {:title "Installation"}]]

"
Add to `project.clj` dependencies:

    [im.chit/hara.data \"{{PROJECT.version}}\"]

Individual namespaces can be added seperately:

    [im.chit/hara.data.map \"{{PROJECT.version}}\"]
    [im.chit/hara.data.nested \"{{PROJECT.version}}\"]
    [im.chit/hara.data.diff \"{{PROJECT.version}}\"]
    [im.chit/hara.data.combine \"{{PROJECT.version}}\"]
    [im.chit/hara.data.complex \"{{PROJECT.version}}\"]
    [im.chit/hara.data.record \"{{PROJECT.version}}\"]
    [im.chit/hara.data.path \"{{PROJECT.version}}\"]
    [im.chit/hara.data.seq \"{{PROJECT.version}}\"]
"

[[:chapter {:title "API - map"}]]

"[hara.data.map](https://github.com/zcaudate/hara/blob/master/src/hara/data/map.clj) contain functions for updating hashmaps."

[[:api {:namespace "hara.data.map"}]]

[[:chapter {:title "API - nested"}]]

"[hara.data.nested](https://github.com/zcaudate/hara/blob/master/src/hara/data/nested.clj) contain functions for updating nested hashmaps."

[[:api {:namespace "hara.data.nested"}]]

[[:chapter {:title "API - diff"}]]

"[hara.data.diff](https://github.com/zcaudate/hara/blob/master/src/hara/data/diff.clj) contain functions for comparing maps as well as functions to patch changes."

[[:api {:namespace "hara.data.diff"}]]

[[:chapter {:title "API - combine"}]]

"[hara.data.combine](https://github.com/zcaudate/hara/blob/master/src/hara/data/combine.clj) contain functions for working with sets of data."

[[:api {:namespace "hara.data.combine"}]]

[[:chapter {:title "API - complex"}]]

"[hara.data.complex](https://github.com/zcaudate/hara/blob/master/src/hara/data/complex.clj) combine functions for working with relational data such as that coming out from datomic."

[[:api {:namespace "hara.data.complex"}]]

[[:chapter {:title "API - record"}]]

"[hara.data.record](https://github.com/zcaudate/hara/blob/master/src/hara/data/record.clj) contain functions for working with clojure records"

[[:api {:namespace "hara.data.record"}]]

[[:chapter {:title "API - path"}]]

"[hara.data.path](https://github.com/zcaudate/hara/blob/master/src/hara/data/path.clj) concerns itself with the translation between data contained in a nested versus data contained in a single map with paths as keys."

[[:api {:namespace "hara.data.path"}]]

[[:chapter {:title "API - seq"}]]

"[hara.data.seq](https://github.com/zcaudate/hara/blob/master/src/hara/data/seq.clj) is a support namespace used internally"

[[:api {:namespace "hara.data.seq"}]]
