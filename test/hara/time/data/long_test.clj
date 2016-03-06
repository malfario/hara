(ns hara.time.data.long-test
  (:use midje.sweet)
  (:require [hara.protocol
             [string :as string]
             [time :as time]]
            [hara.time.data]
            [hara.time.data
             [coerce :as coerce]
             [common :as common]
             [map :as map]]
            [hara.io.environment :as env])
  (:import [java.util Date Calendar TimeZone]
           [java.sql Timestamp]))

(fact "time/-now for java.util datastructures"

  (time/-now {:type Date})
  => #(instance? Date %)

  (time/-now {:type Long})
  => #(instance? Long %)

  (time/-now {:type Calendar})
  => #(instance? Calendar %)

  (time/-now {:type Timestamp})
  => #(instance? Timestamp %)

  (keys (time/-now {:type clojure.lang.PersistentArrayMap}))
  => (just [:day :hour :timezone :long :second :month :type :year :millisecond :minute] :in-any-order))

(fact "time/-from-long for java.util datastructures"

  (time/-from-long 0 {:type Date})
  => #inst "1970-01-01T00:00:00.000-00:00"

  (time/-from-long 0 {:type Date :timezone "Asia/Kolkata"})
  => #inst "1970-01-01T00:00:00.000-00:00"
  
  (time/-from-long 0 {:type Long})
  => 0

  (.getTime ^Calendar (time/-from-long 0 {:type Calendar :timezone "GMT"}))
  => #inst "1970-01-01T00:00:00.000-00:00"

  (-> ^Calendar (time/-from-long 0 {:type Calendar :timezone "Asia/Kolkata"})
      (.getTimeZone)
      (string/-to-string))
  => "Asia/Kolkata" 

  (time/-from-long 0 {:type Timestamp})
  #inst "1970-01-01T00:00:00.000000000-00:00"

  (time/-from-long 0 {:type clojure.lang.PersistentArrayMap :timezone "GMT"})
  => {:type clojure.lang.PersistentArrayMap, :timezone "GMT", :long 0,
      :year 1970, :month 1, :day 1, :hour 0, :minute 0, :second 0, :millisecond 0}
  
  (time/-from-long 0 {:type clojure.lang.PersistentArrayMap :timezone "Asia/Kolkata"})
  => {:type clojure.lang.PersistentArrayMap, :timezone "Asia/Kolkata", :long 0,
      :year 1970, :month 1, :day 1, :hour 5, :minute 30, :second 0, :millisecond 0})

(fact "time/-to-long for java.util datastructures"

  (time/-to-long 0)
  => 0

  (time/-to-long (Date. 0))
  => 0

  (time/-to-long (common/calendar (Date. 0)
                                  (TimeZone/getTimeZone "GMT")))
  => 0

  (time/-to-long (common/calendar (Date. 0)
                                  (TimeZone/getTimeZone "Asia/Kolkata")))
  => 0

  (time/-to-long {:type clojure.lang.PersistentArrayMap, :timezone "GMT", :long 0,
                  :year 1970, :month 1, :day 1, :hour 0, :minute 0, :second 0, :millisecond 0})
  => 0

  (time/-to-long {:type clojure.lang.PersistentArrayMap, :timezone "Asia/Kolkata", :long 0,
                  :year 1970, :month 1, :day 1, :hour 5, :minute 30, :second 0, :millisecond 0})
  => 0)

(env/init
 {:java {:major 1 :minor 8}}
 (:import [java.time Instant Clock ZonedDateTime])
 (:require [hara.time.data.instant
            java-time-instant
            java-time-clock
            java-time-zoneddatetime]
           [hara.time.data.zone
            java-time-zoneid]))

(env/run
  {:java {:major 1 :minor 8}}

  (fact "time/-now for java.time datastructures"
    (time/-now {:type Instant})
    => #(instance? Instant %)
    
    (time/-now {:type Clock})
    => #(instance? Clock %)

    (time/-now {:type ZonedDateTime})
    => #(instance? ZonedDateTime %))


  (fact "time/-from-long for java.util datastructures"

    (-> (time/-from-long 0 {:type Instant})
        (time/-to-long))
    => 0

    (-> (time/-from-long 0 {:type Instant})
        (map/to-map {:timezone "GMT"} common/+default-keys+))
    => {:type java.time.Instant
        :timezone "GMT", :long 0, 
        :year 1970, :month 1, :day 1, :hour 0,
        :minute 0, :second 0 :millisecond 0}

    (-> (time/-from-long 0 {:type Clock :timezone "GMT"})
        (map/to-map {:timezone "Asia/Kolkata"} common/+default-keys+))
    
    => {:type java.time.Clock$FixedClock
        :timezone "Asia/Kolkata", :long 0, 
        :year 1970, :month 1, :day 1, :hour 5,
        :minute 30, :second 0 :millisecond 0}
    
    (-> (time/-from-long 0 {:type Clock :timezone "Asia/Kolkata"})
        (time/-get-timezone))
    => "Asia/Kolkata"
    
    (-> (time/-from-long 0 {:type Clock :timezone "Asia/Kolkata"})
        (map/to-map {} common/+default-keys+))
    => {:type java.time.Clock$FixedClock,
        :timezone "Asia/Kolkata", :long 0, 
        :year 1970, :month 1, :day 1, :hour 5,
        :minute 30, :second 0 :millisecond 0}
    
    (-> (time/-from-long 0 {:type ZonedDateTime :timezone "Asia/Kolkata"})
        (map/to-map {} common/+default-keys+))
    => {:type java.time.ZonedDateTime
        :timezone "Asia/Kolkata", :long 0, 
        :year 1970, :month 1, :day 1, :hour 5,
        :minute 30, :second 0 :millisecond 0}))
