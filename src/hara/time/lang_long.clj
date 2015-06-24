(ns hara.time.lang-long
  (:require [hara.time.util-calendar :as calendar]
            [hara.protocol.time :as time]
            [hara.time.common :as common])
  (:import [java.util Date TimeZone]))

(extend-type Long
  
  time/IInstant
  (-to-long      [t]
    t)
  (-milli        [t tz]
    (time/-milli  (calendar/calendar (Date. t) tz) tz))
  (-second       [t tz]
    (time/-second (calendar/calendar (Date. t) tz) tz))
  (-minute       [t tz]
    (time/-minute (calendar/calendar (Date. t) tz) tz))
  (-hour         [t tz]
    (time/-minute (calendar/calendar (Date. t) tz) tz))
  (-day          [t tz]
    (time/-day    (calendar/calendar (Date. t) tz) tz))
  (-day-of-week  [t tz]
    (time/-day-of-week (calendar/calendar (Date. t) tz) tz))
  (-month        [t tz]
    (time/-month  (calendar/calendar (Date. t) tz) tz))
  (-year         [t tz]
    (time/-year   (calendar/calendar (Date. t) tz) tz)))

(defmethod common/timezone? Long
  [_] false)

(defmethod common/from-long Long
  [type long]
  long)
