(ns hara.time.format-test
  (:use midje.sweet)
  (:require [hara.time.format :as f]
            [hara.time.data :as data]
            [hara.time.data.common :as common])
  (:import [java.util Date Calendar TimeZone]
           java.sql.Timestamp))

^{:refer hara.time.format/format :added "2.2"}
(fact "converts a date into a string"
  (f/format (Date. 0) "HH MM dd Z" {:timezone "GMT" :cached true})
  => "00 01 01 +0000"
  
  (f/format (common/calendar (Date. 0)
                             (TimeZone/getTimeZone "GMT"))
            "HH MM dd Z"
            {})
  => "00 01 01 +0000"

  (f/format (Timestamp. 0)
            "HH MM dd Z"
            {:timezone "GMT"})
  => "00 01 01 +0000")

^{:refer hara.time.format/parse :added "2.2"}
(fact "converts a string into a date"
  (f/parse "00 00 01 01 01 1989 +0000" "ss mm HH dd MM yyyy Z" {:type Date :timezone "GMT"})
  => #inst "1989-01-01T01:00:00.000-00:00"

  (-> (f/parse "00 00 01 01 01 1989 +0000" "ss mm HH dd MM yyyy Z"
               {:type Calendar})
      (.getTime))
  => #inst "1989-01-01T01:00:00.000-00:00"

  (type (f/parse "00 00 01 01 01 1989 +0000" "ss mm HH dd MM yyyy Z"
                 {:type Timestamp}))
  => Timestamp)
