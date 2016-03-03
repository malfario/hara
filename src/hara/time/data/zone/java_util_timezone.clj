(ns hara.time.data.zone.java-util-timezone
  (:require [hara.protocol.time :as time]
            [hara.protocol.string :as string])
  (:import [java.util TimeZone]))

(defmethod time/-time-meta TimeZone
  [_]
  {:base :zone})

(defmethod time/-timezone TimeZone
  [^String tz _]
  (TimeZone/getTimeZone tz))

(extend-type TimeZone
  string/IString
  (-to-string [tz]
    (.getID tz))
  (-to-string-meta [tz]
    {:type TimeZone}))

(defmethod string/-from-string TimeZone
  [^String string _]
  (TimeZone/getTimeZone string))
