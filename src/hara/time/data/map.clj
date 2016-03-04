(ns hara.time.data.map
  (:require [hara.protocol
             [string :as string]
             [time :as time]]
            [hara.time.data
             [common :as common]])
  (:import [java.util Date TimeZone Calendar]
           [clojure.lang PersistentArrayMap PersistentHashMap]))

(defn include-timezone [rep t tz-fn opts]
  (let [timezone (or (:timezone opts)
                     (and tz-fn
                          (tz-fn t opts))
                     (TimeZone/getDefault))
        timezone (if (string? timezone)
                   timezone
                   (string/-to-string timezone))]
    (assoc rep :timezone timezone)))

(defn to-map
  "converts an instant to a map
   (to-map 0 {:timezone \"GMT\"})
   => {:type java.lang.Long, :timezone \"GMT\", 
       :year 1970, :month 1, :day 1,  :day-of-week 5,
       :hour 0, :minute 0 :second 0 :millisecond 0}
 
   (to-map (Date. 0) {:timezone \"EST\"
                      :include-timezone? false
                      :include-type? false}
           [:year :day :month])
   => {:year 1969, :day 31, :month 12}"
  {:added "2.2"}
  ([t]
   (to-map t nil))
  ([t opts]
   (to-map t opts :all))
  ([t opts ks]
   (cond (map? t)
         (-> (merge t opts)
             (assoc :type PersistentArrayMap))

         :else
         (let [tmeta (-> (class t)
                         (time/-time-meta)
                         :rep
                         :to)
               [p pmeta] (common/to-proxy t opts tmeta) 
               fns  (if (= ks :all)
                      (dissoc (:fn pmeta) :timezone)
                      (select-keys (:fn pmeta) ks))
               rep  (reduce-kv (fn [out k t-fn]
                                 (assoc out k (t-fn p opts)))
                               {}
                               fns)
               rep (if (false? (:include-timezone? opts))
                     rep
                     (include-timezone rep p (-> tmeta :fn :timezone) opts))
               rep (if (false? (:include-type? opts))
                     rep
                     (assoc rep :type (class t)))]
           rep))))

(defn from-map
  "converts a map back to an instant type
   (from-map {:type java.lang.Long
              :year 1970, :month 1, :day 1,
              :hour 0, :minute 0 :second 0 :millisecond 0
              :timezone \"GMT\"})
   => 0"
  {:added "2.2"}
  ([m]
   (from-map m nil nil))
  ([m opts]
   (from-map m opts nil))
  ([m opts fill]
   (let [{:keys [type] :as m} (merge fill m opts)
         {:keys [proxy via] :as fmeta} (get-in (time/-time-meta type)
                                               [:rep :from])]
     (if proxy
       (via (from-map (assoc m :type proxy) nil))
       ((:fn fmeta) m)))))

(defmethod time/-from-long PersistentArrayMap 
  [long opts]
  (-> (to-map long opts)
      (dissoc :type)))

(extend-type PersistentArrayMap
  time/IInstant
  (-to-long [m]
    (from-map {:type Long})))

(defmethod time/-now PersistentArrayMap
  [opts]
  (dissoc (to-map (time/-now (assoc opts :type Calendar))
                  opts)
          :type))

(defmethod time/-time-meta PersistentArrayMap
  [_]
  {:base :instant
   :rep  {:from {:fn identity}}})

(defmethod time/-from-long PersistentHashMap 
  [long opts]
  (-> (to-map long opts)
      (dissoc :type)))

(extend-type PersistentHashMap
  time/IInstant
  (-to-long [m]
    (from-map {:type Long})))

(defmethod time/-now PersistentHashMap
  [opts]
  (dissoc (to-map (time/-now (assoc opts :type Calendar))
                  opts)
          :type))

(defmethod time/-time-meta PersistentHashMap
  [_]
  {:base :instant
   :rep  {:from {:fn identity}}})

