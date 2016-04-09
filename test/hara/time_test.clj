(ns hara.time-test
  (:use midje.sweet)
  (:require [hara.time :as t]
            [hara.time.data
             [common :as common]
             [map :as map]])
  (:import [java.util Date TimeZone Calendar]))

^{:refer hara.time/representation? :added "2.2"}
(fact "checks if an object implements the representation protocol"
  (t/representation? 0) => false

  (t/representation? (common/calendar (Date. 0) (TimeZone/getTimeZone "GMT")))
  => true)

^{:refer hara.time/duration? :added "2.2"}
(fact "checks if an object implements the duration protocol"
  (t/duration? 0) => true

  (t/duration? {:weeks 1})
  => true)

^{:refer hara.time/instant? :added "2.2"}
(fact "checks if an object implements the instant protocol"
  (t/instant? 0) => true

  (t/instant? (Date.)) => true)

^{:refer hara.time/has-timezone? :added "2.2"}
(fact "checks if the instance contains a timezone"
  (t/has-timezone? 0) => false

  (t/has-timezone? (common/calendar (Date. 0)
                                    (TimeZone/getDefault)))
  => true)

^{:refer hara.time/get-timezone :added "2.2"}
(fact "returns the contained timezone if exists"
  (t/get-timezone 0) => nil

  (t/get-timezone (common/calendar (Date. 0)
                                   (TimeZone/getTimeZone "EST")))
  => "EST")

^{:refer hara.time/with-timezone :added "2.2"}
(fact "returns the same instance in a different timezone"
  (t/with-timezone 0 "EST") => 0
  ^:hidden
  (t/to-map (t/with-timezone (common/calendar (Date. 0)
                                              (TimeZone/getTimeZone "GMT"))
              "EST"))
  => {:type java.util.GregorianCalendar,
      :timezone "EST", :long 0,
      :year 1969, :month 12, :day 31, :hour 19,
      :minute 0, :second 0, :millisecond 0})

^{:refer hara.time/time-meta :added "2.2"}
(fact "retrieves the meta-data for the time object"
  (t/time-meta TimeZone)
  => {:base :zone}
  ^:hidden
  (t/time-meta Date)
  => (contains {:base :instant,
                :map (contains {:from (contains {:proxy java.util.Calendar,
                                                 :via fn?}),
                                :to (contains {:proxy java.util.Calendar,
                                               :via fn?})})}))

