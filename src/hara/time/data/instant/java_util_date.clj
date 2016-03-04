(ns hara.time.data.instant.java-util-date
  (:require [hara.protocol.time :as time]
            [hara.time.data
             [common :as common]
             [coerce :as coerce]])
  (:import [java.util Date Calendar TimeZone]
           java.text.SimpleDateFormat))

(defmethod time/-time-meta Date
  [_]
  {:base :instant
   :formatter {:type SimpleDateFormat}
   :parser    {:type SimpleDateFormat}
   :rep  {:from {:proxy Calendar
                 :via (fn [^Calendar cal]
                        (.getTime cal))}
          :to   {:proxy Calendar
                 :via (fn [^Date t {:keys [timezone]}]
                        (common/calendar t
                                       (coerce/coerce-zone timezone {:type TimeZone})))}}})

(extend-type Date
  time/IInstant
  (-to-long       [t] (.getTime t)))

(defmethod time/-from-long Date 
  [^Long long _]
  (Date. long))

(defmethod time/-now Date 
  [_]
  (Date.))
