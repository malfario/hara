(ns hara.concurrent.task-test
  (:use midje.sweet)
  (:require [hara.concurrent.task :refer :all]))

^{:refer hara.concurrent.task/max-inputs :added "2.2"}
(fact "finds the maximum number of inputs that a function can take"
  
  (max-inputs (fn ([a]) ([a b])) 4)
  => 2

  (max-inputs (fn [& more]) 4)
  => 4
  
  (max-inputs (fn ([a])) 0)
  => throws)

^{:refer hara.concurrent.task/task :added "2.2"}
(fact "creates ")

(comment
  (def ^:dynamic *blob* (promise))

  (def two-task (task {:name "two"
                       :handler (fn [id params]
                                  (Thread/sleep 1000000)
                                  2)
                       :mode :sync
                       :arglist [:id :params]}))

  (def exec (future (two-task :two {})))
  (def exec (future (two-task :three {})))
  
  (all-running)
  {"two" (:two)}

  (kill "two" :two)
  (kill "two" :three)

  *default-registry*
  *default-cache*)

(comment
  (def print-hello
    (task {:name    "println"
           :handler (fn [t params instance]
                      (println "INSTANCE: " instance)
                      (Thread/sleep 500)
                      (println "ENDED" t))
           :id-fn :timestamp
           ;;:cached true
           :params {:b 2}
           ;;:overwrite true
           :mode :sync
           :arglist [:timestamp :params :instance]}))

  (print-hello (java.util.Date. 0) {:a 1 :c 3} {:rabbit "hello"})
  (print-hello (java.util.Date. 1) {:a 1 :c 3} {:rabbit "hello" :mode :async})
  (print-hello (java.util.Date. 2) {:a 1 :c 3} {:rabbit "hello" :mode :async})

  (all-running)

  (kill "println" (java.util.Date. 0))

  (+ 1 1))

*default-registry*

(comment {:task        <ref>
          :id          <any>
          :params      <map>
          :mode        <keyword>
          :timestamp   <datetime>
          :result      <promise>
          :registry    <registry>
          :cached      <bool>
          :overwrite   <bool>
          :restart     <bool>})

