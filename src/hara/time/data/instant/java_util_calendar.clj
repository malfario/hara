(ns hara.time.data.instant.java-util-calendar
  (:require [hara.protocol.time :as time])
  (:require [hara.time.data
             [common :as common]
             [coerce :as coerce]])
  (:import [java.util Date Calendar TimeZone]
           java.text.SimpleDateFormat))

(extend-type Calendar
  time/IInstant
  (-to-long       [t] (.getTime (.getTime t)))
  
  time/IRepresentation
  (-millisecond  [t _] (.get t Calendar/MILLISECOND))
  (-second       [t _] (.get t Calendar/SECOND))
  (-minute       [t _] (.get t Calendar/MINUTE))
  (-hour         [t _] (.get t Calendar/HOUR_OF_DAY))
  (-day          [t _] (.get t Calendar/DAY_OF_MONTH))
  (-day-of-week  [t _] (rem (dec (.get t Calendar/DAY_OF_WEEK)) 7))
  (-month        [t _] (inc (.get t Calendar/MONTH)))
  (-year         [t _] (.get t Calendar/YEAR)))

(defmethod time/-from-long Calendar
  [ ^Long long {:keys [timezone] :as opts}]
  (common/calendar (Date. long)
                 (coerce/coerce-zone timezone {:type TimeZone})))

(defmethod time/-now Calendar
  [{:keys [timezone]}]
  (common/calendar (Date.)
                 (coerce/coerce-zone timezone {:type TimeZone})))

(defn from-map [{:keys [millisecond second minute hour day month year timezone]}]
  (let [cal (doto (Calendar/getInstance ^TimeZone
                                        (coerce/coerce-zone timezone {:type TimeZone}))
              (.set year (dec month) day hour minute second))
        _   (if (or (nil? millisecond) (zero? millisecond))
              (.set cal Calendar/MILLISECOND 0)
              (.set cal Calendar/MILLISECOND millisecond))]
    cal))

(defmethod time/-time-meta Calendar
  [_]
  {:base :instant
   :formatter {:type SimpleDateFormat}
   :parser    {:type SimpleDateFormat}
   :rep {:from  {:fn from-map}
         :to    {:fn {:millisecond time/-millisecond
                      :second      time/-second
                      :minute      time/-minute
                      :hour        time/-hour
                      :day         time/-day
                      :day-of-week time/-day-of-week
                      :month       time/-month
                      :year        time/-year
                      :timezone    (fn [^Calendar t opts] (.getTimeZone t))}}}})
