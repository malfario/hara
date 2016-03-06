(ns hara.time.data.map
  (:require [hara.protocol
             [string :as string]
             [time :as time]]
            [hara.time.data
             [common :as common]])
  (:import [java.util Date TimeZone Calendar]
           [clojure.lang PersistentArrayMap PersistentHashMap]
           [java.text SimpleDateFormat]))

(defn to-map
  "converts an instant to a map
   (to-map 0 {:timezone \"GMT\"} common/+default-keys+)
   => {:type java.lang.Long, :timezone \"GMT\", :long 0
       :year 1970, :month 1, :day 1,
       :hour 0, :minute 0 :second 0 :millisecond 0}
 
   (to-map (Date. 0) {:timezone \"EST\"}
           [:year :day :month])
   => {:type java.util.Date, :timezone \"EST\", :long 0
       :year 1969, :day 31, :month 12}
 
   (to-map {:type java.lang.Long, :timezone \"GMT\", :long 0
            :year 1970, :month 1, :day 1,
            :hour 0, :minute 0 :second 0 :millisecond 0}
           {:timezone \"EST\"}
           common/+default-keys+)"
  {:added "2.2"}
  [t {:keys [timezone] :as opts} ks]
  (let [tmeta (time/-time-meta (class t))
        [p pmeta] (let [{:keys [proxy via]} (-> tmeta :map :to)]
                    (if (and proxy via)
                      [(via t opts) (time/-time-meta proxy)]
                      [t tmeta]))
        p         (if timezone
                    (time/-with-timezone p timezone)
                    p)
        fns       (select-keys common/+default-fns+ ks)
        output    (reduce-kv (fn [out k t-fn]
                               (assoc out k (t-fn p opts)))
                             {}
                             fns)]
    (-> output
        (assoc :timezone (time/-get-timezone p)
               :type     (class t)
               :long     (time/-to-long t)))))

(defn from-map
  "converts a map back to an instant type
   (from-map {:type java.lang.Long
              :year 1970, :month 1, :day 1,
              :hour 0, :minute 0 :second 0 :millisecond 0
              :timezone \"GMT\"}
             {:timezone \"Asia/Kolkata\"}
             {})
   => 0
   
   (-> (from-map {:type java.util.Calendar
                  :year 1970, :month 1, :day 1,
                  :hour 0, :minute 0 :second 0 :millisecond 0
                  :timezone \"GMT\"}
                 {:timezone \"Asia/Kolkata\"}
                 {})
       (to-map {} common/+default-keys+))
   => {:type java.util.GregorianCalendar, :timezone \"Asia/Kolkata\", :long 0
       :year 1970, :month 1, :day 1,
       :hour 5, :minute 30 :second 0 :millisecond 0}
 
   (to-map (common/calendar (Date. 0)
                            (TimeZone/getTimeZone \"EST\"))
           {:timezone \"GMT\"} [:month :day :year])
   => {:type java.util.GregorianCalendar, :timezone \"GMT\", :long 0,
       :year 1970 :month 1, :day 1}"
  {:added "2.2"}
  [m opts fill]
  (let [m    (merge fill m)
        type (or (:type opts)
                 (:type m))
        {:keys [proxy via] :as tmeta} (get-in (time/-time-meta type)
                                              [:map :from])
        output (cond proxy
                     (-> (assoc m :type proxy)
                         (from-map {} {})
                         (via))
                     
                     :else
                     ((:fn tmeta) m))]
    (if-let [tz (:timezone opts)]
      (time/-with-timezone output tz)
      output)))

(defn with-timezone [{:keys [long timezone] :as m} tz]
  (cond (= timezone tz)
        m

        long
        (time/-from-long long {:type PersistentArrayMap 
                               :timezone tz})

        :else
        (-> m
            (from-map {:type Calendar :timezone tz} {})
            (to-map {} common/+default-keys+))))

(def arraymap-meta
  {:base :instant
   :formatter {:type SimpleDateFormat}
   :parser    {:type SimpleDateFormat}
   :map       {:from {:fn (fn [m] (assoc m :type PersistentArrayMap))}}})

(defmethod time/-time-meta PersistentArrayMap
  [_]
  arraymap-meta)

(extend-type PersistentArrayMap
  time/IInstant
  (-to-long [{:keys [long] :as m}]
    (from-map m {:type Long} {}))
  (-has-timezone? [m] (not (nil? (:timezone m))))
  (-get-timezone  [m] (:timezone m))
  (-with-timezone [m tz]
    (assoc (with-timezone m tz) :type PersistentArrayMap)) 

  time/IRepresentation
  (-millisecond  [t _] (:millisecond t))
  (-second       [t _] (:second t))
  (-minute       [t _] (:minute t))
  (-hour         [t _] (:hour t))
  (-day          [t _] (:day t))
  (-day-of-week  [t _] (:day-of-week t))
  (-month        [t _] (:month t))
  (-year         [t _] (:year t)))

(defmethod time/-from-long PersistentArrayMap 
  [long opts]
  (-> (to-map long opts common/+default-keys+)
      (assoc :type PersistentArrayMap)))

(defmethod time/-now PersistentArrayMap
  [opts]
  (-> (to-map (time/-now (assoc opts :type Calendar))
              opts
              (or (:keys opts)
                  common/+default-keys+))
      (assoc :type PersistentArrayMap)))

(def hashmap-meta
  {:base :instant
   :formatter {:type SimpleDateFormat}
   :parser    {:type SimpleDateFormat}
   :map       {:from {:fn (fn [m] (assoc m :type PersistentHashMap))}}})

(defmethod time/-time-meta PersistentHashMap
  [_]
  hashmap-meta)

(defmethod time/-from-long PersistentHashMap 
  [long opts]
  (-> (to-map long opts common/+default-keys+)
      (assoc :type PersistentHashMap)))

(extend-type PersistentHashMap
  time/IInstant
  (-to-long [{:keys [long] :as m}]
    (from-map m {:type Long} {}))
  (-has-timezone? [m] (not (nil? (:timezone m))))
  (-get-timezone  [m] (:timezone m))
  (-with-timezone [m tz] (assoc (with-timezone m tz) :type PersistentHashMap)) 
  
  time/IRepresentation
  (-millisecond  [t _] (:millisecond t))
  (-second       [t _] (:second t))
  (-minute       [t _] (:minute t))
  (-hour         [t _] (:hour t))
  (-day          [t _] (:day t))
  (-day-of-week  [t _] (:day-of-week t))
  (-month        [t _] (:month t))
  (-year         [t _] (:year t)))

(defmethod time/-now PersistentHashMap
  [opts]
  (-> (to-map (time/-now (assoc opts :type Calendar))
              opts
              (or (:keys opts)
                  common/+default-keys+))
      (assoc :type PersistentHashMap)))
