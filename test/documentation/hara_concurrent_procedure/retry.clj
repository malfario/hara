(ns documentation.hara-concurrent-procedure.retry
  (:require [hara.concurrent.procedure :refer :all]
            [hara.concurrent.procedure.registry :as registry]))

[[:chapter {:title "Retries"}]]

"In the real world, there is always going to be failure and having the ability to deal with failure is key to system robustness. Retries are a very important feature for any concurrent process as it allows for strategies to be implemented on failure.

When a function throws an exception, we wish to then be able to adjust and calmly start again with either the same call or another strategy for mitigation. Although this can be done using `try`/`catch` blocks, `hara.concurrent.procedure` offers an elegant alternative."

[[:section {:title "Simple"}]]

"We can construct the most basic retry"

(defprocedure retry-println
  {:retry {:on Throwable
           :limit  3
           :wait   1000}}
  []
  (println "Started Function")
  (throw (Exception. "Hello")))

"Lets run the function and see what happens:"

(comment
  @(retry-println)
  ;; Started Function
  ;; <1000 ms pause>
  ;; Started Function
  ;; <1000 ms pause>
  ;; Started Function
  ;; <1000 ms pause>
  ;; Started Function
  ;; Exception Hello 
  )

"Notice that the function starts, errors and then retries 3 times, pausing 1000ms before each retry. Once it reaches the limit of 3, the function gives up and throws the error."

[[:section {:title "Wait"}]]

"An important variable to control is the wait time. We construct the next example to explain this feature:"

(defprocedure restart-wait
  {:retry {:on Throwable
           :limit  3
           :wait   (fn [state count]
                     (let [t (* count 1000)]
                       (println "STATE:" state "WAITING" t "ms"  )
                       t))}}
  []
  (println "Started Function")
  (throw (Exception. "Hello")))

"Instead of using a fixed value for the wait time, it is controllable by passing a function taking two parameters: `state` (which is used to hold data about the current status of the retries) and `count` (the number of retries attempted). The output of this function is shown below:"

(comment
  @(restart-wait)
  ;; Started Function
  ;; STATE: nil WAITING 0 ms
  ;; Started Function
  ;; STATE: nil WAITING 1000 ms
  ;; Started Function
  ;; STATE: nil WAITING 2000 ms
  ;; Started Function
  ;; Exception Hello 
  )

[[:section {:title "Arguments"}]]

"The difference between `hara.concurrent.procedure` and other libraries for concurrent constructs lie in the fact that the modes of operation such as caching, timing, threads and other runtime decisions can be passed to the function itself. This also applies to retry strategies as well.  can be passed in as arguments to affect how a function is evaluated. We see an example of a procedure:"

(defprocedure restart-args [:instance]
  []
  (println "Started Function")
  (throw (Exception. "Hello")))

"When it is called directly, the output is predictable:"

(comment
  @(restart-args)
  ;; Started Function
  ;; Exception Hello
  )

"When it is called with retry options, there is a very different output:"

(comment
  @(restart-args {:retry {:on Throwable
                          :limit  3
                          :wait   (fn [state count]
                                    (let [t (* count 1000)]
                                      (println "WAITING" t "ms")
                                      t))}})
  ;; Started Function
  ;; WAITING 0 ms
  ;; Started Function
  ;; WAITING 1000 ms
  ;; Started Function
  ;; WAITING 2000 ms
  ;; Started Function
  ;; Exception Hello
  )

"The advantage of this approach is significant. Most of the time, the modes of execution are hard-coded and so cannot be changed. With parameterised control of execution, how a function is run is now totally in the hands of the caller and so allows much more freedom and flexible for how systems can be defined."

[[:section {:title "Handlers"}]]

"The retry handler mechanism is compatible with `hara.event` and offers the same semanitics for querying on exception data. Notice that multiple handlers can be set up for the procedure."

(defprocedure restart-cats
  {:arglist [:instance]
   :retry {:handlers [{:on    {:cats odd?}
                       :apply (fn [state e]
                                (-> state
                                    (update-in [:types (type e)] (fnil inc 0))
                                    (update-in [:total]
                                               (fnil (fn [out]
                                                       (if (> 0.3 (rand))
                                                         (inc out)
                                                         out))
                                                     0))))
                       :wait  100
                       :limit 100}]}}
  [instance]
  (println (-> instance :retry :count)
           (-> instance :retry :state))
  (if-let [res (-> instance :retry :state :total)]
    (if (< res 5)
      (throw (ex-info "Cats" {:cats 3})))
    (throw (ex-info "Cats" {:cats 3}))))

"We can see the results of the output:"

(comment
  @(restart-cats {})
  ;; nil nil
  ;; 1 {:total 1, :types {clojure.lang.ExceptionInfo 1}}
  ;; 2 {:total 2, :types {clojure.lang.ExceptionInfo 2}}
  ;; 3 {:total 3, :types {clojure.lang.ExceptionInfo 3}}
  ;; 4 {:total 3, :types {clojure.lang.ExceptionInfo 4}}
  ;; 5 {:total 3, :types {clojure.lang.ExceptionInfo 5}}
  ;; 6 {:total 3, :types {clojure.lang.ExceptionInfo 6}}
  ;; 7 {:total 4, :types {clojure.lang.ExceptionInfo 7}}
  ;; 8 {:total 4, :types {clojure.lang.ExceptionInfo 8}}
  ;; 9 {:total 4, :types {clojure.lang.ExceptionInfo 9}}
  ;; 10 {:total 4, :types {clojure.lang.ExceptionInfo 10}}
  ;; 11 {:total 5, :types {clojure.lang.ExceptionInfo 11}}
  )

"Lets talk a little about `state`. In many failure scenarios, there are particular strategies that one needs to apply in order to mitigate a particular situation. `:apply` is a way to manipulate the state based upon the previous state and the exception that occurred. Using a combination of `:apply` and `:wait` will allow most retry strategies to be expressed."

"Some more sample `:handlers` can be seen below. The parameters outside of the `:handlers` vector are default values."
  
{:retry {:handlers [{:on #{Exception}
                     :apply   (fn [state e])
                     :limit   (fn [state count])
                     :wait    (fn [state count])}
                    {:on Error
                     :apply   (fn [state e])
                     :limit   :no}
                    {:on     (fn [e] (instance? Throwable e))
                     :apply  (fn [state e])
                     :limit  :no}]
         :on Throwable
         :count 0
         :state  {:a 1 :b 2}
         :limit 10
         :wait  100}}
