(ns hara.time.data.instant.java-sql-timestamp
  (:require [hara.protocol.time :as time]
            [hara.time.data
             [common :as common]
             [coerce :as coerce]])
  (:import [java.sql Timestamp]
           [java.util Calendar Date TimeZone]
           [java.text SimpleDateFormat]))

(def timestamp-meta
  {:type :instant
   :formatter {:type SimpleDateFormat}
   :parser    {:type SimpleDateFormat}
   :map   {:from {:proxy Calendar
                  :via (fn [^Calendar cal]
                         (Timestamp. (.getTime (.getTime cal))))}
           :to   {:proxy Calendar
                  :via (fn [^Timestamp t {:keys [timezone]}]
                         (common/calendar t (coerce/coerce-zone timezone {:type TimeZone})))}}})

(defmethod time/-time-meta Timestamp
  [_]
  timestamp-meta)

(extend-type Timestamp
  time/IInstant
  (-to-long       [t] (.getTime t))
  (-has-timezone? [t] false)
  (-get-timezone  [t] nil)
  (-with-timezone [t _] t))

(defmethod time/-from-long Timestamp
  [^Long long _]
  (Timestamp. long))

(defmethod time/-now Timestamp
  [_]
  (Timestamp. (.getTime (Date.))))


