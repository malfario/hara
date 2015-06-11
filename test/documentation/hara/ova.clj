(ns documentation.hara.ova
  (:require [hara.ova :refer :all]
            [hara.common.watch :as watch]
            [midje.sweet :refer :all]))

[[:chapter {:title "Installation"}]]

["Add to `project.clj` dependencies (use double quotes): 

    [im.chit/hara.ova '{{PROJECT.version}}']"

"All functions are in the `hara.ova` namespace."]

[[{:numbered false}]]
(comment (use 'hara.ova))

[[:chapter {:title "Motivation"}]]

"An `ova` represents a mutable array of elements. The question should really be asked: Why?

**Biased Answer:** Because it is the most fully featured and *bestest* mutable array.... *EVER!*

In all seriousness, `ova` has been designed especially for dealing with shared mutable state in multi-threaded applications. Clojure uses `refs` and `atoms` off the shelf to resolve this issue but left out methods to deal with arrays of shared elements. `ova` has been specifically designed for the following use case:

 - Elements (usually clojure maps) can be added or removed from an array
 - Element data are accessible and mutated from several threads.
 - Array itself can also be mutated from several threads.

These type of situations are usally co-ordinated using a external cache store like redis. Ova is no where near as fully featured as these libraries. The actual `ova` datastructure is a `ref` containing a `vector` containing ref. The library comes with a whole bundle of goodies to deal with mutation:

 - Clean element selection and array manipulation syntax
 - Watches for both the array and array elements
 - Designed to play nicely with `dosync` and `refs`
 - Pure clojure

The library has been abstracted out of [cronj](https://github.com/zcaudate/cronj), a task scheduling library where it is used to track and manipulate shared state. The `ova` syntax abstracts away alot of clutter. An example of tracking state in a multi-threaded environment can be seen in a [scoreboard example](#scoreboard-example):
"

[[:chapter {:title "Walkthrough"}]]

[[:file {:src "test/documentation/hara/ova/walkthrough.clj"}]]

[[:chapter {:title "API Reference"}]]

[[:file {:src "test/documentation/hara/ova/api.clj"}]]

[[:chapter {:title "Indices Selection"}]]

;[[:file {:src "test/documentation/hara/ova/selection.clj"}]]

[[:chapter {:title "End Notes"}]]

"For any feedback, requests and comments, please feel free to lodge an issue on github or contact me directly.
"

