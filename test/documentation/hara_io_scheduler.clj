(ns documentation.hara-io-scheduler
  (:use midje.sweet)
  (:require [hara.io.scheduler :refer :all]
            [hara.concurrent.procedure.registry :as registry]))

[[:chapter {:title "Introduction"}]]

[[:section {:title "Installation"}]]

"Add to `project.clj` dependencies:

    [im.chit/hara.io.scheduler \"{{PROJECT.version}}\"]"

"All functions are in the `hara.io.scheduler` namespace."

(comment (use 'hara.io.scheduler))

[[:section {:title "Motivation"}]]

"
`hara.io.scheduler` aims to provide an easy and intuitive way of specifying, testing and managing scheduled tasks. Much emphasis has been placed upon task management, the ability to inspect and kill running tasks as well as simulation, the ability to shorten time so that the scheduler can quickly run tests over and over again. These two features make for a very simple to use, but powerful package for dealing with cron-like tasks.

The library was originally developed as [cronj](https://www.github.com/zcaudate/cronj) but has now been included as part of the larger [hara](https://www.github.com/zcaudate/hara) codebase. The innards have been more clearly abstracted though building with [hara.component](hara-component.html). Whilst the dependency on [clj-time](https://www.github.com/clj-time/clj-time) is now option. Another feature that has been added is the ability to control a task's execution model, done through building on top of a [procedure](hara-concurrent-procedure.html).
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

[[:chapter {:title "Concepts and Design"}]]

"`hara.io.scheduler` was built around a concept of a **task**. A task has two components:
- A `handler`  (what is to be done)
- A `schedule` (when it should be done)

Tasks are synchronized to run using a `clock`. If a task was scheduled to run at that time, it's `handler` would be run in a seperate thread. In this section, the general features and use cases for `hara.io.scheduler` will be shown as well as the novel ideas introduced in the library. These ideas will be exanded upon in the [walkthrough](#walkthrough):
"

[[:section {:title "Seperation of Concerns"}]]
"
A task handler is a function that can take up to three arguments:

- `t`, the timestamp
- `params`, custom data associated with the task
- `instance`, options to change the execution model of the task
"

(comment
   (fn [t params instance]
      (... perform a task ...)))

"
`t` represents the time at which the handler was called. This solves the problem of *time synchronisation*. For example, I may have three tasks scheduled to run at a same time:

   - perform a calculation and write the result to the database
   - perform a http call and write result to the database
   - load some files, write to single output then store file location to the database.

All these tasks will end at different times. To retrospectively reasoning about how all three tasks were synced, each handler is required to accept the triggred time `t` as an argument.

`params` is a hashmap, for example `{:path '/app/videos'}`. It has been found that user customisations such as server addresses and filenames, along with job schedules are usually specified at the top-most tier of the application whilst handler logic is usually in the middle-tier. Having an extra `opts` argument allow for better seperation of concerns and more readable code.

`instance` is also a hashmap to manipulate whether to run the function synchronously or asynchronously, to access cached data, etc. More examples can be seen in the [procedure](hara-concurrent-procedure.html) docs.
"

[[:section {:title "Thread Management"}]]
"
Fully-featured thread management capabilities have been implemented:

- tasks can start at the next scheduled time before the previous thread has finished running so that multiple threads can be running simultaneously for a single task.
- running threads can be listed.
- normal and abnormal termination:
    - kill a running thread
    - kill all running threads in a task
    - kill all threads
    - disable task but let running threads finish
    - stop timer but let running threads finish
    - shutdown timer, kill all running threads
"

[[:section {:title "Simulation Testing"}]]

"
Because the `clock` module is been completely decoupled from the task array it was very easy to add a simulation component into library. Simulation has some very handy features:
- Simulate how the entire system would behave over a long periods of time
- Generating test inputs for other applications.
- Both single and multi-threaded execution strategies are supported.
"

[[:chapter {:title "Walkthrough"}]]

[[:section {:title "Creating a Task"}]]

"We start off by defining a very basic function to essentially print what was passed to it:"

(defn print-fn [t params]
  (println (:output params) ":" t))

" We define `print-task`, which adds schedule and custom param information:"

(def print-task
  {:handler print-fn
   :schedule "/2 * * * * * *"
   :params {:output "Hello There"}})

[[:section {:title "Running Tasks"}]]

"We define the scheduler `sch` as having a single task:"

(def sch1 (scheduler {:print-task print-task}))

"Typically, we will read `:schedule` and `:params` from a config. In order to seperate code and data, there is an alternate two argument version that can be used for initialisation:"

(comment
  (def sch1 (scheduler {:print-task print-fn}
                      (<load-config> "<filename>.edn"))))

"So now we can start playing around with starting and stopping the scheduler:"

(comment
  (start! sch)
  ;; > Hello There :  #inst "2015-07-17T06:26:44.000-00:00"

  ... wait 2 secs ...
  
  ;; > Hello There :  #inst "2015-07-17T06:26:46.000-00:00"

  ... wait 2 secs ...

  ;; > Hello There :  #inst "2015-07-17T06:26:48.000-00:00"

  ... wait 2 secs ...

  ;; > Hello There :  #inst "2015-07-17T06:26:50.000-00:00"
  
  (stop! sch) ;; OUTPUT STOPS
  )

[[:section {:title "Components"}]]

"Note that because `start!` is really just a call to `component/start`, the following works as well:"

(comment
  (def sch1 (component/start sch))
  ;; > Hello There :  #inst "2015-07-17T06:26:44.000-00:00"

  ... wait 2 secs ...
  
  ;; > Hello There :  #inst "2015-07-17T06:26:46.000-00:00"

  ... wait 2 secs ...

  ;; > Hello There :  #inst "2015-07-17T06:26:48.000-00:00"

  ... wait 2 secs ...

  ;; > Hello There :  #inst "2015-07-17T06:26:50.000-00:00"
  
  (def sch1 (component/stop sch)) ;; OUTPUT STOPS
  )


[[:section {:title "Schedule"}]]

"Each task has a `:schedule` entry. The value is a string specifying when it is supposed to run. The string is of the same format as `crontab` -  seven elements seperated by spaces. The elements are used to match the time, expressed as seven numbers:

     second minute hour day-of-week day-of-month month year

The rules for a match between the crontab and the current time are:

- `A`       means match on `A`
- `*`       means match on any number
- `E1,E2`   means match on both `E1` and `E2`
- `A-B`     means match on any number between `A` and `B` inclusive
- `/N`      means match on any number divisible by `N`
- `A-B/N`   means match on any number divisible by `N` between `A` and `B` inclusive

Where `A`, `B` and `N` are numbers; `E1` and `E2` are expressions. All seven elements in the string have to match in order for the task to be triggered.
"

(comment

  ;; Triggered every 5 seconds

  "/5 * * * * * *"


  ;; Triggered every 5 seconds between 32 and 60 seconds

  "32-60/5 * * * * * *"

  ;; Triggered every 5 seconds on the 9th aand 10th
  ;; minute of every hour on every Friday from June
  ;; to August between years 2012 to 2020.

  "/5  9,10  * 5 * 6-8 2012-2020")


[[:chapter {:title "Simulation"}]]

"Simulations are a great way to check if the system is working correctly. This allows an entire system to be tested for correctness. How `simulate` works is that it decouples the `clock` from the task array and forces tasks to trigger on the range of date inputs provided."

[[:section {:title "Y2K Revisited"}]]
"For instance, we wish to test that our `print-handler` method was not affected by the Y2K Bug. `T1` and `T2` are defined as start and end times:"

(def T1 #inst "1999-12-31T23:59:50.00-00:00")
(def T2 #inst "2000-01-01T00:00:10.00-00:00")


"The simulation is then run from `T1` to `T2` and the results are shown instantaneously"

(simulate sch1
          {:start T1
           :end   T2})
;; > Hello There : #inst "1999-12-31T23:59:50.000-00:00"
;; > Hello There : #inst "1999-12-31T23:59:52.000-00:00"
;; > Hello There : #inst "1999-12-31T23:59:54.000-00:00"
;; > Hello There : #inst "1999-12-31T23:59:56.000-00:00"
;; > Hello There : #inst "1999-12-31T23:59:58.000-00:00"
;; > Hello There : #inst "2000-01-01T00:00:00.000-00:00"
;; > Hello There : #inst "2000-01-01T00:00:02.000-00:00"
;; > Hello There : #inst "2000-01-01T00:00:04.000-00:00"
;; > Hello There : #inst "2000-01-01T00:00:06.000-00:00"
;; > Hello There : #inst "2000-01-01T00:00:08.000-00:00"

"We can control the way the simulation is run through other params"

(simulate sch1
          {:start T1
           :end   T2
           :mode  :async
           :pause 0
           :step  3})
;; > Hello There : #inst "1999-12-31T23:59:56.000-00:00"
;; > Hello There : #inst "2000-01-01T00:00:02.000-00:00"
;; > Hello There : #inst "2000-01-01T00:00:08.000-00:00"
;; > Hello There : #inst "1999-12-31T23:59:50.000-00:00"

"`:mode` can be either `:sync` (default) or `:async`. `:step` is the number of second to wait to test again and pause is the sleep time in milliseconds."


[[:section {:title "Interval and Pause"}]]

"
It can be seen that we can simulate the actual speed of outputs by keeping the step as 1 and increasing the pause time to 1000ms"


(comment
  (simulate sch1
            {:start T1
             :end   T2
             :mode  :async
             :pause 1000
             :step  1}))
;; > Hello There : #inst "1999-12-31T23:59:50.000-00:00"

;;            ... wait 2 seconds ...

;; > Hello There : #inst "1999-12-31T23:59:52.000-00:00"

;;            ... wait 2 seconds ...

;; > Hello There : #inst "1999-12-31T23:59:54.000-00:00"


[[:subsection {:title "Speeding Up"}]]
"In the following example, the step has been increased to 2 whilst the pause time has decreased to 100ms. This results in a 20x increase in the speed of outputs."
[[{:numbered false}]]
(comment
  (simulate sch1
            {:start T1
             :end   T2
             :mode  :async
             :pause 100
             :step  2})

;; > Hello There : #inst "1999-12-31T23:59:50.000-00:00"

;;            ... wait 0.1 seconds ...

;; > Hello There : #inst "1999-12-31T23:59:52.000-00:00"

;;            ... wait 0.1 seconds ...

;; > Hello There : #inst "1999-12-31T23:59:54.000-00:00"
  )

"Being able to adjust these simulation parameters are really powerful testing tools and saves an incredible amount of time in development. For example, we can quickly test the year long output of a task that is scheduled to run once an hour very quickly by making the interval 3600 seconds and the pause time to the same length of time that the task takes to finish. Through simulations, task scheduling can now be tested and entire systems just got easier to manage and reason about."

[[:chapter {:title "Task Management"}]]

"Task management capabilities are demonstrated by first creating two task entries labeled `l1` and `l2` doing nothing but sleeping for a long time:"

(def sch2
  (scheduler {:l1 {:handler  (fn [t params] (Thread/sleep 30000000000000))
                   :schedule "/2 * * * * * *"
                   :params {:data "foo"}}
              :l2 {:handler  (fn [t params] (Thread/sleep 30000000000000))
                   :schedule "/2 * * * * * *"
                   :params {:data "bar"}}}))

[[:section {:title "trigger!"}]]

"We can manually start a task using `trigger!` and a key:"

(trigger! sch2 :l1 :first-run)

"And then look at the running instances:"

(list-instances sch2 :l1)
;; => (#proc[:first-run]{:args (:run {:data "foo"}),
;;                       :name :l1, :mode :async,
;;                       :params {:data "foo"}, :result :waiting,
;;                       :runtime {:started #inst "2015-07-17T07:37:10.818-00:00"},
;;                       :interrupt false,
;;                       :input (:run {:data "foo"} {}),
;;                       :timestamp :run})

"Using a key is good for when you just want one instance to run at a time, calling it a second time will not start another process"

(fact
  (trigger! sch2 :l1 :first-run)
  (count (list-instances sch2 :l1))
  => 1)

"Calling `trigger!` with a different key will start another process:"

(fact
  (trigger! sch2 :l1 :second-run)
  (count (list-instances sch2 :l1))
  => 2)

"Calling `trigger!` without a key will use the current timestamp. "

(fact
  (trigger! sch2 :l1)
  ;; => #proc[Fri Jul 17 13:15:59 IST 2015]
  ;;      {:args (#inst "2015-07-17T07:45:59.464-00:00" {:data "foo"}),
  ;;       :name :l1, :mode :async, :params {:data "foo"},
  ;;       :result :waiting,
  ;;       :runtime {:started #inst "2015-07-17T07:45:59.464-00:00"},
  ;;       :interrupt false,
  ;;       :input (#inst "2015-07-17T07:45:59.464-00:00" {:data "foo"} {}),
  ;;       :timestamp #inst "2015-07-17T07:45:59.464-00:00"}
  (count (list-instances sch2 :l1))
  => 3)

[[:section {:title "killing instances"}]]

"We can kill them individually using `kill-instance`:"

(fact  
  (kill-instance sch2 :l1 :first-run)
  => true

  (count (list-instances sch2 :l1))
  => 2)

"We can kill all instances of a particular task using `kill-instances`:"

(fact  
  (kill-instances sch2 :l1)
  => [true true]

  (count (list-instances sch2 :l1))
  => 0)

"We can kill all running instances of all tasks within a scheduler with `kill-all`:"

(fact
  (trigger! sch2 :l1)
  (Thread/sleep 100)
  (trigger! sch2 :l1)
  (Thread/sleep 100)
  (trigger! sch2 :l1)
  
  (trigger! sch2 :l2)
  (Thread/sleep 100)
  (trigger! sch2 :l2)
  (Thread/sleep 100)
  (trigger! sch2 :l2)

  
  (count (list-instances sch2 :l1))
  => 3

  (count (list-instances sch2 :l2))
  => 3

  (kill-all sch2)

  (count (list-instances sch2 :l1))
  => 0

  (count (list-instances sch2 :l2))
  => 0)

[[:section {:title "live action"}]]

"To show some of these methods in action, we will call these methods on a running scheduler:"

(comment

  (start! sch2)
  

  ... wait a moment and see how many instances are running...
  
  (count (list-instances sch2 :l1))
  => 10

  ... wait and check again ...

  (count (list-instances sch2))
  => 44

  ... and again ...

  (count (list-instances sch2))
  => 145

  ... okay that's too many threads being generated ...

  (stop! sch2)

  ... stop! doesn't kill the instances that are already running ...

  (count (list-instances sch2))
  => 145

  ... so we have to kill them manually ...
  
  (kill-all sch2)
  => [true true true true
      ...
      ...
      true true true true true]

  (count (list-instances sch2))
  => 0)

[[:section {:title "shutdown and restart"}]]

"To show the difference between `shutdown!` and `stop!`, lets start up the scheduler again:"

(comment

  (start! sch2)

  
  ... and again we wait ...
  
  (count (list-instances sch2))
  => 10

  ... we now have instances running ...

  (shutdown! sch2)

  ... after the call, we notice that there are no running istances ...

  (count (list-instances sch2))
  => 0)
   
"`restart!` does the same as `shutdown!` but just starts up the system again once it has been stopped."

[[:chapter {:title "Links and Resources"}]]

"
Here are some more links and resources on the web:

- [immutability, time and testable task schedulers](http://z.caudate.me/immutability-time-and-task-schedulers/) - original article on the philosophy behind [cronj](http://www.github.com/zcaudate/cronj)
"
