(ns hara.time.data.map-test
  (:use midje.sweet)
  (:require [hara.time.data.map :refer :all]
            [hara.time.data])
  (:import java.util.Date))

^{:refer hara.time.data.map/to-map :added "2.2"}
(fact "converts an instant to a map"
  (to-map 0 {:timezone "GMT"})
  => {:type java.lang.Long, :timezone "GMT", 
      :year 1970, :month 1, :day 1,  :day-of-week 5,
      :hour 0, :minute 0 :second 0 :millisecond 0}

  (to-map (Date. 0) {:timezone "EST"
                     :include-timezone? false
                     :include-type? false}
          [:year :day :month])
  => {:year 1969, :day 31, :month 12})

^{:refer hara.time.data.map/from-map :added "2.2"}
(fact "converts a map back to an instant type"
  (from-map {:type java.lang.Long
             :year 1970, :month 1, :day 1,
             :hour 0, :minute 0 :second 0 :millisecond 0
             :timezone "GMT"})
  => 0)
