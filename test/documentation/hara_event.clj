(ns documentation.hara-event
  (:require [hara.event :refer :all]
            [midje.sweet :refer :all]))

[[:chapter {:title "Introduction"}]]

"
`hara.event` aims to provide more loosely coupled code through two mechanisms:

- a global eventing system for decoupilng of side-effecting
- a conditional restart framework that hooks into the eventing system.

`hara.event` was originally developed as a seperate library - [ribol](https://github.com/zcaudate/ribol) but has been included as part [hara](https://github.com/zcaudate/hara). The main change from the original library has been the inclusion of the eventing system as it was felt that there should be an integrated way of dealing with side-effecting calls such as logging, indexing, emails and many other tasks within both normal and abnormal program flows.
"

[[:section {:title "Installation"}]]

"Add to `project.clj` dependencies (use double quotes):

    [im.chit/hara.event '{{PROJECT.version}}']"

"All functionality is found contained in `hara.event`"

[[{:numbered false}]]
(comment  (use 'hara.event))

;;[[:file {:src "test/clj/midje_doc/ribol_unlucky_numbers.clj"}]]

[[:section {:title "Similar Libraries"}]]

"
There are currently three other conditional restart libraries for clojure, in the more traditional Common Lisp style syntax:

- [errorkit](https://github.com/richhickey/clojure-contrib/blob/master/src/main/clojure/clojure/contrib/error_kit.clj) was the first and provided the guiding architecture for this library.

- [swell](https://github.com/hugoduncan/swell) and [conditions](https://github.com/bwo/conditions) have been written to work with [slingshot](https://github.com/scgilardi/slingshot).
"

[[:section {:title "Motivation"}]]

"
For those that are not familiar with restart systems (most well know in Common Lisp), they can be thought of as an issue resolution system or `try++/catch++`. In `hara.event`, we use the term `issues` to differentiate them from `exceptions`. The difference is purely semantic: `issues` are `managed` whilst `exceptions` are `caught`. They all refer to abnormal program flow.

There are two forms of `exceptions` that a programmer will encounter:

 1. **Programming Mistakes** - These are a result of logic and reasoning errors, should not occur in normal operation and should be eliminated as soon as possible. The best strategy for dealing with this is to write unit tests and have functions fail early with a clear message to the programmer what the error is.
  - Null pointers
  - Wrong inputs to functions

 2. **Exceptions due to Circumstances** - These are circumstancial and should be considered part of the normal operation of the program.
  - A database connection going down
  - A file not found
  - User input not valid

The method of `try` and `catch` exception handing, though commonly used and familiar to most programmers is a very weak system for exception handling. For *Type 1* exceptions,

really needed when dealing with the *Type 1* exceptions and a little too weak when dealing with those of *Type 2*. The net effect of using only the `try/catch` paradigm in application code is that in order to mitigate these *Type 2* exceptions, there requires a lot of defensive programming. This turns the middle level of the application into spagetti code with program control flow (`try/catch`) mixed in with program logic.

Conditional restarts provide a way for the top-level application to more cleanly deal with *Type 2* exceptions. A simple use case looking at advantages in using restarts over exceptions can be seen in the [Quickstart](#unlucky-numbers).

For those that wish to know more about conditional restarts, a comparison of different strategies that can be implemented is done in [Chapter {{control-strategies}}](#control-strategies). While for those curious about how this jumping around has been achieved, look at [Chapter {{4mplementation}}](#implementation).

"


[[:section {:title "Raising issues"}]]

"
`ribol` provide richer semantics for resolution of *Type 2* exceptions. Instead of `throw`, a new form `raise` is introduced ([e.{{raise-syntax}}](#raise-syntax)).
"

[[{:title "raise syntax"}]]
(comment
  (raise {:input-not-string true :input-data 3}     ;; issue payload
         (option :use-na [] "NA")                   ;; option 1
         (option :use-custom [n] n)                 ;; option 2
         (default :use-custom "nil"))               ;; default choice
)

"
`raise` differs to `throw` in a few ways:
  - issues are of type `clojure.lang.ExceptionInfo`.
  - the payload is a `hash-map`.
  - **optional**: multiple `option` handlers can be specified.
  - **optional**: a `default` choice can be specified.
"

[[:section {:title "Managing issues"}]]

"Instead of the `try/catch` combination, `manage/on` is used ([e.{{manage-syntax}}](#manage-syntax))."

[[{:title "manage/on syntax" :tag "manage-syntax"}]]
(comment
  (manage (complex-operation)
          (on :node-working [node-name]
              (choose :wait-for-node))
          (on :node-failed [node-name]
              (choose :reinit-node))
          (on :database-down []
              (choose :use-database backup-database))
          (on :core-failed []
              (terminate-everything)))
)


"Issues are managed through `on` handlers within a `manage` block. If any `issue` is raised with the manage block, it is passed to each handler. There are six ways that a handler can deal with a raised issue:
  - directly (same as `try/catch`)
  - using `continue` to keep going with a given value
  - using `choose` to specify an option
  - using `escalate` to notify higher level managers
  - using `default` to allow the issue to resolve itself
  - using `fail` to throw an exception

Using these six different different issue resolution directives, the programmer has the richness of language to craft complex process control flow strategies without mixing logic handling code in the middle tier. Restarts can also create new ways of thinking about the problem beyond the standard `throw/catch` mechanism and offer more elegant ways to build programs and workflows.
"

;;[[:file {:src "test/clj/midje_doc/ribol_api.clj"}]]

;;[[:file {:src "test/clj/midje_doc/ribol_strategies.clj"}]]

;;[[:file {:src "test/clj/midje_doc/ribol_implementation.clj"}]]
