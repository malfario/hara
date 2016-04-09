(ns hara.time
  (:require [hara.protocol
             [time :as time]
             [string :as string]]
            [hara.time.data]
            [hara.time.data
             [common :as common]
             [coerce :as coerce]
             [format :as format]
             [duration :as duration]
             [map :as map]
             [vector :as vector]]
            [hara.class.checks :as class]
            [hara.namespace.import :as ns])
  (:import [java.util Calendar TimeZone]
           [clojure.lang PersistentArrayMap PersistentHashMap])
  (:refer-clojure :exclude [second format]))

(ns/import hara.time.data.common   [local-timezone default-timezone default-type]
           hara.time.data.vector   [to-vector]
           hara.time.data.format   [format parse])

(defn representation?
  "checks if an object implements the representation protocol
   (t/representation? 0) => false
 
   (t/representation? (common/calendar (Date. 0) (TimeZone/getTimeZone \"GMT\")))
   => true"
  {:added "2.2"}
  [obj]
  (satisfies? time/IRepresentation obj))

(defn duration?
  "checks if an object implements the duration protocol
   (t/duration? 0) => true
 
   (t/duration? {:weeks 1})
   => true"
  {:added "2.2"}
  [obj]
  (satisfies? time/IDuration obj))

(defn instant?
  "checks if an object implements the instant protocol
   (t/instant? 0) => true
 
   (t/instant? (Date.)) => true"
  {:added "2.2"}
  [obj]
  (satisfies? time/IInstant obj))

(defn has-timezone?
  "checks if the instance contains a timezone
   (t/has-timezone? 0) => false
 
   (t/has-timezone? (common/calendar (Date. 0)
                                     (TimeZone/getDefault)))
   => true"
  {:added "2.2"}
  [t]
  (time/-has-timezone? t))

(defn get-timezone
  "returns the contained timezone if exists
   (t/get-timezone 0) => nil
 
   (t/get-timezone (common/calendar (Date. 0)
                                    (TimeZone/getTimeZone \"EST\")))
   => \"EST\""
  {:added "2.2"}
  [t]
  (time/-get-timezone t))

(defn with-timezone
  "returns the same instance in a different timezone
   (t/with-timezone 0 \"EST\") => 0
   "
  {:added "2.2"}
  [t tz]
  (time/-with-timezone t tz))

(defn time-meta
  "retrieves the meta-data for the time object
   (t/time-meta TimeZone)
   => {:base :zone}
   "
  {:added "2.2"}
  [cls]
  (time/-time-meta cls))

(defn to-long
  "gets the long representation for the instant
   (t/to-long #inst \"1970-01-01T00:00:10.000-00:00\")
   => 10000"
  {:added "2.2"}
  [t]
  (time/-to-long t))

(defn from-long
  "creates an instant from a long
   (-> (t/from-long 0 {:timezone \"Asia/Kolkata\"
                       :type Calendar})
       (t/to-map))
   
   => {:type java.util.GregorianCalendar,
       :timezone \"Asia/Kolkata\", :long 0
       :year 1970, :month 1, :day 1,
       :hour 5, :minute 30 :second 0, :millisecond 0}"
  {:added "2.2"}
  ([t]
   (from-long t nil))
  ([t opts]
   (time/-from-long t (merge {:type common/*default-type*} opts))))

(defn to-map
  "creates an map from an instant
   (-> (t/from-long 0 {:timezone \"Asia/Kolkata\"
                       :type Date})
       (t/to-map {:timezone \"GMT\"} [:year :month :day]))
   => {:type java.util.Date, :timezone \"GMT\", :long 0, 
       :year 1970, :month 1, :day 1}"
  {:added "2.2"}
  ([t] (to-map t {}))
  ([t opts] (to-map t opts common/+default-keys+))
  ([t {:keys [timezone] :as opts} ks]
   (cond (map? t)
         (if timezone
           (time/-with-timezone t timezone)
           t)
         
         :else
         (map/to-map t opts ks))))

(defn from-map
  "creates an map from an instant
   (t/from-map {:type java.util.GregorianCalendar,
                :timezone \"Asia/Kolkata\", :long 0
                :year 1970, :month 1, :day 1,
                :hour 5, :minute 30 :second 0, :millisecond 0}
               
               {:timezone \"Asia/Kolkata\"
                :type Date})
   => #inst \"1970-01-01T00:00:00.000-00:00\"
   "
  {:added "2.2"}
  ([t] (from-map t {}))
  ([t opts] (from-map t opts common/+zero-values+))
  ([t {:keys [type timezone] :as opts} fill]
   (cond (#{PersistentArrayMap PersistentHashMap} type)
         (time/-with-timezone t timezone)
         
         :else
         (map/from-map t opts fill))))

(defn to-length
  "converts a object implementing IDuration to a long
   (t/to-length {:days 1})
   => 86400000"
  {:added "2.2"}
  ([t]
   (to-length t {:year 0 :month 1 :day 1}))
  ([t rep]
   (time/-to-length t rep)))

(defn wrap-proxy [f]
  (fn [t opts]
    (cond (representation? t)
          (f t opts)

          :else
          (let [tmeta (time/-time-meta (class t))]
            (if-let [p-fn (-> tmeta :map :to :via)]
              (f (p-fn t opts) opts)
              (throw (Exception. (str "No proxy method for type " (class t)))))))))

(defn year
  "accesses the year representated by the instant
   (t/year 0 {:timezone \"GMT\"}) => 1970
 
   (t/year (Date. 0) {:timezone \"EST\"}) => 1969"
  {:added "2.2"}
  ([t] (year t {}))
  ([t opts]
   ((wrap-proxy time/-year) t opts)))

(defn month
  "accesses the month representated by the instant
   (t/month 0 {:timezone \"GMT\"}) => 1
   "
  {:added "2.2"}
  ([t] (month t {}))
  ([t opts]
   ((wrap-proxy time/-month) t opts)))

(defn day
  "accesses the day representated by the instant
   (t/day 0 {:timezone \"GMT\"}) => 1
 
   (t/day (Date. 0) {:timezone \"EST\"}) => 31"
  {:added "2.2"}
  ([t] (day t {}))
  ([t opts]
   ((wrap-proxy time/-day) t opts)))

(defn day-of-week
  "accesses the day of week representated by the instant
   (t/day-of-week 0 {:timezone \"GMT\"}) => 4
 
   (t/day-of-week (Date. 0) {:timezone \"EST\"}) => 3"
  {:added "2.2"}
  ([t] (day-of-week t {}))
  ([t opts]
   ((wrap-proxy time/-day-of-week) t opts)))

(defn hour
  "accesses the hour representated by the instant
   (t/hour 0 {:timezone \"GMT\"}) => 0
 
   (t/hour (Date. 0) {:timezone \"Asia/Kolkata\"}) => 5"
  {:added "2.2"}
  ([t] (hour t {}))
  ([t opts]
   ((wrap-proxy time/-hour) t opts)))

(defn minute
  "accesses the minute representated by the instant
   (t/minute 0 {:timezone \"GMT\"}) => 0
 
   (t/minute (Date. 0) {:timezone \"Asia/Kolkata\"}) => 30"
  {:added "2.2"}
  ([t] (minute t {}))
  ([t opts]
   ((wrap-proxy time/-minute) t opts)))

(defn second
  "accesses the second representated by the instant
   (t/second 1000 {:timezone \"GMT\"}) => 1"
  {:added "2.2"}
  ([t] (second t {}))
  ([t opts]
   ((wrap-proxy time/-second) t opts)))

(defn millisecond
  "accesses the millisecond representated by the instant
   (t/millisecond 1010 {:timezone \"GMT\"}) => 10"
  {:added "2.2"}
  ([t] (millisecond t {}))
  ([t opts]
   ((wrap-proxy time/-millisecond) t opts)))

(defn now
  "returns the current datetime
   (t/now {:type Date})
   => #(instance? Date %)
   "
  {:added "2.2"}
  ([] (now {}))
  ([opts] (time/-now (merge {:type common/*default-type*}
                            opts))))

(defn epoch
  "returns the beginning of unix epoch
   (t/epoch {:type Date})
   => #inst \"1970-01-01T00:00:00.000-00:00\"
   "
  {:added "2.2"}
  ([] (epoch {}))
  ([opts] (from-long 0 (merge {:type common/*default-type*}
                              opts))))

(defn equal
  "compares dates, retruns true if all inputs are the same
   (t/equal 1 (Date. 1) (common/calendar (Date. 1) (TimeZone/getTimeZone \"GMT\")))
   => true"
  {:added "2.2"}
  ([t1 t2]
   (= (to-long t2) (to-long t1)))
  ([t1 t2 & more]
   (apply = (map to-long (cons t1 (cons t2 more))))))

(defn before
  "compare dates, returns true if t1 is before t2, etc
   (t/before 0 (Date. 1) (common/calendar (Date. 2) (TimeZone/getTimeZone \"GMT\")))
   => true"
  {:added "2.2"}
  ([t1 t2]
   (< (to-long t1) (to-long t2)))
  ([t1 t2 & more]
   (apply < (map to-long (cons t1 (cons t2 more))))))

(defn after
  "compare dates, returns true if t1 is after t2, etc
   (t/after 2 (Date. 1) (common/calendar (Date. 0) (TimeZone/getTimeZone \"GMT\")))
   => true"
  {:added "2.2"}
  ([t1 t2]
   (> (to-long t1) (to-long t2)))
  ([t1 t2 & more]
   (apply > (map to-long (cons t1 (cons t2 more))))))

(defn plus
  "adds a duration to the time
   (t/plus (Date. 0) {:weeks 2})
   => #inst \"1970-01-15T00:00:00.000-00:00\"
 
   (t/plus (Date. 0) 1000)
   => #inst \"1970-01-01T00:00:01.000-00:00\""
  {:added "2.2"}
  ([t duration]
   (plus t duration {}))
  ([t duration opts]
   (from-long (+ (to-long t)
                 (to-length duration
                            (to-map t opts [:day :month :year])))
              (assoc opts :type (class t)))))

(defn minus
  "substracts a duration from the time
   (t/minus (Date. 0) {:years 1})
   => #inst \"1969-01-01T00:00:00.000-00:00\""
  {:added "2.2"}
  ([t duration]
   (minus t duration {}))
  ([t duration opts]
   (from-long (- (to-long t)
                 (to-length duration
                            (-> (to-map t opts [:day :month :year])
                                (assoc :backward true))))
              (assoc opts :type (class t)))))


(defn adjust
  "adjust fields of a particular time
   (t/adjust (Date. 0) {:year 2000 :second 10} {:timezone \"GMT\"})
   => #inst \"2000-01-01T00:00:10.000-00:00\"
   "
  {:added "2.2"}
  ([t rep]
   (adjust t rep {}))
  ([t rep opts]
   (let [m (-> (to-map t opts)
               (merge rep)
               (dissoc :long))]
     (from-long (from-map m {:type Long})
                (assoc m :type (class t))))))

(defn truncate
  "truncates the time to a particular field
   (t/truncate #inst \"1989-12-28T12:34:00.000-00:00\"
               :hour {:timezone \"GMT\"})
   => #inst \"1989-12-28T12:00:00.000-00:00\"
   
   (t/truncate #inst \"1989-12-28T12:34:00.000-00:00\"
               :year {:timezone \"GMT\"})
   => #inst \"1989-01-01T00:00:00.000-00:00\"
   "
  {:added "2.2"}
  ([t col]
   (truncate t col {}))
  ([t col opts]
   (let [rep  (to-map t opts)
         trep (->> common/+default-keys+
                   (drop-while #(not= col %))
                   (concat [:type :timezone])
                   (select-keys rep))]
     (from-map (merge common/+zero-values+ (dissoc trep :long))
               (assoc opts :type (class t))))))

(defn coerce
  "adjust fields of a particular time
   (t/coerce 0 {:type Date})
   => #inst \"1970-01-01T00:00:00.000-00:00\"
   
   (t/coerce {:type clojure.lang.PersistentHashMap,
              :timezone \"PST\", :long 915148800000,
              :year 1999, :month 1, :day 1, :hour 0, :minute 0 :second 0, :millisecond 0}
             {:type Date})
   => #inst \"1999-01-01T08:00:00.000-00:00\""
  {:added "2.2"}
  [t {:keys [type timezone] :as opts}]
  (let [type (or type common/*default-type*)
        timezone (or timezone
                     (get-timezone t))]
    (-> (to-long t)
        (from-long {:type type :timezone timezone}))))

(defn latest
  "returns the latest date out of a range of inputs
   (t/latest (Date. 0) (Date. 1000) (Date. 20000))
   => #inst \"1970-01-01T00:00:20.000-00:00\""
  {:added "2.2"}
  [t1 t2 & more]
  (last (sort-by time/-to-long (apply vector t1 t2 more))))

(defn earliest
  "returns the earliest date out of a range of inputs
   (t/earliest (Date. 0) (Date. 1000) (Date. 20000))
   => #inst \"1970-01-01T00:00:00.000-00:00\""
  {:added "2.2"}
  [t1 t2 & more]
  (first (sort-by time/-to-long (apply vector t1 t2 more))))
