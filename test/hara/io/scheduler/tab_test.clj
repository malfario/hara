(ns hara.io.scheduler.tab-test
    (:use midje.sweet)
    (:require [hara.time :as t]
              [hara.io.scheduler.tab :refer :all]))

(def test-num (range 60))
(def every-5-seconds-0 [(*- 0 60 5) (*-) (*-) (*-) (*-) (*-) (*-)])
(def every-5-seconds-1 [(*- 5) (*-) (*-) (*-) (*-) (*-) (*-)])

^{:refer hara.io.scheduler.tab/*- :added "2.2"}
(fact "takes a string and returns something"
  (*-) => :*

  (map (*- 2) (range 60))
  => (map even? (range 60))

  ^:hidden
  (map (*- 0 10) (range 60))
  => (map (fn [x] (and (>= x 0) (<= x 10))) (range 60)))


^{:refer hara.io.scheduler.tab/to-time-array :added "2.2"}
(fact "takes a time element and returns an array representation"
  
  (to-time-array #inst "1970-01-01T00:00:00.000-00:00" "UTC")
  => [0 0 0 4 1 1 1970]

  (to-time-array #inst "1970-01-01T00:00:00.000-00:00" "GMT-10")
  => [0 0 14 3 31 12 1969])

^{:refer hara.io.scheduler.tab/parse-tab :added "2.2"}
(fact "takes a string and creates matches"

  (parse-tab "* * * * * * *")
  => '[(:*) (:*) (:*) (:*) (:*) (:*) (:*)]

  (parse-tab "* * * * * *")
  => '[(0) (:*) (:*) (:*) (:*) (:*) (:*)]

  (parse-tab "* * * * *")
  => (throws Exception)

  ^:hidden
  (parse-tab "* * * * * * * *") => (throws Exception)
  (parse-tab "1,2 1,5 * * 1 * *") => '[(1 2) (1 5) (:*) (:*) (1) (:*) (:*)]
  (parse-tab "1,2 * * 1 * *") => '[(0) (1 2) (:*) (:*) (1) (:*) (:*)])

^{:refer hara.io.scheduler.tab/match-element? :added "2.2"}
(fact "takes an element of the array and compares with a single matcher"

  (match-element? 1 :*)
  => true
  
  (match-element? 1 [2 3 4])
  => false

  ^:hidden
  (match-element? 1 [:*]) => true
  
  (match-element? 1 [2 3 4 :*]) => true)

^{:refer hara.io.scheduler.tab/match-array? :added "2.2"}
(fact "takes an array representation for match comparison"

  (match-array? [30 14 0 4 26 7 2012]
                [(*- 0 60 5) (*-) (*-) (*-) (*-) (*-) (*-)])
  => true

  (match-array? [31 14 0 4 26 7 2012]
                [(*- 0 60 5) (*-) (*-) (*-) (*-) (*-) (*-)])
  => false)
