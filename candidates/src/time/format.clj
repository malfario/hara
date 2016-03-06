(ns hara.time.format
  (:require [hara.protocol.time :as time]
            [hara.time.data.common :as common])
  (:refer-clojure :exclude [format]))

;; Common set of time references:
;;
;; s - seconds in minute
;; m - minutes in hour
;; H - hours in day
;; d - day in month
;; M - month in year
;; y - year
;; Z - timezone

;; Please refer to the specific formatter strings for the following:

;; java.text.SimpleDateFormat (https://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html)
;;  - java.util.Date
;;  - java.util.Calendar
;;  - java.sql.Timestamp

;; java.time.format.DateTimeFormatter (https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html)
;; - java.time.Instant
;; - java.time.Clock
;; - java.time.ZonedDateTime

(defonce +format-cache+ (atom {}))

(defonce +parse-cache+  (atom {}))

(defn cache [cache constructor ks flag]
  (cond flag
        (or (get-in @cache ks)
            (let [obj (constructor)]
              (swap! cache assoc-in ks obj)
              obj))

        :else
        (constructor)))

(defn format
  "converts a date into a string
   (f/format (Date. 0) \"HH MM dd Z\" {:timezone \"GMT\" :cached true})
   => \"00 01 01 +0000\"
   
   (f/format (common/calendar (Date. 0)
                              (TimeZone/getTimeZone \"GMT\"))
             \"HH MM dd Z\"
             {})
   => \"00 01 01 +0000\"
 
   (f/format (Timestamp. 0)
             \"HH MM dd Z\"
             {:timezone \"GMT\"})
   => \"00 01 01 +0000\""
  {:added "2.2"}
  ([t pattern] (format t pattern {}))
  ([t pattern {:keys [cached] :as opts}]
   (let [tmeta (time/-time-meta (class t))
         ftype (-> tmeta :formatter :type)
         fmt   (cache +format-cache+
                      (fn [] (time/-formatter pattern (assoc opts :type ftype)))
                      [ftype pattern]
                      cached)]
     (time/-format fmt t opts))))

(defn parse
  "converts a string into a date
   (f/parse \"00 00 01 01 01 1989 +0000\" \"ss mm HH dd MM yyyy Z\")
   => #inst \"1989-01-01T01:00:00.000-00:00\"
 
   (-> (f/parse \"00 00 01 01 01 1989 +0000\" \"ss mm HH dd MM yyyy Z\"
                {:type Calendar})
       (.getTime))
   => #inst \"1989-01-01T01:00:00.000-00:00\"
 
   (type (f/parse \"00 00 01 01 01 1989 +0000\" \"ss mm HH dd MM yyyy Z\"
                  {:type Timestamp}))
   => Timestamp"
  {:added "2.2"}
  ([s pattern] (parse s pattern {}))
  ([s pattern {:keys [cached] :as opts}]
   (let [opts   (merge {:type common/*default-type*} opts)
         type   (:type opts) 
         tmeta  (time/-time-meta type)
         ptype  (-> tmeta :parser :type)
         parser (cache +parse-cache+
                      (fn [] (time/-parser pattern (assoc opts :type ptype)))
                      [ptype pattern]
                      cached)]
     (time/-parse parser s opts))))
