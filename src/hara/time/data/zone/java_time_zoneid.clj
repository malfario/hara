(ns hara.time.data.zone.java-time-zoneid
  (:require [hara.protocol
             [string :as string]
             [time :as time]])
  (:import [java.time ZoneId]))

(defmethod time/-time-meta ZoneId
  [_]
  {:base :zone})

(extend-type ZoneId
  string/IString
  (-to-string [tz]
    (.getId tz))
  (-to-string-meta [tz]
    {:type ZoneId}))

(defmethod string/-from-string ZoneId
  [^String string _]
  (ZoneId/of string ZoneId/SHORT_IDS))

