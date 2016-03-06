(ns hara.time.data.format-test
  (:use midje.sweet)
  (:require [hara.protocol.time :as time]
            [hara.time.data.format :as f]
            [hara.time.data :as data]
            [hara.time.data
             [common :as common]
             [map :as map]]
            [hara.io.environment :as env])
  (:import [java.util Date Calendar TimeZone]
           java.sql.Timestamp))

^{:refer hara.time.data.format/format :added "2.2"}
(fact "converts a date into a string"
  (f/format (Date. 0) "HH MM dd Z" {:timezone "GMT" :cached true})
  => "00 01 01 +0000"
  
  (f/format (common/calendar (Date. 0)
                             (TimeZone/getTimeZone "GMT"))
            "HH MM dd Z"
            {})
  => "00 01 01 +0000"

  (f/format (Timestamp. 0)
            "HH MM dd Z"
            {:timezone "PST"})
  => "16 12 31 -0800")

^{:refer hara.time.data.format/parse :added "2.2"}
(fact "converts a string into a date"
  (f/parse "00 00 01 01 01 1989 +0000" "ss mm HH dd MM yyyy Z" {:type Date :timezone "GMT"})
  => #inst "1989-01-01T01:00:00.000-00:00"

  (-> (f/parse "00 00 01 01 01 1989 -0800" "ss mm HH dd MM yyyy Z"
               {:type Calendar})
      (map/to-map {:timezone "GMT"} common/+default-keys+))
  {:type java.util.GregorianCalendar, :timezone "GMT", :long 599648400000, 
   :year 1989, :month 1, :day 1, :hour 9, :minute 0, :second 0, :millisecond 0}
 

  (-> (f/parse "00 00 01 01 01 1989 +0000" "ss mm HH dd MM yyyy Z"
               {:type Timestamp})
      (map/to-map {:timezone "Asia/Kolkata"} common/+default-keys+))
  => {:type java.sql.Timestamp, :timezone "Asia/Kolkata", :long 599619600000, 
      :year 1989, :month 1, :day 1, :hour 6,
      :minute 30, :second 0, :millisecond 0})

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

  (fact "testing format for java.time datastructures"
    (-> (f/parse "00 00 01 01 01 1989 +0000" "ss mm HH dd MM yyyy Z"
                 {:type Clock})
        (map/to-map {} common/+default-keys+))
    => {:type java.time.Clock$FixedClock,
        :timezone "Etc/GMT"
        :long 599619600000,
        :year 1989, :month 1, :day 1,
        :hour 1, :minute 0, :second 0, :millisecond 0}
    
    (-> (f/parse "00 00 01 01 01 1989 -1000" "ss mm HH dd MM yyyy Z"
                 {:type Clock})
        (map/to-map {} common/+default-keys+))
    => {:type java.time.Clock$FixedClock, :timezone "Etc/GMT-10", :long 599583600000,
        :year 1989, :month 1, :day 1, :hour 1, :minute 0, :second 0, :millisecond 0}

    (-> (f/parse "00 00 01 01 01 1989 +1000" "ss mm HH dd MM yyyy Z"
                 {:type Clock})
        (map/to-map {} common/+default-keys+))
    => {:type java.time.Clock$FixedClock, :timezone "Etc/GMT+10", :long 599655600000,
        :year 1989, :month 1, :day 1, :hour 1, :minute 0, :second 0, :millisecond 0}))
