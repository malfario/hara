(ns hara.concurrent.procedure-test
  (:use midje.sweet)
  (:require [hara.concurrent.procedure :refer :all]
            [hara.event :refer :all]))

^{:refer hara.concurrent.procedure/max-inputs :added "2.2"}
(fact "finds the maximum number of inputs that a function can take"
  
  (max-inputs (fn ([a]) ([a b])) 4)
  => 2

  (max-inputs (fn [& more]) 4)
  => 4
  
  (max-inputs (fn ([a])) 0)
  => throws)

^{:refer hara.concurrent.procedure/procedure :added "2.2"}
(fact "creates ")

(comment
  (deflistener print-log :log
    ev
    (println ev))
  
  (def ^:dynamic *blob* (promise))

  (def two-procedure (procedure {:name "two"
                                 :handler (fn [id params]
                                            (Thread/sleep 1000000)
                                            2)
                                 :mode :async}
                                [:id :params]))
  
  (def exec (future (two-procedure (rand) {})))
  *default-registry*
  (def exec (future (two-procedure :three {})))
  
  (all-running)
  {"two" (:two)}

  (kill "two" :two)
  (kill "two" :three)

  *default-registry*
  *default-cache*)

(comment
  (def print-hello
    (procedure {:name    "println"
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

  (swap! *default-cache* empty)
  (kill "println" (java.util.Date. 0))

  (+ 1 1))

*default-registry*

(comment {:procedure        <ref>
          :id          <any>
          :params      <map>
          :mode        <keyword>
          :timestamp   <datetime>
          :result      <promise>
          :registry    <registry>
          :cached      <bool>
          :overwrite   <bool>
          :restart     <bool>})

