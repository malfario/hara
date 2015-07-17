(ns documentation.hara-event
  (:use midje.sweet)
  (:require [hara.event :refer :all]))

[[:chapter {:title "Introduction"}]]

"
[hara.event](https://github.com/zcaudate/hara/blob/master/src/hara/event.clj) aims to provide more loosely coupled code through two mechanisms:

- a global eventing system for decoupilng of side-effecting
- a conditional restart framework that hooks into the eventing system.

`hara.event` was originally developed as a [seperate library](https://github.com/zcaudate/ribol) but has been included as part of the larger [hara](https://github.com/zcaudate/hara) codebase. The main addition to the original library has been the inclusion of the eventing system as it was felt that there should be an integrated way of dealing with side-effecting calls such as logging, indexing, emails and many other tasks within both normal and abnormal program flows.
"

[[:section {:title "Installation"}]]

"Add to `project.clj` dependencies (use double quotes):

    [im.chit/hara.event '{{PROJECT.version}}']"

"All functionality is found contained in the `hara.event` namespace"

(comment  (use 'hara.event))

[[:section {:title "Motivation"}]]

"
For those that are not familiar with restart systems (most well know in Common Lisp), they can be thought of as an issue resolution system or `try++/catch++`. In `hara.event`, we use the term `issues` to differentiate them from `exceptions`. The difference is purely semantic: `issues` are `managed` whilst `exceptions` are `caught`. They all refer to abnormal program flow.

There are two forms of `exceptions` that a programmer will typically encounter:

 1. **Programming Mistakes** - These are a result of logic and reasoning errors, should not occur in normal operation and should be eliminated as soon as possible. The best strategy for dealing with this is to use offensive checks such as pre- and post- conditions, write unit tests and have functions fail early with a clear message to the programmer about the error. Typical cases are:

  - Null pointer exceptions
  - Wrong inputs to functions

 2. **Exceptions due to Circumstances** - These are circumstancial and should be considered part of the normal operation of the program. There are a wide variety of such exceptions and usually is up to the programming workflow to handle such cases:
  - A database connection going down
  - A file not found
  - User input not valid

The method of `try` and `catch` exception handing, though commonly used and familiar to most programmers is a very weak system for exception handling. The try blocks are not really needed when dealing with the *Type 1* exceptions and a little too weak when dealing with those of *Type 2*. The net effect of using only the `try/catch` paradigm in application code is that in order to mitigate these *Type 2* exceptions, there requires a lot of defensive programming. This turns the middle level of the application into spagetti code with program control flow (`try/catch`) mixed in with program logic.

Conditional restarts provide a way for the top-level application to more cleanly deal with *Type 2* exceptions. A simple use case looking at advantages in using restarts over exceptions can be seen in the [quickstart](#quickstart). For those that wish to know more about conditional restarts, there is a comprehensive [strategies](#strategies) chapter to the listing different ways abnormal program flow can be dealt with. For those curious about how the framework has been implementated, please jump over to the [implementation](#implementation) chapter.
"

[[:section {:title "Other Libraries"}]]

"
There are currently three other conditional restart libraries for clojure, in the more traditional Common Lisp style syntax:

- [errorkit](https://github.com/richhickey/clojure-contrib/blob/master/src/main/clojure/clojure/contrib/error_kit.clj) was the first and provided the guiding architecture for this library.

- [swell](https://github.com/hugoduncan/swell) and [conditions](https://github.com/bwo/conditions) have been written to work with [slingshot](https://github.com/scgilardi/slingshot).
"

[[:chapter {:title "Event Management"}]]

[[:section {:title "Listeners and Managers"}]]

"
In any program, we see the following patterns

- side effects occur within both abnormal and normal program flow (usually logging)
- side-effecting libraries coupled to the main logic (logback, email systems, alerts)
- usually some sort of logging has to be done on abnormal program flow

`hara.event` provides for listeners and managers:

- Listeners act on events they have jurisdiction over for both `signal` and `raise` calls. All listeners participate.
- Managers act in a hierarchical fashion where if an issue is raised, the closest manager will handle the issue.

An example is provided below:
"

[[:image {:src "img/hara_event/event_pathway.png" :title "example pathways" :width "600px"}]]

"
The only difference between `signal` and `raise` calls is that `signal` does not trigger `manage` handlers. Both calls will trigger any compatible listener. The listers and managers act in a horizontal/vertical or only/all fashion to provide for better decoupilng of functionality within the code base.
"

[[:section {:title "Signal Syntax"}]]

"`signal` typically just informs its listeners with a given set of information:"

(comment
  (signal {:everything-is-good true :input-data 3})
  )

[[:section {:title "Listener Syntax"}]]

"`signal` typically just informs its listeners with a given set of information:"

(comment
  (deflistener print-listener :log
    data
    (println data)))

[[:section {:title "Raise Syntax"}]]

"Instead of `throw`, a new form `raise` is introduced ([e.{{raise-syntax}}](#raise-syntax)):
"

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

[[:section {:title "Manage Syntax"}]]

"Instead of the `try/catch` combination, `manage/on` is used ([e.{{manage-syntax}}](#manage-syntax))."

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

[[:chapter {:title "Quickstart"}]]

[[:file {:src "test/documentation/hara_event/unlucky_numbers.clj"}]]

[[:chapter {:title "API"}]]

[[:file {:src "test/documentation/hara_event/api.clj"}]]

[[:chapter {:title "Strategies"}]]

[[:file {:src "test/documentation/hara_event/strategies.clj"}]]

[[:chapter {:title "Implementation"}]]

[[:file {:src "test/documentation/hara_event/implementation.clj"}]]

[[:chapter {:title "Links and Resources"}]]

"
Here are some more links and resources on the web:

- [beyond exception handling](http://www.gigamonkeys.com/book/beyond-exception-handling-conditions-and-restarts.html) - Peter Seibel's chapter on conditional restarts
- [why exceptions should cascade like stylesheets](http://z.caudate.me/why-exceptions-are-like-stylesheets/) - original article on the philosophy behind [ribol](http://www.github.com/zcaudate/ribol)
- [the power of abstraction](http://www.youtube.com/watch?v=GDVAHA0oyJU) - excellent talk by Barbara Liskov
"
