(ns documentation.hara-component)

[[:chapter {:title "Introduction"}]]

"
`hara.component` is really a method of dependency injection has been inspired by the original Stuart Sierra component [library](https://github.com/stuartsierra/component) and [talk](http://www.youtube.com/watch?v=13cmHf_kt-Q). The virtues of this library has been much lauded and is quite a common practise within enterprise applications. Doing a [search](https://www.google.com?q=stuart+sierra+component) will yield many uses of such a pattern."

[[:section {:title "Installation"}]]

"Add to `project.clj` dependencies:

    [im.chit/hara.component \"{{PROJECT.version}}\"]"

"All functions are in the `hara.component` namespace."

(comment (require '[hara.component :as component]))

[[:section {:title "Motivation"}]]

"
The main reason for a reinterpretation of the original [stuartsierra/component](https://github.com/stuartsierra/component) was for a couple reasons:

- the `component/Lifecycle` protocol did not expose `started?` and `stopped?` methods
- the new library has been designed to work well with configuration files
- dependencies are now not required to be explicitly defined
- added support for dealing with arrays of component
- more control was needed when working with nested systems
- more emphasis has been placed on prettiness and readibility
"
