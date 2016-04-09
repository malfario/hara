(ns documentation.hara-time
  (:use midje.sweet)
  (:require [hara.time :as t]
            [hara.time.data
             [common :as common]
             [map :as map]])
  (:import [java.util Date TimeZone Calendar]))

[[:chapter {:title "Introduction"}]]

"
[hara.time](https://github.com/zcaudate/hara/blob/master/src/hara/time.clj) is a unified framework for representating time on the jvm."

[[:section {:title "Installation"}]]

"
Add to `project.clj` dependencies:

    [im.chit/hara.time \"{{PROJECT.version}}\"]

All functionality is contained in the `hara.time` namespace.
"

(comment
  (require '[hara.time :as t]))


[[:section {:title "Motivation"}]]

"
`hara.time` provides a compact interface for dealing with the different representation of time available on the jvm. The library sticks to the following principles of how an interface around dates should be exposed:

- it should be consistent, so that there may be a common language between all time implementions.
- it should be extensible, so that new implemention can be added easily
- it should be simple and clear, to have easy to use functions and for interfactions between time objects to be seamless

Currently there are a couple of implementions for time on the JVM:

- the < jdk1.8 options for time: `java.util.Date`, `java.util.Calendar`, `java.sql.Timestamp`
- the < jdk1.8 defacto standard: the [joda-time](http://www.joda.org/joda-time/) package
- the new jdk1.8 `java.time` library

Clojure libraries for time are:

- [clj-time](https://github.com/clj-time/clj-time) wraps joda and is the standard for dealing with time in clojure
- [clojure.joda-time](https://github.com/dm3/clojure.joda-time) is another wrapper around joda time
- [clojure.java-time](https://github.com/dm3/clojure.java-time) is a wrapper around the jdk1.8 `java.time` package. 
- [duckling](https://github.com/wit-ai/duckling) is a super amazing library for temporal expressions

Ignoring [duckling](https://github.com/wit-ai/duckling), which is about ten years ahead of it's time, the other three implementations faithfully wrap the underlying time library. While there are advantages in such an apprach, design decisions at the object level may impact the usability at the wrapper level.

`hara.time` comes at the problem from a slightly different angle. A core set of operations and representations of time is abstracted out of each implementation, allowing for many different implementions of time to speak the same language. The library may not be as feature complete as the rest, but provides a generic and very extensible framework for time manipulation.

An example of extensiblity can be seen with [hara.time.joda](https://github.com/zcaudate/hara.time.joda), an add-on package for `hara.time` for [joda-time](http://www.joda.org/joda-time/) compatibility.
"

[[:chapter {:title "Walkthrough"}]]

[[:section {:title "Representation"}]]

"We can start off with the easiest call:"

(t/now)
;;=> {:day 4, :hour 14, :timezone "Asia/Kolkata",
;;    :long 1457081866919, :second 46, :month 3,
;;    :type java.util.Date, :year 2016, :millisecond 919, :minute 27}

"Note that `now` returns a clojure map representing the current time. This is the default type, but we can also specify that we want a `java.util.Date` object"

(t/now {:type java.util.Date})
;;=> #inst "2016-03-04T08:57:46.919-00:00"

"If the jvm is Java 1.8, the use of `:type` can set the returned object to be of type `java.time.Instant`."

(t/now {:type java.time.Instant})
;;=> #<Instant 2016-03-04T08:58:11.678Z>

"The default type can be accessed through `default-type`:"

(t/default-type)
;;=> clojure.lang.PersistentArrayMap

"The default timezone can also be accessed and modified through `default-timezone`"

(t/default-timezone)
;;=> "Asia/Kolkata"

[[:section {:title "Supported Types"}]]

"The default type can be changed by passing in another parameter, currently `hara.time` supports the following options:

- `java.lang.Long`    
- `java.util.Date`
- `java.util.Calendar`
- `java.sql.Timestamp`
- `java.time.Instant`
- `java.time.Clock`
- `java.time.ZonedDateTime`
"

"Changing the default-type to Calendar will immediately affect the `now` function to return a `java.util.Calendar` object"

(t/default-type java.util.Calendar)

(t/now)
;;=> #inst "2016-03-04T14:28:39.481+05:30"

(type (t/now))
;;=> java.util.GregorianCalendar

"And again, a change of type will result in another representation"

(t/default-type java.time.ZonedDateTime)

(t/now)
;;=> #<ZonedDateTime 2016-03-04T15:41:17.901+05:30[Asia/Kolkata]>

(type (t/now))
;;=> java.time.ZonedDateTime

[[:section {:title "Date as Data"}]]

"`hara.time` has two basic concepts of time:

- time as an absolute value (long)
- time as a representation in a given context (map)"

"These concepts can also be set as the default type, for example, we now set `Long` as the default type:"

(t/default-type Long)

(t/now)
;;=> 1457086323250

"As well as a map as the default type:"

(t/default-type clojure.lang.PersistentArrayMap)

(t/now)
;;=> {:day 4, :hour 14, :timezone "Asia/Kolkata",
;;    :second 0, :day-of-week 6, :month 3,
;;    :year 2016, :millisecond 611, :minute 33}

"A specific timezone can be passed in and this is the same for all supported time objects:"

(t/now {:timezone "GMT"})
;;=> {:day 4, :hour 9, :timezone "GMT",
;;    :second 13, :day-of-week 6, :month 3,
;;    :year 2016, :millisecond 585, :minute 4}

[[:section {:title "Accessors"}]]

[[:subsection {:title "year"}]]

"accesses the year representated by the instant"

(fact 
  (t/year 0 {:timezone "GMT"}) => 1970

  (t/year (Date. 0) {:timezone "EST"}) => 1969)

[[:subsection {:title "month"}]]

"accesses the month representated by the instant"

(fact 
  (t/month 0 {:timezone "GMT"}) => 1
  ^:hidden
  (t/month (Date. 0) {:timezone "EST"}) => 12)

[[:subsection {:title "month"}]]

"accesses the day representated by the instant"

(fact 
  (t/day 0 {:timezone "GMT"}) => 1

  (t/day (Date. 0) {:timezone "EST"}) => 31)

[[:subsection {:title "day"}]]

"accesses the day of week representated by the instant"

(fact 
  (t/day-of-week 0 {:timezone "GMT"}) => 4

  (t/day-of-week (Date. 0) {:timezone "EST"}) => 3)

[[:subsection {:title "hour"}]]

"accesses the hour representated by the instant"

(fact 
  (t/hour 0 {:timezone "GMT"}) => 0

  (t/hour (Date. 0) {:timezone "Asia/Kolkata"}) => 5)

[[:subsection {:title "minute"}]]

"accesses the minute representated by the instant"

(fact 
  (t/minute 0 {:timezone "GMT"}) => 0

  (t/minute (Date. 0) {:timezone "Asia/Kolkata"}) => 30)

[[:subsection {:title "second"}]]

"accesses the second representated by the instant"

(fact 
  (t/second 1000 {:timezone "GMT"}) => 1)

[[:subsection {:title "millisecond"}]]

"accesses the millisecond representated by the instant"

(fact 
  (t/millisecond 1010 {:timezone "GMT"}) => 10)


[[:section {:title "Coercion"}]]

"The map representation and the long representation provide the two most basic forms of time"

(fact
  (t/to-map 0 {:timezone "GMT"})
  => {:type java.lang.Long,
      :timezone "GMT", :long 0
      :year 1970, :month 1, :day 1, 
      :hour 0, :minute 0, :second 0, :millisecond 0})

"`from-map`, `to-map`, `from-long` and `to-long` can be used to convert datetime to the representation of data and back"

(fact
  (-> (t/from-long 0 {:type java.util.Calendar})
      (t/to-map {:timezone "GMT"}))
  => {:type java.util.GregorianCalendar
      :timezone "GMT", :long 0
      :year 1970, :month 1, :day 1,
      :hour 0, :minute 0, :second 0, :millisecond 0})

"Here are a couple more examples of the flexibilty of these methods:"

(fact
  (t/from-map {:timezone "GMT", 
               :year 1970, :month 1, :day 1, :day-of-week 4,
               :hour 0, :minute 0, :second 0, :millisecond 0}
              {:type java.util.Date})
  => #inst "1970-01-01T00:00:00.000-00:00")

"Most time objects can be created using `from-long`"

(t/from-long 0 {:type java.time.Instant})
;;=> #<Instant 1970-01-01T00:00:00Z>

[[:section {:title "Format"}]]

"Dates can be formatted as follows:"

(fact
  (-> (t/from-long 0 {:type java.util.Date})
      (t/format "yyyy MM dd HH mm ss"
                {:timezone "GMT"}))
  => "1970 01 01 00 00 00")

"All types will follow the same interface:"

(fact
  (-> (t/from-long 0 {:type java.time.Instant})
      (t/format "yyyy MM dd HH mm ss"
                {:timezone "GMT"}))
  => "1970 01 01 00 00 00")

[[:section {:title "Parsing"}]]

"The opposite of formatting is parsing:"

(fact
  (-> "1970 01 01 00 00 00 +0000"
      (t/parse "yyyy MM dd HH mm ss Z"
               {:type java.util.Date}))
  => #inst "1970-01-01T00:00:00.000-00:00")

"All types will follow the same interface:"

(fact
  (-> "1970 01 01 00 00 00 +0000"
      (t/parse "yyyy MM dd HH mm ss Z"
               {:type java.time.ZonedDateTime})
      (t/to-map {}))
  => {:type java.time.ZonedDateTime
      :timezone "Etc/GMT", :long 0
      :year 1970, :month 1, :day 1,
      :hour 0, :minute 0, :second 0, :millisecond 0})

[[:section {:title "Addition and Subtraction"}]]

"Dates can be added and substracted using long values"

(fact
  (t/plus (java.util.Date. 0) 1000)
  => #inst "1970-01-01T00:00:01.000-00:00")

"Dates can be added and substracted using as well as map values:"

(fact
  (t/plus (java.util.Date. 0) {:weeks 4})
  => #inst "1970-01-29T00:00:00.000-00:00")

"Following the convention of adding an `s` at the end of each field, we can manipulate dates very easily"

(fact
  (t/plus (java.util.Date. 0) {:years 10 :months 1 :weeks 4 :days 2})
  => #inst "1980-03-02T00:00:00.000-00:00")

"As well as convert from a map and back again"

(fact
  (-> (t/from-map {:type java.time.ZonedDateTime
                   :timezone "GMT", 
                   :year 1970, :month 1, :day 1, 
                   :hour 0, :minute 0, :second 0, :millisecond 0})
      (t/minus    {:years 10 :months 1 :weeks 4 :days 2})
      (t/to-map {:timezone "GMT"}))
  => {:type java.time.ZonedDateTime, :timezone "GMT",
      :long -320803200000
      :year 1959, :month 11, :day 2, 
      :hour 0, :minute 0, :second 0, :millisecond 0})


[[:section {:title "Timezone"}]]

"There are additional methods for dealing with timezone:"

[[:subsection {:title "has-timezone?"}]]

"checks if the instance contains a timezone"

(fact 
  (t/has-timezone? 0) => false

  (t/has-timezone? (common/calendar (Date. 0)
                                    (TimeZone/getDefault)))
  => true)

[[:subsection {:title "get-timezone?"}]]

"returns the contained timezone if exists"

(fact 
  (t/get-timezone 0) => nil

  (t/get-timezone (common/calendar (Date. 0)
                                   (TimeZone/getTimeZone "EST")))
  => "EST")

[[:subsection {:title "with-timezone?"}]]

"returns the same instance in a different timezone"

(fact 
  (t/with-timezone 0 "EST") => 0
  
  (t/to-map (t/with-timezone (common/calendar (Date. 0)
                                              (TimeZone/getTimeZone "GMT"))
              "EST"))
  => {:type java.util.GregorianCalendar,
      :timezone "EST", :long 0,
      :year 1969, :month 12, :day 31, :hour 19,
      :minute 0, :second 0, :millisecond 0})

[[:section {:title "Comparison"}]]


[[:subsection {:title "equal"}]]

"compares dates, retruns true if all inputs are the same"

(fact 
  (t/equal 1 (Date. 1) (common/calendar (Date. 1) (TimeZone/getTimeZone "GMT")))
  => true)

[[:subsection {:title "before"}]]

"compare dates, returns true if t1 is before t2, etc"

(fact 
  (t/before 0 (Date. 1) (common/calendar (Date. 2) (TimeZone/getTimeZone "GMT")))
  => true)

[[:subsection {:title "after"}]]

"compare dates, returns true if t1 is after t2, etc"

(fact 
  (t/after 2 (Date. 1) (common/calendar (Date. 0) (TimeZone/getTimeZone "GMT")))
  => true)


[[:subsection {:title "lastest"}]]

"returns the latest date out of a range of inputs"

(fact 
  (t/latest (Date. 0) (Date. 1000) (Date. 20000))
  => #inst "1970-01-01T00:00:20.000-00:00")

[[:subsection {:title "earliest"}]]

"returns the earliest date out of a range of inputs"

(fact 
  (t/earliest (Date. 0) (Date. 1000) (Date. 20000))
  => #inst "1970-01-01T00:00:00.000-00:00")


[[:section {:title "Coercion"}]]

"Any of the dates can be coerced to and from each other:"

(fact 
  (t/coerce 0 {:type Date})
  => #inst "1970-01-01T00:00:00.000-00:00"
  
  (t/coerce {:type clojure.lang.PersistentHashMap,
             :timezone "PST", :long 915148800000,
             :year 1999, :month 1, :day 1, :hour 0, :minute 0 :second 0, :millisecond 0}
            {:type Date})
  => #inst "1999-01-01T08:00:00.000-00:00")


[[:section {:title "Adjust and Truncate"}]]

[[:subsection {:title "truncate"}]]

"truncates the time to a particular field"

(fact 
  (t/truncate #inst "1989-12-28T12:34:00.000-00:00"
              :hour {:timezone "GMT"})
  => #inst "1989-12-28T12:00:00.000-00:00"
  
  (t/truncate #inst "1989-12-28T12:34:00.000-00:00"
              :year {:timezone "GMT"})
  => #inst "1989-01-01T00:00:00.000-00:00"

  (t/truncate (t/to-map #inst "1989-12-28T12:34:00.000-00:00" {:timezone "GMT"})
              :hour)
  => {:type clojure.lang.PersistentArrayMap, :timezone "GMT", :long 630849600000,
      :year 1989, :month 12, :day 28,
      :hour 12, :minute 0, :second 0, :millisecond 0})

[[:subsection {:title "adjust"}]]

"adjust fields of a particular time"

(fact 
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

[[:chapter {:title "Extensiblity"}]]

"Because the API is based on protocols, it is very easy to extend. For an example of how other date libraries can be added to the framework, please see [hara.time.joda](https://github.com/zcaudate/hara.time.joda) for how [joda-time](http://www.joda.org/joda-time/) was added."

