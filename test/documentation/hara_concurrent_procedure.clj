(ns documentation.hara-concurrent-procedure
  (:use midje.sweet)
  (:require [hara.concurrent.procedure :refer :all]))

[[:chapter {:title "Introduction"}]]

"`hara.concurrent.procedure` provides a wrapper in order to control the execution of a function such as restarts, timeouts, caching, synchronous/asynchronous dispatch, timing and other related params. This is a very useful construct for workflow modelling and concurrent applications where the library provides rich information about the execution of a particular running instance:

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


(defprocedure hello )

(procedure {:name    "println"
            :handler (fn [t params instance]
                       (println "INSTANCE: " instance)
                       (Thread/sleep 500)
                       (println "ENDED" t))
            :id-fn :timestamp
            :cached true
            :params {:b 2}
            ; :overwrite true
            ; :mode :sync
            }
           [:timestamp :params :instance])

(defprocedure hello
  [:timestamp :params :instance]
  [t params instance]
  (println t params instance))

(hello 123 {} {})
