(ns hara.time.data.instant.java-time-zoneddatetime
  (:require [hara.protocol
             [string :as string]
             [time :as time]])
  (:require [hara.time.data
             [coerce :as coerce]]
            [hara.time.data.zone
             java-time-zoneid])
  (:import [java.time Clock ZonedDateTime ZoneId]
           [java.time.format DateTimeFormatter]))

(defn from-map [{:keys [millisecond second minute hour day month year timezone] :as rep}]
  (ZonedDateTime/of year month day hour minute second (* millisecond 1000000)
                    (coerce/coerce-zone timezone {:type ZoneId})))

(def zoneddatetime-meta
  {:base :instant
   :formatter {:type DateTimeFormatter}
   :parser    {:type DateTimeFormatter}
   :map {:from  {:fn from-map}}})

(defmethod time/-time-meta ZonedDateTime
  [_]
  zoneddatetime-meta)

(extend-type ZonedDateTime
  time/IInstant
  (-to-long       [t] (.toEpochMilli (.toInstant t)))
  (-has-timezone? [t] true)
  (-get-timezone  [t] (string/-to-string (.getZone t)))
  (-with-timezone [t tz] (.withZoneSameInstant
                          t
                          ^ZoneId (coerce/coerce-zone tz {:type ZoneId})))
  
  time/IRepresentation
  (-millisecond  [t _] (/ (.getNano t) 1000000))
  (-second       [t _] (.getSecond t))
  (-minute       [t _] (.getMinute t))
  (-hour         [t _] (.getHour t))
  (-day          [t _] (.getDayOfMonth t))
  (-day-of-week  [t _] (.getValue (.getDayOfWeek t)))
  (-month        [t _] (.getValue (.getMonth t)))
  (-year         [t _] (.getYear t)))

(defmethod time/-from-long ZonedDateTime
  [ ^Long long opts]
  (ZonedDateTime/now ^Clock (time/-from-long long (assoc opts :type Clock))))

(defmethod time/-now ZonedDateTime
  [opts]
  (ZonedDateTime/now ^Clock (time/-now (assoc opts :type Clock))))
