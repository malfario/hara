(ns hara.io.scheduler.clock-test
  (:use midje.sweet)
  (:require [hara.io.scheduler.clock :refer :all]
            [hara.io.scheduler :as scheduler]))

^{:refer hara.io.scheduler.clock/clock :added "2.2"}
(fact "creates an instance of a clock"
  (clock (:clock scheduler/*defaults*))
  ;;=> #clock {:start-time nil,
  ;;           :current-time nil,
  ;;           :running false}
  )

^{:refer hara.io.scheduler.clock/clock-loop :added "2.2"}
(fact "updates the clock tick, if `recur?` is true, then it keeps running"
  (-> (:clock scheduler/*defaults*)
      (clock)
      (clock-loop false))
  ;;=> #clock {:start-time nil,
  ;;           :current-time #inst "2015-07-16T15:24:42.608-00:00",
  ;;           :running false}
  )

^{:refer hara.io.scheduler.clock/clock-stopped? :added "2.2"}
(fact "checks if the clock has stopped")

^{:refer hara.io.scheduler.clock/clock-started? :added "2.2"}
(fact "checks if the clock has started")

^{:refer hara.io.scheduler.clock/clock-start :added "2.2"}
(fact "starts the clock")

^{:refer hara.io.scheduler.clock/clock-stop :added "2.2"}
(fact "stops the clock")
