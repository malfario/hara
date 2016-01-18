(ns documentation.hara-concurrent)

[[:chapter {:title "Introduction"}]]

"
[hara.concurrent](https://github.com/zcaudate/hara/blob/master/src/hara/concurrent.clj) provides a set of concurrency primitives and components so that concurrent applications can be built and designed in a modular fashion."

[[:section {:title "Installation"}]]

"
Add to `project.clj` dependencies:

    [im.chit/hara.concurrent \"{{PROJECT.version}}\"]

Individual namespaces can be added seperately:

    [im.chit/hara.concurrent.latch \"{{PROJECT.version}}\"]
    [im.chit/hara.concurrent.notification \"{{PROJECT.version}}\"]
    [im.chit/hara.concurrent.pipe \"{{PROJECT.version}}\"]
    [im.chit/hara.concurrent.propagate \"{{PROJECT.version}}\"]
"

[[:chapter {:title "API - latch"}]]

"[hara.concurrent.latch](https://github.com/zcaudate/hara/blob/master/src/hara/concurrent/latch.clj) supplies a simple master/slave latch mechanism for atoms and ref such that if the master if updated, then the slave will as well"

[[:api {:namespace "hara.concurrent.latch"}]]

[[:chapter {:title "API - notification"}]]

"[hara.concurrent.notification](https://github.com/zcaudate/hara/blob/master/src/hara/concurrent/notification.clj) introduces a way to be notified of changes to a system, based on this [post](http://stackoverflow.com/questions/13717161/are-there-any-good-libraries-or-strategies-for-testing-multithreaded-application)"

[[:api {:namespace "hara.concurrent.notification"}]]

[[:chapter {:title "API - pipe"}]]

"[hara.concurrent.pipe](https://github.com/zcaudate/hara/blob/master/src/hara/concurrent/pipe.clj) provides a simple implemention of a asynchronous pipe that can be sent tasks that are queued until previous tasks are complete"

[[:api {:namespace "hara.concurrent.pipe"}]]

[[:chapter {:title "API - propagate"}]]

"[hara.concurrent.propagate](https://github.com/zcaudate/hara/blob/master/src/hara/concurrent/propagate.clj) is another implemention around the concept of [propagators](http://web.mit.edu/~axch/www/art.pdf), introduced by [Sussman](https://vimeo.com/12184930)"

[[:api {:namespace "hara.concurrent.propagate"}]]
