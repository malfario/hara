(ns hara.time.data.zone.java-time-zoneid
  (:require [hara.protocol.time :as time]
            [hara.protocol.string :as string])
  (:import [java.time ZoneId]))

(defmethod time/-time-meta ZoneId
  [_]
  {:base :zone})

(defmethod time/-timezone ZoneId
  [^String tz _]
  (ZoneId/of tz))

(extend-type ZoneId
  string/IString
  (-to-string [tz]
    (.getId tz))
  (-to-string-meta [tz]
    {:type ZoneId}))

(defmethod string/-from-string ZoneId
  [^String string _]
  (ZoneId/of string))
