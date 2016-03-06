(ns hara.time.data.coerce-test
  (:use midje.sweet)
  (:require [hara.time.data
             [coerce :refer :all]
             [common :as common]
             [long :as long]
             [map :as map]]
            [hara.protocol
             [string :as string]
             [time :as time]]
            [hara.time.data.instant
             java-util-date
             java-util-calendar]
            [hara.time.data.zone
             java-util-timezone]
            [hara.io.environment :as env])
  (:import [java.util Date Calendar TimeZone]))

^{:refer hara.time.data.coerce/coerce-instant :added "2.2"}
(fact "coercion of one instant object to another"
  (-> ^Calendar (coerce-instant 0 {:type Calendar})
      (.getTime)
      (.getTime))
  => 0)

^{:refer hara.time.data.coerce/coerce-zone :added "2.2"}
(fact "coercion of one zone object to another"
  (-> (coerce-zone "Asia/Kolkata" {:type TimeZone})
      (string/-to-string))
  => "Asia/Kolkata"

  (-> (coerce-zone nil {:type TimeZone})
      (string/-to-string))
  => (-> (TimeZone/getDefault)
         (string/-to-string)))

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

  (fact "coerce-instant for java.time datastructures"
    (coerce-instant 0 {:type Long
                       :timezone "GMT"})
    => 0
    
    (-> (coerce-instant 0 {:type ZonedDateTime
                           :timezone "GMT"})
        (map/to-map {} common/+default-keys+))
    => {:type ZonedDateTime
        :timezone "GMT", :long 0, 
        :year 1970, :month 1, :day 1, :hour 0,
        :minute 0, :second 0 :millisecond 0}

    
    (-> (time/-from-long 0 {:type ZonedDateTime
                            :timezone "GMT"})
        (coerce-instant {:type Clock
                         :timezone "Asia/Kolkata"})
        (map/to-map {} common/+default-keys+))
    => {:type java.time.Clock$FixedClock,
        :timezone "Asia/Kolkata", :long 0, 
        :year 1970, :month 1, :day 1, :hour 5,
        :minute 30, :second 0 :millisecond 0}

    (-> (time/-from-long 0 {:type Clock
                            :timezone "GMT"})
        (coerce-instant {:type Calendar
                         :timezone "Asia/Kolkata"})
        (map/to-map {} common/+default-keys+))
    => {:type java.util.GregorianCalendar
        :timezone "Asia/Kolkata", :long 0, 
        :year 1970, :month 1, :day 1, :hour 5,
        :minute 30, :second 0 :millisecond 0}))
