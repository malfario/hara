(ns hara.time.data.instant.java-time-zoneddatetime
  (:require [hara.protocol.time :as time])
  (:require [hara.time.data.coerce :as coerce])
  (:import [java.time Clock ZonedDateTime ZoneId]
           [java.time.format DateTimeFormatter]))

(extend-type ZonedDateTime
  time/IInstant
  (-to-long       [t] (.toEpochMilli (.toInstant t)))
  
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

(defn from-map [{:keys [millisecond second minute hour day month year timezone] :as rep}]
  (ZonedDateTime/of year month day hour minute second (* millisecond 1000000)
                    (coerce/coerce-zone timezone {:type ZoneId})))

(defmethod time/-time-meta ZonedDateTime
  [_]
  {:base :instant
   :formatter {:type DateTimeFormatter}
   :parser    {:type DateTimeFormatter}
   :rep {:from  {:fn from-map}
         :to    {:fn {:millisecond time/-millisecond
                      :second      time/-second
                      :minute      time/-minute
                      :hour        time/-hour
                      :day         time/-day
                      :day-of-week time/-day-of-week
                      :month       time/-month
                      :year        time/-year
                      :timezone    (fn [^ZonedDateTime t opts] (.getZone t))}}}})
