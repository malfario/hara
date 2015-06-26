(ns hara.time.util-date
  (:require [hara.protocol.time :as time]
            [hara.time.common :as common]
            [hara.time.util-calendar :as calendar])
  (:import [java.util Date Calendar TimeZone]))

(extend-type Date

  time/IInstant  
  (-to-long      [t]
    (.getTime t))
  (-timezone?    [t]
    false)
  (-milli        [t ^String tz]
    (time/-milli  (calendar/calendar t (TimeZone/getTimeZone tz)) tz))
  (-second       [t ^String tz]
    (time/-second (calendar/calendar t (TimeZone/getTimeZone tz)) tz))
  (-minute       [t ^String tz]
    (time/-minute (calendar/calendar t (TimeZone/getTimeZone tz)) tz))
  (-hour         [t ^String tz]
    (time/-hour (calendar/calendar t (TimeZone/getTimeZone tz)) tz))
  (-day          [t ^String tz]
    (time/-day    (calendar/calendar t (TimeZone/getTimeZone tz)) tz))
  (-day-of-week  [t ^String tz]
    (time/-day-of-week (calendar/calendar t (TimeZone/getTimeZone tz)) tz))
  (-month        [t ^String tz]
    (time/-month  (calendar/calendar t (TimeZone/getTimeZone tz)) tz))
  (-year         [t ^String tz]
    (time/-year   (calendar/calendar t (TimeZone/getTimeZone tz)) tz)))

(defmethod common/timezone? Date
  [_] false)

(defmethod common/from-long Date
  [_ ^long long tz]
  (Date. long))

(defmethod common/from-map Date
  [{:keys [^long value] :as m}]
  (if value
    (Date. value)))

(defmethod common/truncate Date
  [^Date t field tz]
  (if (= :milli field)
    (let [val (.getTime t)]
      (Date. (* (quot val 1000) 1000)))
    (throw (Exception. (str "truncate for " field " not implemented")))))

(comment

  (common/from-long Date 0 nil)
  
  (./refresh))
