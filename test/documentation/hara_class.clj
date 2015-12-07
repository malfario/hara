(ns documentation.hara-class)

[[:chapter {:title "Introduction"}]]

"
[hara.class](https://github.com/zcaudate/hara/blob/master/src/hara/class.clj) are a set of functions that give information about class properties. There used by [hara.reflect](hara-reflect.html) and [hara.object](hara-object.html) for their reflection calls."

[[:section {:title "Installation"}]]

"
Add to `project.clj` dependencies:

    [im.chit/hara.class \"{{PROJECT.version}}\"]

Individual namespaces can be added seperately:

    [im.chit/hara.class.checks \"{{PROJECT.version}}\"]
    [im.chit/hara.class.inheritance \"{{PROJECT.version}}\"]
"

[[:chapter {:title "API - checks"}]]

"[hara.class.checks](https://github.com/zcaudate/hara/blob/master/src/hara/class/checks.clj) contain class checking predicates:"

[[:api {:namespace "hara.class.checks"}]]

[[:chapter {:title "API - inheritance"}]]

"[hara.class.inheritance](https://github.com/zcaudate/hara/blob/master/src/hara/class/inheritance.clj) contain inheritance checking predicates:"

[[:api {:namespace "hara.class.inheritance"}]]
