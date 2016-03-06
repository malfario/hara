(ns hara.time.data.long
  (:require [hara.protocol.time :as time]
            [hara.time.data
             [common :as common]
             [coerce :as coerce]])
  (:import [java.util Date Calendar TimeZone]))

(def long-meta
  {:base :instant
   :map  {:from {:proxy Calendar
                 :via (fn [^Calendar cal]
                       (.getTime (.getTime cal)))}
          :to   {:proxy Calendar
                 :via (fn [^Long t {:keys [timezone]}]
                        (common/calendar (Date. t)
                                       (coerce/coerce-zone timezone {:type TimeZone})))}}})

(defmethod time/-time-meta Long
  [_]
  long-meta)

(extend-type Long  
  time/IInstant
  (-to-long       [t] t)
  (-has-timezone? [t] false)
  (-get-timezone  [t] nil)
  (-with-timezone [t _] t)

  time/IDuration
  (-to-length     [d _] d))

(defmethod time/-from-long Long
  [long _]
  long)

(defmethod time/-now Long
  [_]
  (.getTime (Date.)))

(defmethod time/-from-length Long
  [long _]
  long)
