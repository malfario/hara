(ns hara.time.data.format.java-time-format-datetimeformatter
  (:require [hara.protocol.time :as time]
            [hara.time.data
             [coerce :as coerce]
             [zone :as zone]])
  (:import [java.time
            Instant Clock ZonedDateTime
            LocalDateTime ZoneId
            LocalDate LocalTime]
           [java.time.format DateTimeFormatter ResolverStyle]))

(defmethod time/-formatter DateTimeFormatter
  [pattern {:keys [timezone] :as opts}]
  (DateTimeFormatter/ofPattern pattern))

(defmethod time/-format [DateTimeFormatter Instant]
  [^DateTimeFormatter formatter ^Instant t {:keys [timezone]}]
  (let [tz  (if timezone
              (coerce/coerce-zone timezone {:type ZoneId})
              (ZoneId/systemDefault))]
    (.format formatter (LocalDateTime/ofInstant t tz))))

(defmethod time/-format [DateTimeFormatter Clock]
  [^DateTimeFormatter formatter ^Clock t {:keys [timezone]}]
  (.format formatter
           (LocalDateTime/ofInstant (.instant t)
                                    (if timezone
                                      (coerce/coerce-zone timezone {:type ZoneId})
                                      (.getZone t)))))

(defmethod time/-format [DateTimeFormatter ZonedDateTime]
  [^DateTimeFormatter formatter ^ZonedDateTime t {:keys [timezone]}]
  (.format formatter (if timezone
                       (.withZoneSameInstant t ^ZoneId (coerce/coerce-zone timezone {:type ZoneId}))
                       t)))

(defmethod time/-parser DateTimeFormatter
  [pattern {:keys [timezone] :as opts}]
  (DateTimeFormatter/ofPattern pattern))

(defmethod time/-parse [DateTimeFormatter Instant]
  [^DateTimeFormatter parser s opts]
  (Instant/from (.parse parser s)))

(defmethod time/-parse [DateTimeFormatter ZonedDateTime]
  [^DateTimeFormatter parser s opts]
  (let [t (ZonedDateTime/from (.parse parser s))
        offset (.getId (.getZone t))
        ^ZoneId tz (coerce/coerce-zone (-> offset
                                           zone/by-string-offset
                                           zone/by-offset)
                                       {:type ZoneId})]
    (.withZoneSameLocal t tz)))

(defmethod time/-parse [DateTimeFormatter Clock]
  [^DateTimeFormatter parser s opts]
  (let [^ZonedDateTime dt (time/-parse parser s (assoc opts :type ZonedDateTime))]
    (Clock/fixed (.toInstant dt)
                 (.getZone dt))))

