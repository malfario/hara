(ns hara.time.data.coerce-test
  (:use midje.sweet)
  (:require [hara.time.data
             long
             [coerce :refer :all]]
            [hara.protocol
             [string :as string]
             [time :as time]]
            [hara.time.data.instant
             java-util-date
             java-util-calendar]
            [hara.time.data.zone.java-util-timezone])
  (:import [java.util Date Calendar TimeZone]))

^{:refer hara.time.data.coerce/coerce-instant :added "2.2"}
(fact "coercion of one instant object to another"
  (-> ^Calendar (coerce-instant 0 {:type Calendar})
      (.getTime)
      (.getTime))
  => 0)

^{:refer hara.time.data.coerce/coerce-zone :added "2.2"}
(fact "coercion of one zone object to another"
  (-> (coerce-zone "Asia/Kolkata" {:type TimeZone})
      (string/-to-string))
  => "Asia/Kolkata"

  (-> (coerce-zone nil {:type TimeZone})
      (string/-to-string))
  => (-> (TimeZone/getDefault)
         (string/-to-string)))

^{:refer hara.time.data.coerce/coerce :added "2.2"}
(fact "general coercion function"
  (coerce (Date. 0) {:type Long})
  => 0

  (-> (coerce "Asia/Kolkata" {:type TimeZone})
      (string/-to-string))
  => "Asia/Kolkata")
