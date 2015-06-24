(ns hara.time
  (:require [hara.protocol.time :as time]
            [hara.time
             [common :as common]
             lang-long
             util-date
             util-calendar])
  (:import [java.util TimeZone])
  (:refer-clojure :exclude [second]))

(def ^:dynamic *default-type* java.util.Date)

(defn to-long [t]
  (time/-to-long t))

(defn from-long
  ([l]
   (common/from-long *default-type* l nil))
  ([type l]
   (common/from-long type l nil))
  ([type l tz]
   (common/from-long type l tz)))

(defn equal? [t1 t2]
  (= (time/-to-long t1) (time/-to-long t2)))

(defn before? [t1 t2]
  (< (time/-to-long t1) (time/-to-long t2)))

(defn after? [t1 t2]
  (> (time/-to-long t1) (time/-to-long t2)))

(defn system-timezone []
  (.getID (TimeZone/getDefault)))

(defn to-map
  ([t] (to-map t (if (common/timezone? t)
                   (time/-timezone t)
                   (system-timezone))))
  ([t tz]
   (hash-map :value    (time/-to-long t)
             :milli    (time/-milli t tz) 
             :second   (time/-second t tz) 
             :minute   (time/-minute t tz)
             :hour     (time/-hour t tz)
             :day      (time/-day t tz)
             :month    (time/-month t tz)
             :year     (time/-year t tz)
             :timezone tz)))

(defn milli
  ([t] (milli t (system-timezone)))
  ([t tz] (time/-milli t tz)))

(defn second
  ([t] (second t (system-timezone)))
  ([t tz] (time/-second t tz)))

(defn minute
  ([t] (minute t (system-timezone)))
  ([t tz] (time/-minute t tz)))

(defn hour
  ([t] (hour t (system-timezone)))
  ([t tz] (time/-hour t tz)))

(defn day
  ([t] (day t (system-timezone)))
  ([t tz] (time/-day t tz)))

(defn day-of-week
  ([t] (day-of-week t (system-timezone)))
  ([t tz] (time/-day-of-week t tz)))

(defn month
  ([t] (month t (system-timezone)))
  ([t tz] (time/-month t tz)))

(defn year
  ([t] (year t (system-timezone)))
  ([t tz] (time/-year t tz)))

(defn now
  ([] (now *default-type* (system-timezone)))
  ([type tz] (common/from-long type (System/currentTimeMillis) tz)))

(comment

  (now)

  
  )
