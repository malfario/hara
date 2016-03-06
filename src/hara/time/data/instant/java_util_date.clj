(ns hara.time.data.instant.java-util-date
  (:require [hara.protocol.time :as time]
            [hara.time.data
             [common :as common]
             [coerce :as coerce]])
  (:import [java.util Date Calendar TimeZone]
           java.text.SimpleDateFormat))

(def date-meta
  {:base :instant
   :formatter {:type SimpleDateFormat}
   :parser    {:type SimpleDateFormat}
   :map  {:from {:proxy Calendar
                 :via (fn [^Calendar cal]
                        (.getTime cal))}
          :to   {:proxy Calendar
                 :via (fn [^Date t {:keys [timezone]}]
                        (common/calendar t
                                         (coerce/coerce-zone timezone {:type TimeZone})))}}})

(defmethod time/-time-meta Date
  [_]
  date-meta)

(extend-type Date
  time/IInstant
  (-to-long       [t] (.getTime t))
  (-has-timezone? [t] false)
  (-get-timezone  [t] nil)
  (-with-timezone [t _] t))

(defmethod time/-from-long Date 
  [^Long long _]
  (Date. long))

(defmethod time/-now Date 
  [_]
  (Date.))
