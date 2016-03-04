(ns documentation.hara-time
  (:use midje.sweet)
  (:require [hara.time :as t]))

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
;;=> #inst "2016-03-04T08:57:46.919-00:00"

"Note that `now` returns a `java.util.Date` object which represents the current time. If the jvm is Java 1.8, the use of `:type` can set the returned object to be of type `java.time.Instant`."

(t/now {:type java.time.Instant})
;;=> #<Instant 2016-03-04T08:58:11.678Z>

"The default type can be accessed through `default-type`:"

(t/default-type)
;;=> java.util.Date

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

"Lets change it back to the default:"

(t/default-type java.util.Date)

[[:section {:title "Coercion"}]]

"The map representation and the long representation provide the two most basic forms of time"

(fact
  (t/to-map 0 {:timezone "GMT"})
  => {:type java.lang.Long,
      :timezone "GMT", 
      :year 1970, :month 1, :day 1, :day-of-week 4,
      :hour 0, :minute 0, :second 0, :millisecond 0})

"`from-map`, `to-map`, `from-long` and `to-long` can be used to convert datetime to the representation of data and back"

(fact
  (-> (t/from-long 0 {:type java.util.Calendar})
      (t/to-map {:timezone "GMT"}))
  => {:type java.util.GregorianCalendar
      :timezone "GMT", 
      :year 1970, :month 1, :day 1, :day-of-week 4,
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
      :timezone "Z", 
      :year 1970, :month 1, :day 1, :day-of-week 4,
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
                   :year 1970, :month 1, :day 1, :day-of-week 4,
                   :hour 0, :minute 0, :second 0, :millisecond 0})
      (t/minus    {:years 10 :months 1 :weeks 4 :days 2})
      (t/to-map {:timezone "GMT"}))
  => {:type java.time.ZonedDateTime, :timezone "GMT",
      :year 1959, :month 11, :day 2, :day-of-week 1, 
      :hour 0, :minute 0, :second 0, :millisecond 0})


[[:chapter {:title "API"}]]

"The entire API for `hara.time` can be seen below:"

[[:api {:namespace "hara.time"}]]
