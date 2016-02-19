(ns hara.time.util-calendar
  (:require [hara.protocol.time :as time]
            [hara.time.common :as common])
  (:import [java.util Date Calendar TimeZone]))

(defn calendar
  ([date] (calendar date (TimeZone/getDefault)))
  ([^Date date ^TimeZone tz]
   (doto (Calendar/getInstance tz)
     (.setTime date))))

(extend-type Calendar
  
  time/IInstant
  (-to-long      [t]
    (.getTime (.getTime t)))
  (-milli        [t tz]
    (.get t Calendar/MILLISECOND))
  (-second       [t tz]
    (.get t Calendar/SECOND))
  (-minute       [t tz]
    (.get t Calendar/MINUTE))
  (-hour         [t tz]
    (.get t Calendar/HOUR_OF_DAY))
  (-day          [t tz]
    (.get t Calendar/DAY_OF_MONTH))
  (-day-of-week  [t tz]
    (.get t Calendar/DAY_OF_WEEK))
  (-month        [t tz]
    (inc (.get t Calendar/MONTH)))
  (-year         [t tz]
    (.get t Calendar/YEAR))

  time/IRegion
  (-timezone     [t]
    (-> t (.getTimeZone) (.getID))))

(defmethod common/timezone? Calendar
  [_] true)

(defmethod common/from-long Calendar
  [_ ^long long ^String tz]
  (calendar (Date. long)
            (if tz
              (TimeZone/getTimeZone tz)
              (TimeZone/getDefault))))

(defmethod common/from-map Calendar
  [type {:keys [value timezone] :as m}]
  (if value
    (common/from-long Calendar value timezone)
    (throw (Exception. (str "Missing `:value` key in map: " m)))))

(comment
  
  (common/from-long Calendar 0 nil)
  (time/-timezone (common/from-long Calendar 0 "UTC")))
