(ns hara.time.data.common
  (:require [hara.protocol.time :as time])
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

(defn calendar
  "creates a calendar to be used by the base date classes
   (-> ^Calendar (calendar (Date. 0) (TimeZone/getTimeZone \"GMT\"))
       (.getTime))
   => #inst \"1970-01-01T00:00:00.000-00:00\""
  {:added "2.2"}
  [^Date date ^TimeZone timezone]
  (doto (Calendar/getInstance timezone)
    (.setTime date)))

(defn local-timezone
  "returns the current timezone as a string"
  {:added "2.2"}
  []
  (.getID (TimeZone/getDefault)))

(defn to-proxy
  "uses a proxy class for retrieving date representation
   (to-proxy 0 {} (-> (time/-time-meta Long)
                      :rep :to))
   => (contains
       [#(instance? Calendar %)
        (contains {:fn #(every? % [:day :hour :timezone :second
                                   :day-of-week :month :year
                                   :millisecond :minute])})])"
  {:added "2.2"}
  [t opts {:keys [proxy via] :as tmeta}]
  (if proxy
    (to-proxy (via t opts) proxy
              (-> proxy (time/-time-meta) :rep :to))
    [t tmeta]))
