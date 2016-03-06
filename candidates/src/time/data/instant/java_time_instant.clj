(ns hara.time.data.instant.java-time-instant
  (:require [hara.protocol.time :as time]
            [hara.time.data.coerce :as coerce])
  (:import [java.time Instant Clock ZoneId ZonedDateTime]
           [java.time.format DateTimeFormatter]))

(defmethod time/-time-meta Instant
  [_]
  {:base :instant
   :formatter {:type DateTimeFormatter}
   :parser    {:type DateTimeFormatter}
   :rep  {:from {:proxy ZonedDateTime
                 :via (fn [^ZonedDateTime t]
                        (.toInstant t))}
          :to   {:proxy ZonedDateTime
                 :via (fn [^Instant t {:keys [timezone]}]
                        (ZonedDateTime/now
                         (Clock/fixed t
                                      (or (coerce/coerce-zone timezone {:type ZoneId})
                                          (ZoneId/systemDefault)))))}}})

(extend-type Instant
  time/IInstant
  (-to-long       [t] (.toEpochMilli t)))

(defmethod time/-from-long Instant
  [^Long long _]
  (Instant/ofEpochMilli long))

(defmethod time/-now Instant
  [_]
  (Instant/now))
