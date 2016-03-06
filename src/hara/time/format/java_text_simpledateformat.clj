(ns hara.time.format.java-text-simpledateformat
  (:require [hara.protocol.time :as time]
            [hara.time.data.coerce :as coerce]
            [hara.time.data.map :as map])
  (:import java.text.SimpleDateFormat
           java.sql.Timestamp
           [java.util Date Calendar TimeZone]
           [clojure.lang PersistentArrayMap PersistentHashMap]))

(defmethod time/-formatter SimpleDateFormat
  [s {:keys [timezone] :as opts}]
  (let [fmt (SimpleDateFormat. s)
        _ (if timezone
            (.setTimeZone fmt (coerce/coerce-zone timezone
                                                  {:type TimeZone})))]
    fmt))

(defmethod time/-format [SimpleDateFormat Date]
  [^SimpleDateFormat formatter ^Date t {:keys [timezone]}]
  (if timezone
    (.setTimeZone formatter (coerce/coerce-zone timezone
                                                {:type TimeZone})))
  (.format formatter t))

(defmethod time/-format [SimpleDateFormat Timestamp]
  [^SimpleDateFormat formatter ^Timestamp t {:keys [timezone]}]
  (if timezone
    (.setTimeZone formatter (coerce/coerce-zone timezone
                                                {:type TimeZone})))
  (.format formatter t))

(defmethod time/-format [SimpleDateFormat Calendar]
  [^SimpleDateFormat formatter ^Calendar t {:keys [timezone]}]
  (let [timezone (or timezone
                     (.getTimeZone t))
        _ (.setTimeZone formatter (coerce/coerce-zone timezone
                                                      {:type TimeZone}))
        t (.getTime t)]
    (.format formatter t)))

(defmethod time/-format [SimpleDateFormat PersistentArrayMap]
  [^SimpleDateFormat formatter ^PersistentHashMap m {:keys [timezone] :as opts}]
  (time/-format formatter (map/from-map m {:type Calendar}) opts))

(defmethod time/-format [SimpleDateFormat PersistentHashMap]
  [^SimpleDateFormat formatter ^PersistentHashMap m {:keys [timezone] :as opts}]
  (time/-format formatter (map/from-map m {:type Calendar}) opts))

(defmethod time/-parser SimpleDateFormat
  [s {:keys [timezone] :as opts}]
  (SimpleDateFormat. s))

(defmethod time/-parse [SimpleDateFormat Date]
  [^SimpleDateFormat parser s opts]
  (.parse parser s))

(defmethod time/-parse [SimpleDateFormat Calendar]
  [^SimpleDateFormat parser s opts]
  (let [_ (.parse parser s)]
    (.getCalendar parser)))

(defmethod time/-parse [SimpleDateFormat Timestamp]
  [^SimpleDateFormat parser s opts]
  (let [_ (.parse parser s)]
    (-> (.getCalendar parser)
        (.getTime)
        (.getTime)
        (Timestamp.))))

(defmethod time/-parse [SimpleDateFormat PersistentArrayMap]
  [^SimpleDateFormat parser s opts]
  (-> (time/-parse parser s (assoc opts :type Calendar))
      (map/to-map opts)))

(defmethod time/-parse [SimpleDateFormat PersistentHashMap]
  [^SimpleDateFormat parser s opts]
  (-> (time/-parse parser s (assoc opts :type Calendar))
      (map/to-map opts)))
