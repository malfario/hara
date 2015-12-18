(ns documentation.hara-concurrent-procedure
  (:use midje.sweet)
  (:require [hara.concurrent.procedure :refer :all]
            [hara.concurrent.procedure.registry :as registry]))

[[:chapter {:title "Introduction"}]]

"`hara.concurrent.procedure` provides a wrapper-like layer for controlling the execution of concurrent operations. Many features are added in order to support use in the real world. These include restarts, interrupts, timeouts, caching, synchronous/asynchronous dispatch, timing and other issues associated with concurrent systems."

[[:section {:title "Installation"}]]

"Add to `project.clj` dependencies:

    [im.chit/hara.concurrent.procedure \"{{PROJECT.version}}\"]"

"All functions are in the `hara.concurrent.procedure` namespace."

(comment (use 'hara.concurrent.procedure))

[[:section {:title "Motivation"}]]

"The `concurrent.procedure` construct is like a future, but contains more information and so is more **self aware**. This is a very useful construct for workflow modelling and concurrent applications where the library provides rich information about the execution of a particular running instance:

- the function that originated the process instance
- the thread or future on which the instance is executing
- the result (maybe cached) of the execution if returned
- the time of execution
- the id of the process (used for identification)
- other running instances of the proces"

[[:chapter {:title "Features"}]]

[[:section {:title "Basics"}]]

"There are many uses for a function that stores information about it's execution, as well as having it's execution properties be customised through input arguments. This will be demonstrated by creating a very simple function that waits for a second before outputting a result. As can be seen, there is an additional map before the argument vector and it's use will be shown later."

(defprocedure hello {}
  []
  (Thread/sleep 1000)
  "Hello")

"We can call hello and see that it yields a future-like reference for the execution of the function. This will be called an `instance` of execution."

(hello)
;; => #proc[d858c5b0-7671-41f1-96af-fdd768c24e84]
;; {:args (), :input nil, :mode :async,
;;  :runtime {:started #inst "2015-12-08T15:10:41.695-00:00"},
;;  :interrupt false,
;;  :timestamp #inst "2015-12-08T15:10:41.695-00:00",
;;  :result :waiting}

"The result of the function is shown after one second when dereferenced."

@(hello)
;; => "Hello" <after one second>

[[:section {:title "Mode"}]]

"We now change the procedure slightly by setting the mode to `:sync`, by default the mode is `:async`"

(defprocedure hello {:mode :sync}
  []
  (Thread/sleep 1000)
  "Hello")

"Notice that the call to `hello` does not return immediately but pauses for one second before returning the execution instance. Instead of running asynchronously like before, the call is synchronous. Notice that `:runtime` has an end time and there the value for `:result` has changed from `:waiting` to a map having `:type` and `:data` keys."

(hello)
;; => #proc[0deadecf-2503-4653-9380-1739c6f1b1f8]
;; {:args (), :input nil, :mode :sync,
;;  :runtime {:ended #inst "2015-12-08T15:34:53.250-00:00", :started #inst "2015-12-08T15:34:52.248-00:00"},
;;  :interrupt false, :timestamp #inst "2015-12-08T15:34:52.248-00:00",
;;  :result {:type :success, :data "Hello"}}

"When it is dereferenced, there is no difference between the dereference second and the first `hello`s"

@(hello)
;; => "Hello" <after one second>

[[:section {:title "Control of Execution"}]]

"Lets add another option to our procedure, this time `:arglist`"

(defprocedure hello {:mode :sync :arglist [:mode]}
  []
  (Thread/sleep 1000)
  "Hello")

"We should be familiar with the call to `hello` because it behaves the same to the previous definitions when called directly:"

(hello)
;; => #proc[7b2de1b5-267b-4d54-be66-1fd5fabd0a4c]
;; {:args (), :input nil, :mode :sync,
;;  :runtime {:ended #inst "2015-12-08T15:49:35.948-00:00", :started #inst "2015-12-08T15:49:34.946-00:00"},
;;  :interrupt false, :timestamp #inst "2015-12-08T15:49:34.946-00:00",
;;  :result {:type :success, :data "Hello"}}

"However, we can play with what mode we wish the function to run by running `hello` with an additional argument:"

(hello :async)
;; => #proc[50d3d2fb-faa3-4bd4-810d-936b30881b55]
;; {:args (), :input (:async), :mode :async,
;;  :runtime {:started #inst "2015-12-08T15:51:08.486-00:00"},
;;  :interrupt false, :timestamp #inst "2015-12-08T15:51:08.486-00:00",
;;  :result :waiting}

"When it is dereferenced, there should be no difference of behavior to the previous definitions."

@(hello :async)
;; => "Hello" <after one second>

"The `:arglist` can be seen as an outer wrapper on top of the original function. The arglist count should always be equal or more than the function's argument count. In this way, the execution of the function can be controlled by the caller using only data. This is extremely useful as will be seen next:"

[[:section {:title "Instance"}]]

"There are special keywords that we can put in the `:arglist` vector in order to adjust the behavior of execution. The most general is `:instance`. With `:instance`, we can overwrite any execution property. This is shown in an example below:"

(defprocedure hello {:mode :sync :arglist [:instance]}
  [instance]
  (Thread/sleep (:sleep instance))
  (:mode instance))

""

@(hello {:sleep 2000 :mode :async})
;; => :async <after two seconds>

"We can see what the `instance` variable passed to the function actually contains:"

(defprocedure hello {:mode :sync :arglist [:instance]}
  [instance]
  (Thread/sleep (:sleep instance))
  (keys instance))

""

@(hello {:sleep 100 :mode :async})
;; => (:args :time :arglist :registry :mode :procedure
;;     :id-fn :cache :result :id :runtime :interrupt
;;     :input :timestamp :handler :sleep) <after 100ms>

"In fact, this structure is exactly what we get when we call `hello` without dereferencing:"

(keys (hello {:sleep 100 :mode :async}))
;; => (:args :time :arglist :registry :mode :procedure
;;     :id-fn :cache :result :id :runtime :interrupt
;;     :input :timestamp :handler :sleep) <occurs instantly>

[[:section {:title "Runtime and Registry"}]]

"We can look at the function's execution through it's `:runtime` and `:registry` keys. We can reuse the previous definition of `hello` to create two long running functions:"

(def a (hello {:name "hello" :sleep 100000 :mode :async :id "instance-a"}))
(def b (hello {:name "hello" :sleep 100000 :mode :async :id "instance-b"}))

"Now that we have the instances of execution, we can view when it has been started:"

(:runtime a)
;; => #<Atom@9cab002: {:started #inst "2015-12-10T05:56:17.685-00:00"}>

"As well as access all running instances through a global registry:"

(:registry a)
;; => #reg {"hello" ("instance-b" "instance-a")}

"We can stop the execution of `instance a`:"

(comment (require '[hara.concurrent.procedure.registry :as registry]))

(registry/kill (:registry a) "hello" "instance-a")
;; => true

"A check will reveal that `instance b` is still running"

(:registry a)
;; => #reg {"hello" ("instance-b")}

"`instance-b` is accessible from `instance-a`:"

(-> a :registry deref (get "hello") (get "instance-b"))
;; => #proc[instance-b]{:args (nil),
;; :input ({:name "hello", :mode :async, :id "instance-b", :sleep 100000}), :mode :async,
;; :runtime {:started #inst "2015-12-10T06:02:21.752-00:00"},
;; :interrupt false, :timestamp #inst "2015-12-10T06:02:21.752-00:00",
;; :result :waiting, :name "hello"}

"As well as to be killed from from `instance-a`:"

(registry/kill (:registry a) "hello" "instance-b")
;; => true

""

(:registry a)
;; => #reg {}

"This is very useful for coordinating strategies between execution instances."

[[:section {:title "Identity and Caching"}]]

"In the previous section, there were concepts like `:id` and `:name` being introduced. These properties give context to the type of function being executed as well as to identify duplicates and wasted execution. To be able to uniquely identify an instance of execution as being the same as another means that additional operations such as caching, timeouts and interrupts can be used for control of execution."

"Let's look at a particular use case where results can be cached. The `power` function has been written using `defprocedure` to include caching. "

(defprocedure power {:arglist [:x :instance] :id-fn :x :cached true :name "power" :interrupt true}
  ([x] @(power x {}))
  ([x instance]
   (Thread/sleep 100)
   (if (zero? x)
     1
     (* x @(power (dec x) (select-keys instance [:cached]))))))

"More about the parameters will be explained later but first, let's try this out. We will calculate 20! and see the caching kick in after the second try:"

@(power 20)
;; => 2432902008176640000 <after some time>

@(power 20)
;; => 2432902008176640000 <instantaneously>

"Sometimes, we actually wish to ignore the cache and have it recalculate. This can be done through arguments instead of any special forms"

@(power 20 {:cached false})
;; => 2432902008176640000 <after some time>

@(power 20)
;; => 2432902008176640000 <instantaneously>

"We can look at the cached values directly from the instance:"

(:cache (power 20))
;; => #cache {"power" {0 ((0 nil)), 7 ((7 nil)), 20 ((20) (20 nil)),
;;                     1 ((1 nil)), 4 ((4 nil)), 15 ((15 nil)), 13 ((13 nil)),
;;                     6 ((6 nil)), 17 ((17 nil)), 3 ((3 nil)), 12 ((12 nil)),
;;                     2 ((2 nil)), 19 ((19 nil)), 11 ((11 nil)), 9 ((9 nil)),
;;                     5 ((5 nil)), 14 ((14 nil)), 16 ((16 nil)), 10 ((10 nil)),
;;                     18 ((18 nil)), 8 ((8 nil))}}

"In concurrent applications, where multiple processes may be doing the same calculation, this type of mechanism will be able to save quite a few clock cycles when used in the right way."

[[:section {:title "Intermission"}]]

"Let's switch gears a little bit and talk philosophy. In working with the standard concurrent execution contructs like futures and promises, it was found that they lacked the self-awareness to coordinate with each other. The reason behind this is very simple: there is dissociation between what should be three concepts critical to concurrent execution: the function (governed by its definition), the execution (governed by time) and the result (governed by input)."

[[:subsection {:title "Synchronous Execution"}]]

"Execution is not an issue in a synchronous world; it occurs with the ticking of the system clock and there are no other processes that are able to affect the world, we can establish a link between the **past** and the **future** because there is no **present** as such in terms of what is happening right now. It just doesn't exist, or rather we don't need to account for it in order to build our programs. In the synchronous world, there is no difference between a function and a lookup table of inputs and outputs"

[[:image {:src "img/hara_concurrent_procedure/synchronous.png" :height "500px" :title "The Synchronous World"}]]

[[:subsection {:title "Concurrent Execution"}]]

"In the concurrent world things happen very differently; or rather, things are required to be accounted very differently in order for a program to succeed. Time the conquerer is the master behind all calculation. To neglect time is a idealistic and will ultimately result in failure."

[[:image {:src "img/hara_concurrent_procedure/concurrent.png" :height "500px" :title "The Concurrent World"}]]

[[:subsection {:title "Coordination of Concurrent Execution"}]]

"An example of how this could be useful can be shown below."

[[:image {:src "img/hara_concurrent_procedure/example.png" :width "100%" :title "Execution Coordination"}]]

"Computation **C** is estimated to take 10 hours to complete and both process A and process B both require the same result. Now, process A has started computation for 9 and a half hours but has not finished and process B is starting. In this case, instead of waiting another 10 hours for computation, if process B is aware that process A is already doing the computation, all it needs to do is to wait on the result of A instead of starting from scratch.

Instead of taking 10 hours, process B will just take 0.5 hours."

[[:section {:title "Overwrite"}]]

"Caching is a part of the bigger problem of how calculation can be shared between one or more concurrent processes. So when working with caching, we have to think about coordination and how processes should be able to work together. Lets try this out with a function that returns a random integer between 0 and 1000."

(defprocedure random-int {:arglist [:instance]
                          :id-fn (constantly "current")
                          :cached true
                          :name "random"
                          :interrupt true}
  ([instance] (rand-int 1000)))

"Lets give this a go by making two calls and watching the cache kick in:"

@(random-int {})
;; => 675

@(random-int {})
;; => 675

"This is may or may not be good depending on the problem. Imagine something like multiple concurrent processes hitting a database to retrieve the same value. Sometimes, we want to make a call without affecting everyone else; other times, we would wish to make a call and then update it globally for all processes. A combination of `:cached` and `:overwrite` allows this to happen."

"In the first case, we see that `:overwrite true` will update the cache with a new value:"

@(random-int {:overwrite true})
;; => 132
@(random-int {})
;; => 132

"In the second case, we see that `:cached false` will return the value whilst skipping the cache altogether"

@(random-int {:cached false})
;; => 276
@(random-int {})
;; => 132

[[:section {:title "Interrupt"}]]

"Another important characteristic is that sometimes, a process is taking too long and we need to restart it. We construct a function showing 50% chance of failure."

(defprocedure random-failure {:arglist [:instance]
                              :id-fn (constantly "current")
                              :name "random"}
  ([instance]
   (println "Function Started:" (:timestamp instance) )
   (if (> 0.5 (rand))
     (Thread/sleep 1000000))
   (println "Function Finished: " (:timestamp instance))))

"The function behavior can be activated by calling it a few times. As the probabily of failure is quite high, We find that there is no output for `Function Finished`."

(random-failure {})
;; Function Started: #inst "2015-12-13T10:21:28.382-00:00"
;; Function Finished:  #inst "2015-12-13T10:21:28.382-00:00"

(random-failure {})
;; Function Started: #inst "2015-12-13T10:21:29.757-00:00"

"Our function is set so that another call of the same type will just wait, subsequent calls will block for the calculation:"

(random-failure {})
;; => <No output>

"By default, a called procedure will block and wait on another of the same type to finish before returning the result. However, if it is required to kill a procedure that is taking too long to finish, setting `:interrupt true` will interrupt the current execution and proceed again from the beginning:"

(random-failure {:interrupt true})
;; Function Started: #inst "2015-12-13T11:35:28.751-00:00"
;; Function Finished:  #inst "2015-12-13T11:35:28.751-00:00"

"This is used for mitigating coordination problems when one process is waiting on the other without reason."

[[:section {:title "Time"}]]

"We can access information about the instant's start and end time through the `:runtime` key. The `:timestamp` key is used for coordination between processes that may have started at slightly different times due to the randomness of the thread pool. We see an example below:"

(defprocedure current [:instance]
  [instance]
  (Thread/sleep 100)
  (-> instance
      (select-keys [:timestamp :runtime])))

"A call to `current` returns information about the operation:"

(-> @(current {})
    (update-in [:runtime] deref))
;; => {:runtime {:ended #inst "2015-12-13T13:54:24.475-00:00",
;;               :started #inst "2015-12-13T13:54:24.371-00:00"},
;;     :timestamp #inst "2015-12-13T13:54:24.371-00:00"}

"By default, the value of `:timestamp` is same as `:started`. However, we can also pass in the exact `:timestamp` we want the function to have as it's argument."

(-> @(current {:timestamp "2000-01-01T00:00:00-00:00"})
    :timestamp)
;; => "2000-01-01T00:00:00-00:00"

[[:file {:src "test/documentation/hara_concurrent_procedure/retry.clj"}]]
