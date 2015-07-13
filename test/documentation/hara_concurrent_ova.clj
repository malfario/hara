(ns documentation.hara-concurrent-ova)

[[:chapter {:title "Introduction"}]]

"An `ova` represents a mutable array of elements. It has been designed especially for dealing with shared mutable state in multi-threaded applications. Clojure uses `refs` and `atoms` off the shelf to resolve this issue but left out methods to deal with arrays of shared elements. `ova` has been specifically designed for the following use case:

- Elements (usually clojure maps) can be added or removed from an array
- Element data are accessible and mutated from several threads.
- Array itself can also be mutated from several threads."

[[:section {:title "Installation"}]]

"Add to `project.clj` dependencies:

    [im.chit/hara.concurrent.ova \"{{PROJECT.version}}\"]"

"All functions are in the `hara.concurrent.ova` namespace."

(comment (use 'hara.concurrent.ova))

[[:section {:title "Motivation"}]]

"
Coordination in multi-threaded applications have always been a pain. Most times situations are usally co-ordinated using a external cache store like redis. `hara.concurrent.ova` provides an easy to use interface for array data. Alought is no where near as fully featured as a database/cache, it has a small footprint and is small. The actual `ova` datastructure is a `ref` containing a `vector` containing ref. The library comes with a whole bundle of goodies to deal with mutation:

- Clean element selection and array manipulation syntax.
- Watches for both the array and array elements.
- Designed to play nicely with `dosync` and `refs`.
- Written in pure clojure, no external dependencies.
"

[[:chapter {:title "Walkthrough"}]]

[[:file {:src "test/documentation/hara_concurrent_ova/walkthrough.clj"}]]

[[:chapter {:title "API"}]]

[[:file {:src "test/documentation/hara_concurrent_ova/api.clj"}]]

[[:chapter {:title "Selection"}]]

[[:file {:src "test/documentation/hara_concurrent_ova/selection.clj"}]]

[[:chapter {:title "Scoreboard"}]]

[[:file {:src "test/documentation/hara_concurrent_ova/scoreboard.clj"}]]
