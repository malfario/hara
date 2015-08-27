(ns hara.concurrent.procedure.retry_test
  (:use midje.sweet)
  (:require [hara.concurrent.procedure.retry :refer :all]))

^{:refer hara.concurrent.procedure.retry/retry-wait :added "2.2"}
(fact "waits in milliseconds depending upon the handler"

  (retry-wait {}) => 0

  (retry-wait {:wait 100}) => 100

  (retry-wait {:wait (fn [state count]
                       (if (> (:expiry state) count)
                         1000
                         0))
               :state {:expiry 4}
               :count 5})
  => 0)

^{:refer hara.concurrent.procedure.retry/retry-pick :added "2.2"}
(fact "chooses the exception handler based upon the exception"
  (retry-pick {:on #{Exception}} (Exception.))
  => true

  (retry-pick {:on Exception} (Exception.))
  => true

  (retry-pick {:on {:a #(= :error %)}} (ex-info "error" {:a :error}))
  => true)

^{:refer hara.concurrent.procedure.retry/retry-args :added "2.2"}
(fact "injects new args based on the instance arglist"
  (retry-args [1 2 {}]
              [:a :b :retry]
              {:wait 5}
              {})
  => '(1 2 {:wait 5}))

^{:refer hara.concurrent.procedure.retry/retry-check :added "2.2"}
(fact "checks to see if a retry is needed"

  (retry-check {:limit 2 :count 3})
  => false

  (retry-check {:limit (fn [state count]
                         (> (/ (inc count) (:restarted state))
                            2))
                :state {:restarted 3}
                :count 8})
  => true)

^{:refer hara.concurrent.procedure.retry/retry-state :added "2.2"}
(fact "calculates the next retry state"

  (retry-state {:apply (fn [state e]
                         (update-in state [:file-errors] (fnil inc 0)))
                :state {}}
               (Exception.))
  => {:file-errors 1})

^{:refer hara.concurrent.procedure.retry/retry :added "2.2"}
(fact "sets up arguments if retrying. if no retry, returns nil"

  (retry {:arglist [:age :gender :retry]
              :retry {:handlers [{:on {:cats odd?}
                                  :apply   (fn [state e]
                                             (update-in state [:types (type e)] (fnil inc 0)))
                                  :wait    (fn [state count]
                                             0)}]
                      :on Throwable
                      :count 0
                      :state  {:a 1 :b 2}
                      :limit 10
                      :wait  100}}
             [:a :b {}]
             (ex-info "hello" {:cats 11}))
  => (contains [:a :b (contains
                       {:handlers
                        (contains
                         [(contains {:on {:cats odd?}, :apply fn?, :wait fn?})])
                        :on java.lang.Throwable,
                        :count 1,
                        :state {:types {clojure.lang.ExceptionInfo 1},
                                :b 2,
                                :a 1},
                        :limit 10,
                        :wait 100})]))