^{:refer hara.time/to-long :added "2.2"}
(fact "gets the long representation for the instant"
  (t/to-long #inst "1970-01-01T00:00:10.000-00:00")
  => 10000)

^{:refer hara.time/from-long :added "2.2"}
(fact "creates an instant from a long"
  (-> (t/from-long 0 {:timezone "Asia/Kolkata"
                      :type Calendar})
      (t/to-map))
  
  => {:type java.util.GregorianCalendar,
      :timezone "Asia/Kolkata", :long 0
      :year 1970, :month 1, :day 1,
      :hour 5, :minute 30 :second 0, :millisecond 0})

^{:refer hara.time/to-map :added "2.2"}
(fact "creates an map from an instant"
  (-> (t/from-long 0 {:timezone "Asia/Kolkata"
                      :type Date})
      (t/to-map {:timezone "GMT"} [:year :month :day]))
  => {:type java.util.Date, :timezone "GMT", :long 0, 
      :year 1970, :month 1, :day 1})

^{:refer hara.time/from-map :added "2.2"}
(fact "creates an map from an instant"
  (t/from-map {:type java.util.GregorianCalendar,
               :timezone "Asia/Kolkata", :long 0
               :year 1970, :month 1, :day 1,
               :hour 5, :minute 30 :second 0, :millisecond 0}
              
              {:timezone "Asia/Kolkata"
               :type Date})
  => #inst "1970-01-01T00:00:00.000-00:00"
  ^:hidden
  (t/from-map {:type Long,
               :timezone "Asia/Kolkata", :long 0
               :year 1970, :month 1, :day 1,
               :hour 5, :minute 30 :second 0, :millisecond 0})
  => 0
  ^:hidden
  (t/from-map {:type Long
               :timezone "Asia/Kolkata",
               :year 1970, :month 1, :day 1,
               :hour 5, :minute 30 :second 0, :millisecond 0}
              {:type clojure.lang.PersistentHashMap
               :timezone "GMT"})
  => {:type clojure.lang.PersistentHashMap
      :timezone "GMT", :long 0
      :year 1970, :month 1, :day 1,
      :hour 0, :minute 0 :second 0, :millisecond 0})

^{:refer hara.time/to-length :added "2.2"}
(fact "converts a object implementing IDuration to a long"
  (t/to-length {:days 1})
  => 86400000)

^{:refer hara.time/duration :added "2.2"}
(fact "calculates the duration between two intervals")


^{:refer hara.time/year :added "2.2"}
(fact "accesses the year representated by the instant"
  (t/year 0 {:timezone "GMT"}) => 1970

  (t/year (Date. 0) {:timezone "EST"}) => 1969)

^{:refer hara.time/month :added "2.2"}
(fact "accesses the month representated by the instant"
  (t/month 0 {:timezone "GMT"}) => 1
  ^:hidden
  (t/month (Date. 0) {:timezone "EST"}) => 12)

^{:refer hara.time/day :added "2.2"}
(fact "accesses the day representated by the instant"
  (t/day 0 {:timezone "GMT"}) => 1

  (t/day (Date. 0) {:timezone "EST"}) => 31)

^{:refer hara.time/day-of-week :added "2.2"}
(fact "accesses the day of week representated by the instant"
  (t/day-of-week 0 {:timezone "GMT"}) => 4

  (t/day-of-week (Date. 0) {:timezone "EST"}) => 3)

^{:refer hara.time/hour :added "2.2"}
(fact "accesses the hour representated by the instant"
  (t/hour 0 {:timezone "GMT"}) => 0

  (t/hour (Date. 0) {:timezone "Asia/Kolkata"}) => 5)

^{:refer hara.time/minute :added "2.2"}
(fact "accesses the minute representated by the instant"
  (t/minute 0 {:timezone "GMT"}) => 0

  (t/minute (Date. 0) {:timezone "Asia/Kolkata"}) => 30)

^{:refer hara.time/second :added "2.2"}
(fact "accesses the second representated by the instant"
  (t/second 1000 {:timezone "GMT"}) => 1)

^{:refer hara.time/millisecond :added "2.2"}
(fact "accesses the millisecond representated by the instant"
  (t/millisecond 1010 {:timezone "GMT"}) => 10)

^{:refer hara.time/now :added "2.2"}
(fact "returns the current datetime"
  (t/now {:type Date})
  => #(instance? Date %)
  ^:hidden
  (t/now {:type Calendar})
  => #(instance? Calendar %))

^{:refer hara.time/epoch :added "2.2"}
(fact "returns the beginning of unix epoch"
  (t/epoch {:type Date})
  => #inst "1970-01-01T00:00:00.000-00:00"
  ^:hidden
  (t/epoch {:type clojure.lang.PersistentArrayMap :timezone "GMT"})
  {:type clojure.lang.PersistentArrayMap,
   :timezone "GMT", :long 0, 
   :year 1970, :month 1, :day 1, :hour 0, :minute 0, :second 0, :millisecond 0, })

^{:refer hara.time/equal :added "2.2"}
(fact "compares dates, retruns true if all inputs are the same"
  (t/equal 1 (Date. 1) (common/calendar (Date. 1) (TimeZone/getTimeZone "GMT")))
  => true)

^{:refer hara.time/before :added "2.2"}
(fact "compare dates, returns true if t1 is before t2, etc"
  (t/before 0 (Date. 1) (common/calendar (Date. 2) (TimeZone/getTimeZone "GMT")))
  => true)

^{:refer hara.time/after :added "2.2"}
(fact "compare dates, returns true if t1 is after t2, etc"
  (t/after 2 (Date. 1) (common/calendar (Date. 0) (TimeZone/getTimeZone "GMT")))
  => true)

^{:refer hara.time/plus :added "2.2"}
(fact "adds a duration to the time"
  (t/plus (Date. 0) {:weeks 2})
  => #inst "1970-01-15T00:00:00.000-00:00"

  (t/plus (Date. 0) 1000)
  => #inst "1970-01-01T00:00:01.000-00:00")

^{:refer hara.time/minus :added "2.2"}
(fact "substracts a duration from the time"
  (t/minus (Date. 0) {:years 1})
  => #inst "1969-01-01T00:00:00.000-00:00")

^{:refer hara.time/adjust :added "2.2"}
(fact "adjust fields of a particular time"
  (t/adjust (Date. 0) {:year 2000 :second 10} {:timezone "GMT"})
  => #inst "2000-01-01T00:00:10.000-00:00"
  ^:hidden
  (t/adjust {:year 1970, :month 1 :day 1, :day-of-week 4, 
             :hour 0 :minute 0 :second 0 :millisecond 0, 
             :timezone "GMT"}
            {:year 1999})
  => {:type clojure.lang.PersistentHashMap,
      :timezone "GMT", :long 915148800000,
      :year 1999, :month 1, :day 1, :hour 0, :minute 0 :second 0, :millisecond 0})

^{:refer hara.time/coerce :added "2.2"}
(fact "adjust fields of a particular time"
  (t/coerce 0 {:type Date})
  => #inst "1970-01-01T00:00:00.000-00:00"
  
  (t/coerce {:type clojure.lang.PersistentHashMap,
             :timezone "PST", :long 915148800000,
             :year 1999, :month 1, :day 1, :hour 0, :minute 0 :second 0, :millisecond 0}
            {:type Date})
  => #inst "1999-01-01T08:00:00.000-00:00")

^{:refer hara.time/truncate :added "2.2"}
(fact "truncates the time to a particular field"
  (t/truncate #inst "1989-12-28T12:34:00.000-00:00"
              :hour {:timezone "GMT"})
  => #inst "1989-12-28T12:00:00.000-00:00"
  
  (t/truncate #inst "1989-12-28T12:34:00.000-00:00"
              :year {:timezone "GMT"})
  => #inst "1989-01-01T00:00:00.000-00:00"
  ^:hidden
  (t/truncate (t/to-map #inst "1989-12-28T12:34:00.000-00:00" {:timezone "GMT"})
              :hour)
  => {:type clojure.lang.PersistentArrayMap, :timezone "GMT", :long 630849600000,
      :year 1989, :month 12, :day 28,
      :hour 12, :minute 0, :second 0, :millisecond 0})

^{:refer hara.time/latest :added "2.2"}
(fact "returns the latest date out of a range of inputs"
  (t/latest (Date. 0) (Date. 1000) (Date. 20000))
  => #inst "1970-01-01T00:00:20.000-00:00")

^{:refer hara.time/earliest :added "2.2"}
(fact "returns the earliest date out of a range of inputs"
  (t/earliest (Date. 0) (Date. 1000) (Date. 20000))
  => #inst "1970-01-01T00:00:00.000-00:00")
