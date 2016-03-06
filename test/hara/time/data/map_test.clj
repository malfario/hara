(ns hara.time.data.map-test
  (:use midje.sweet)
  (:require [hara.protocol.time :as time]
            [hara.time.data
                [common :as common]
                [map :refer :all]]
            [hara.time.data]
            [hara.io.environment :as env])
  (:import [java.util Date Calendar TimeZone]))

^{:refer hara.time.data.map/to-map :added "2.2"}
(fact "converts an instant to a map"
  (to-map 0 {:timezone "GMT"} common/+default-keys+)
  => {:type java.lang.Long, :timezone "GMT", :long 0
      :year 1970, :month 1, :day 1,
      :hour 0, :minute 0 :second 0 :millisecond 0}

  (to-map (Date. 0) {:timezone "EST"}
          [:year :day :month])
  => {:type java.util.Date, :timezone "EST", :long 0
      :year 1969, :day 31, :month 12}

  (to-map {:type java.lang.Long, :timezone "GMT", :long 0
           :year 1970, :month 1, :day 1,
           :hour 0, :minute 0 :second 0 :millisecond 0}
          {:timezone "EST"}
          common/+default-keys+))

^{:refer hara.time.data.map/from-map :added "2.2"}
(fact "converts a map back to an instant type"
  (from-map {:type java.lang.Long
             :year 1970, :month 1, :day 1,
             :hour 0, :minute 0 :second 0 :millisecond 0
             :timezone "GMT"}
            {:timezone "Asia/Kolkata"}
            {})
  => 0
  
  (-> (from-map {:type java.util.Calendar
                 :year 1970, :month 1, :day 1,
                 :hour 0, :minute 0 :second 0 :millisecond 0
                 :timezone "GMT"}
                {:timezone "Asia/Kolkata"}
                {})
      (to-map {} common/+default-keys+))
  => {:type java.util.GregorianCalendar, :timezone "Asia/Kolkata", :long 0
      :year 1970, :month 1, :day 1,
      :hour 5, :minute 30 :second 0 :millisecond 0}

  (to-map (common/calendar (Date. 0)
                           (TimeZone/getTimeZone "EST"))
          {:timezone "GMT"} [:month :day :year])
  => {:type java.util.GregorianCalendar, :timezone "GMT", :long 0,
      :year 1970 :month 1, :day 1})

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

    (to-map (time/-from-long 0 {:type ZonedDateTime})
            {:timezone "GMT"}
            [:month :day :year])
    => {:type java.time.ZonedDateTime, :long 0,
        :timezone "GMT", :year 1970 :month 1, :day 1}

    (to-map (time/-from-long 0 {:type ZonedDateTime})
            {:timezone "PST"}
            [:month :day :year])
    => {:type java.time.ZonedDateTime, :long 0, 
        :timezone "America/Los_Angeles",
        :year 1969, :month 12, :day 31}

    (to-map (time/-from-long 0 {:type Clock :timezone "EST"})
            {:timezone "GMT"}
            [:month :day :year])
    => {:type java.time.Clock$FixedClock, :timezone "GMT", :long 0,
        :year 1970, :month 1, :day 1}))
