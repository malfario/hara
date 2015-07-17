(ns documentation.hara-io-scheduler
  (:use midje.sweet)
  (:require [hara.io.scheduler :refer :all]))

[[:chapter {:title "Introduction"}]]

[[:section {:title "Installation"}]]

"Add to `project.clj` dependencies:

    [im.chit/hara.io.scheduler \"{{PROJECT.version}}\"]"

"All functions are in the `hara.io.scheduler` namespace."

(comment (use 'hara.io.scheduler))

[[:section {:title "Motivation"}]]

"
`hara.io.scheduler` aims to provide an easy and intuitive way of specifying, testing and managing scheduled tasks. Much emphasis has been placed upon task management, the ability to inspect and kill running tasks as well as simulation, the ability to shorten time so that the scheduler can quickly run tests over and over again. These two features make for a very simple to use, but powerful package for dealing with cron-like tasks.

The library was originally developed as [cronj](https://www.github.com/zcaudate/cronj) but has now been included as part of the larger [hara](https://www.github.com/zcaudate/hara) codebase. The innards have been more clearly abstracted though building with [hara.component](hara-component.html). Whilst the dependency on [clj-time](https://www.github.com/clj-time/clj-time) is now option. Another feature that has been added is the ability to control a task's execution model, done through building on top of [hara.concurrent.procedure](hara-concurrent-procedure.html).
"

[[:section {:title "Other Libraries"}]]

"
`hara.io.scheduler` is just on of many scheduling libraries in the clojure world including:

- [at-at](https://github.com/overtone/at-at)
- [chime](https://github.com/james-henderson/chime)
- [clj-cronlike](https://github.com/kognate/clj-cronlike)
- [cron4j](http://www.sauronsoftware.it/projects/cron4j)
- [monotony](https://github.com/aredington/monotony)
- [quartzite](https://github.com/michaelklishin/quartzite)
- [schejulure](https://github.com/AdamClements/schejulure)
"
