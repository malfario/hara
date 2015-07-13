(ns documentation.hara-concurrent-procedure)

[[:chapter {:title "Introduction"}]]

"`hara.concurrent.procedure` provides a wrapper around a function in order to control the execution of a function such as caching, asynchronous dispatch, timing and other runtime information. This is a very useful construct for workflow modelling and concurrent applications where the library provides rich information about the execution of a particular running instance:

- the function that originated the process instance
- the thread or future on which the instance is executing
- the result (maybe cached) of the execution if returned
- the time of execution
- the id of the process (used for identification)
- other running instances of the process
"

[[:section {:title "Installation"}]]

"Add to `project.clj` dependencies:

    [im.chit/hara.concurrent.procedure \"{{PROJECT.version}}\"]"

"All functions are in the `hara.concurrent.procedure` namespace."

(comment (use 'hara.concurrent.procedure))

[[:section {:title "Motivation"}]]
