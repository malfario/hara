(ns hara.time.data.vector-test
  (:use midje.sweet)
  (:require [hara.protocol.time :as time]
            [hara.time.data
             [common :as common]
             [map :as map]
             [vector :refer :all]]
            [hara.time.data]
            [hara.io.environment :as env])
  (:import [java.util Date TimeZone]))

^{:refer hara.time.data.vector/to-vector :added "2.2"}
(fact "converts an instant to an array representation"
  (to-vector 0 {:timezone "GMT"} :all)
  => [1970 1 1 0 0 0 0]

  (to-vector (Date. 0) {:timezone "GMT"} :day)
  => [1970 1 1]

  (to-vector (common/calendar (Date. 0)
                              (TimeZone/getTimeZone "EST"))
             {}
             [:month :day :year])
  => [12 31 1969]

  (to-vector (common/calendar (Date. 0)
                              (TimeZone/getTimeZone "EST"))
             {:timezone "GMT"}
             [:month :day :year])
  => [1 1 1970])


(env/init
 {:java {:major 1 :minor 8}}
 (:import [java.time Instant Clock ZonedDateTime ZoneId])
 (:require [hara.time.data.instant
            java-time-instant
            java-time-clock
            java-time-zoneddatetime]
           [hara.time.data.zone
            java-time-zoneid]))

(env/run
  {:java {:major 1 :minor 8}}

  (fact "testing for v1.8 data structures"

    (to-vector (time/-from-long 0 {:type ZonedDateTime})
               {:timezone "GMT"} [:month :day :year])
    => [1 1 1970]
    
    (to-vector (time/-from-long 0 {:type ZonedDateTime})
               {:timezone "PST"}
               [:month :day :year])
    => [12 31 1969]

    (to-vector (time/-from-long 0 {:type Clock :timezone "EST"})
               {:timezone "GMT"}
               [:month :day :year])
    => [1 1 1970]

    (to-vector (time/-from-long 0 {:type Instant})
               {:timezone "PST"}
               [:month :day :year])
    => [12 31 1969]))

