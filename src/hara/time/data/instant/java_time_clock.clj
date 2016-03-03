(ns hara.time.data.instant.java-time-clock
  (:require [hara.protocol.time :as time])
  (:require [hara.time.data.coerce :as coerce]
            [hara.time.data.instant.java-time-zoneddatetime])
  (:import [java.time Clock Instant ZoneId ZonedDateTime]))

(defmethod time/-time-meta Clock
  [_]
  {:type :instant
   :rep  {:from {:proxy ZonedDateTime
                 :via (fn [^ZonedDateTime t]
                        (Clock/fixed (.toInstant t)
                                     (.getZone t)))}
          :to   {:proxy ZonedDateTime
                 :via (fn [^Clock t opts]
                        (ZonedDateTime/now t))}}})

(extend-type Clock
  time/IInstant
  (-to-long       [t] (.millis t)))

(defmethod time/-from-long Clock
  [ ^Long long {:keys [timezone]}]
  (Clock/fixed (Instant/ofEpochMilli long)
               (coerce/coerce-zone timezone {:type ZoneId})))

(defmethod time/-now Clock
  [{:keys [timezone]}]
  (Clock/fixed (Instant/now)
               (coerce/coerce-zone timezone {:type ZoneId})))
