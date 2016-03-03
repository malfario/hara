(ns hara.time.data.vector
  (:use midje.sweet)
  (:require [hara.time.data.vector :refer :all]
            [hara.time.data])
  (:import java.util.Date))

^{:refer hara.time.data.vector/to-vector :added "2.2"}
(fact "converts an instant to an array representation"
  (to-vector 0 {:timezone "GMT"})
  => [1970 1 1 0 0 0 0]

  (to-vector (Date. 0) {:timezone "GMT"} :day)
  => [1970 1 1]

  (to-vector (Date. 0) {:timezone "GMT"} [:month :day :year])
  => [1 1 1970])
