(ns hara.time.data.instant.java-sql-timestamp
  (:require [hara.protocol.time :as time]
            [hara.time.data
             [common :as common]
             [coerce :as coerce]])
  (:import [java.sql Timestamp]
           [java.util Calendar Date]))

(defmethod time/-time-meta Timestamp
  [_]
  {:type :instant
   :rep  {:from {:proxy Calendar
                 :via (fn [^Calendar cal]
                        (Timestamp. (.getTime (.getTime cal))))}
          :to   {:type Calendar
                 :via (fn [^Timestamp t {:keys [timezone]}]
                        (common/calendar t timezone))}}})

(extend-type Timestamp
  time/IInstant
  (-to-long       [t] (.getTime t)))

(defmethod time/-from-long Timestamp
  [^Long long _]
  (Timestamp. long))

(defmethod time/-now Timestamp
  [_]
  (Timestamp. (.getTime (Date.))))


