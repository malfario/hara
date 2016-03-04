(ns hara.time
  (:require [hara.protocol
             [time :as time]
             [string :as string]]
            [hara.time
             [data :as data]
             [format :as format]]
            [hara.time.data
             [common :as common]
             [coerce :as coerce]
             [duration :as duration]
             [interval :as interval]
             [map :as map]
             [vector :as vector]]
            [hara.class.checks :as class]
            [hara.namespace.import :as ns])
  (:import [java.util Calendar TimeZone])
  (:refer-clojure :exclude [second format]))

(ns/import hara.time.data.common   [local-timezone]
           hara.time.data.coerce   [coerce]
           hara.time.data.map      [to-map from-map]
           hara.time.data.vector   [to-vector]
           hara.time.data.interval [interval]
           hara.time.format        [format parse])

(defn default-type
  "accessor to the default type for date creation
   (t/default-type)
   => java.util.Date"
  {:added "2.2"}
  ([] common/*default-type*)
  ([type] (alter-var-root #'common/*default-type*
                          (constantly type))))

(defn default-timezone
  "accessor to the default timezone
   (t/default-timezone)
   => (.getID (TimeZone/getDefault))"
  {:added "2.2"}
  ([] (or common/*default-timezone*
          (.getID (TimeZone/getDefault))))
  ([tz] (alter-var-root #'common/*default-timezone*
                        (fn [_]
                          (string/-to-string tz)))))

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

(defn interval?
  "checks if an object implements the interval protocol"
  {:added "2.2"}
  [obj]
  (satisfies? time/IInterval obj))

(defn time-meta
  "retrieves the meta-data for the time object
   (t/time-meta TimeZone)
   => {:base :zone}
 
   (t/time-meta Date)
   => (contains {:base :instant,
                 :rep (contains {:from (contains {:proxy java.util.Calendar,
                                                  :via fn?}),
                                 :to (contains {:proxy java.util.Calendar,
                                                :via fn?})})})"
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
       :timezone \"Asia/Kolkata\",
       :year 1970, :month 1, :day 1, :day-of-week 5,
       :hour 5, :minute 30 :second 0, :millisecond 0}"
  {:added "2.2"}
  ([t]
   (from-long t nil))
  ([t opts]
   (time/-from-long t (merge {:type common/*default-type*} opts))))

(defn to-length
  "converts a object implementing IDuration to a long
   (t/to-length {:days 1})
   => 86400000"
  {:added "2.2"}
  ([t]
   (to-length t {:year 0 :month 1 :day 1}))
  ([t rep]
   (time/-to-length t rep)))

(defn duration
  "calculates the duration between two intervals"
  {:added "2.2"}
  [interval]
  (time/-duration interval))

(defn wrap-proxy [f]
  (fn [t opts]
    (cond (representation? t)
          (f t opts)

          :else
          (let [tmeta (time/-time-meta (class t))]
            (if-let [p-fn (-> tmeta :rep :to :via)]
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
 
   (t/month (Date. 0) {:timezone \"EST\"}) => 12"
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
   (t/day-of-week 0 {:timezone \"GMT\"}) => 5
 
   (t/day-of-week (Date. 0) {:timezone \"EST\"}) => 4"
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
   (t/now)
   => #(instance? Date %)
 
   (t/now {:type Calendar})
   => #(instance? Calendar %)"
  {:added "2.2"}
  ([] (now {}))
  ([opts] (time/-now (merge {:type common/*default-type*}
                            opts))))

(defn epoch
  "returns the beginning of unix epoch
   (t/epoch)
   => #inst \"1970-01-01T00:00:00.000-00:00\"
 
   (t/epoch {:type clojure.lang.PersistentArrayMap :timezone \"GMT\"})
   => {:year 1970, :month 1 :day 1, :day-of-week 5, 
       :hour 0 :minute 0 :second 0 :millisecond 0, 
       :timezone \"GMT\"}"
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
 
   (t/adjust {:year 1970, :month 1 :day 1, :day-of-week 5, 
              :hour 0 :minute 0 :second 0 :millisecond 0, 
              :timezone \"GMT\"}
             {:year 1999})
   => {:year 1999, :month 1 :day 1, :day-of-week 6, 
       :hour 0 :minute 0 :second 0 :millisecond 0, 
       :timezone \"GMT\"}"
  {:added "2.2"}
  ([t rep]
   (adjust t rep {}))
  ([t rep opts]
   (let [m (-> (to-map t opts)
               (merge rep)
               (assoc :type Long))]
     (from-long (from-map m)
                (assoc m :type (class t))))))

(defn truncate
  "truncates the time to a particular field
   (t/truncate #inst \"1989-12-28T12:34:00.000-00:00\"
               :hour {:timezone \"GMT\"})
   => #inst \"1989-12-28T12:00:00.000-00:00\"
   
   (t/truncate #inst \"1989-12-28T12:34:00.000-00:00\"
               :year {:timezone \"GMT\"})
   => #inst \"1989-01-01T00:00:00.000-00:00\""
  {:added "2.2"}
  ([t col]
   (truncate t col {}))
  ([t col opts]
   (let [rep (to-map t opts)
         trep (select-keys rep (drop-while #(not= col %) common/+default-keys+))]
     (from-map trep
               (assoc opts :type (class t))
               common/+zero-values+))))

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
