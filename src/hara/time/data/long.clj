(ns hara.time.data.long
  (:require [hara.protocol.time :as time]
            [hara.time.data
             [common :as common]
             [coerce :as coerce]])
  (:import [java.util Date Calendar TimeZone]))

(defmethod time/-time-meta Long
  [_]
  {:base :instant
   :rep  {:from {:proxy Calendar
                 :via (fn [^Calendar cal]
                       (.getTime (.getTime cal)))}
          :to   {:proxy Calendar
                 :via (fn [^Long t {:keys [timezone]}]
                        (common/calendar (Date. t)
                                       (coerce/coerce-zone timezone {:type TimeZone})))}}})

(extend-type Long  
  time/IInstant
  (-to-long       [long] long)

  time/IDuration
  (-to-length     [long _] long))

(defmethod time/-from-long Long
  [long _]
  long)

(defmethod time/-now Long
  [_]
  (.getTime (Date.)))

(defmethod time/-from-length Long
  [long _]
  long)
