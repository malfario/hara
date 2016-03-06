(ns hara.time.data.common
  (:require [hara.protocol
             [string :as string]
             [time :as time]])
  (:import [java.util Calendar Date TimeZone]))

(def ^:dynamic *default-type* clojure.lang.PersistentArrayMap)

(def ^:dynamic *default-timezone* nil)

(def +default-keys+ [:millisecond
                     :second
                     :minute
                     :hour
                     :day
                     :month
                     :year])

(def +zero-values+  {:millisecond 0
                     :second 0
                     :minute 0
                     :hour 0
                     :day 1
                     :month 1})

(def +default-fns+
  {:millisecond #'time/-millisecond
   :second      #'time/-second
   :minute      #'time/-minute
   :hour        #'time/-hour
   :day         #'time/-day
   :day-of-week #'time/-day-of-week
   :month       #'time/-month
   :year        #'time/-year})

(defn calendar
  "creates a calendar to be used by the base date classes
   (-> ^Calendar (calendar (Date. 0) (TimeZone/getTimeZone \"GMT\"))
       (.getTime))
   => #inst \"1970-01-01T00:00:00.000-00:00\""
  {:added "2.2"}
  [^Date date ^TimeZone timezone]
  (doto (Calendar/getInstance timezone)
    (.setTime date)))

(defn default-type
  "accesses the default type for datetime"
  {:added "2.2"}
  ([] *default-type*)
  ([cls]
   (alter-var-root #'*default-type*
                   (constantly cls))))

(defn local-timezone
  "returns the current timezone as a string"
  {:added "2.2"}
  []
  (.getID (TimeZone/getDefault)))

(defn default-timezone
  "accesses the default timezone as a string"
  {:added "2.2"}
  ([]
   (or *default-timezone*
       (local-timezone)))
  ([tz]
   (alter-var-root #'*default-timezone*
                   (constantly (string/-to-string tz)))))
