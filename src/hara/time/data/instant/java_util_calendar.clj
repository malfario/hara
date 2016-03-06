(ns hara.time.data.instant.java-util-calendar
  (:require [hara.protocol
             [string :as string]
             [time :as time]])
  (:require [hara.time.data
             [common :as common]
             [coerce :as coerce]])
  (:import [java.util Date Calendar TimeZone GregorianCalendar]
           java.text.SimpleDateFormat))

(defn from-map [{:keys [millisecond second minute hour day month year timezone]}]
  (let [cal (doto (Calendar/getInstance ^TimeZone
                                        (coerce/coerce-zone timezone {:type TimeZone}))
              (.set year (dec month) day hour minute second))
        _   (if (or (nil? millisecond) (zero? millisecond))
              (.set cal Calendar/MILLISECOND 0)
              (.set cal Calendar/MILLISECOND millisecond))]
    cal))

(def calendar-meta
  {:base :instant
   :formatter {:type SimpleDateFormat}
   :parser    {:type SimpleDateFormat}
   :map       {:from  {:fn from-map}}})

(defn with-timezone [^Calendar t tz]
  (cond (= (time/-get-timezone t)
             (string/-to-string tz))
        t
        
        :else
        (common/calendar (.getTime t)
                         (coerce/coerce-zone tz {:type TimeZone}))))

(defmethod time/-time-meta GregorianCalendar
  [_]
  calendar-meta)

(extend-type GregorianCalendar
  time/IInstant
  (-to-long       [t] (.getTime (.getTime t)))
  (-has-timezone? [t] true)
  (-get-timezone  [t] (string/-to-string (.getTimeZone t)))
  (-with-timezone [t tz] (with-timezone t tz))
 
  time/IRepresentation
  (-millisecond  [t _] (.get t Calendar/MILLISECOND))
  (-second       [t _] (.get t Calendar/SECOND))
  (-minute       [t _] (.get t Calendar/MINUTE))
  (-hour         [t _] (.get t Calendar/HOUR_OF_DAY))
  (-day          [t _] (.get t Calendar/DAY_OF_MONTH))
  (-day-of-week  [t _] (rem (dec (.get t Calendar/DAY_OF_WEEK)) 7))
  (-month        [t _] (inc (.get t Calendar/MONTH)))
  (-year         [t _] (.get t Calendar/YEAR)))

(defmethod time/-time-meta Calendar
  [_]
  calendar-meta)

(extend-type Calendar
  time/IInstant
  (-to-long       [t] (.getTime (.getTime t)))
  (-has-timezone? [t] true)
  (-get-timezone  [t] (string/-to-string (.getTimeZone t)))
  (-with-timezone [t tz] (with-timezone t tz))

  
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
