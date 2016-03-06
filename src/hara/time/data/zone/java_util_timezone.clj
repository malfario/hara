(ns hara.time.data.zone.java-util-timezone
  (:require [hara.protocol
             [string :as string]
             [time :as time]])
  (:import [java.util TimeZone]))

(defmethod time/-time-meta TimeZone
  [_]
  {:base :zone})

(extend-type TimeZone
  string/IString
  (-to-string [tz]
    (.getID tz))
  (-to-string-meta [tz]
    {:type TimeZone}))

(defmethod string/-from-string TimeZone
  [^String string _]
  (TimeZone/getTimeZone string))
