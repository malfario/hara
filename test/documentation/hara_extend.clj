(ns documentation.hara-extend)

[[:chapter {:title "Introduction"}]]

"
[hara.extend](https://github.com/zcaudate/hara/blob/master/src/hara/extend.clj) provide additional functionality on top of `defrecord` and `defmulti`/`defmethod`."

[[:section {:title "Installation"}]]

"Add to `project.clj` dependencies:

    [im.chit/hara.extend \"{{PROJECT.version}}\"]

Individual namespaces can be added seperately:

    [im.chit/hara.extend.abstract \"{{PROJECT.version}}\"]
    [im.chit/hara.extend.all \"{{PROJECT.version}}\"]
"

[[:chapter {:title "API - abstract"}]]

"[hara.extend.abstract](https://github.com/zcaudate/hara/blob/master/src/hara/extend/abstract.clj)"

[[:api {:namespace "hara.extend.abstract"}]]

[[:chapter {:title "API - all"}]]

"[hara.extend.all](https://github.com/zcaudate/hara/blob/master/src/hara/extend/all.clj)"

[[:api {:namespace "hara.extend.all"}]]
