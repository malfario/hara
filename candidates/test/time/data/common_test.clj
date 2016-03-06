(ns hara.time.data.common-test
  (:use midje.sweet)
  (:require [hara.time.data.common :refer :all]
            [hara.protocol.time :as time])
  (:import [java.util Date TimeZone Calendar]))

^{:refer hara.time.data.common/calendar :added "2.2"}
(fact "creates a calendar to be used by the base date classes"
  (-> ^Calendar (calendar (Date. 0) (TimeZone/getTimeZone "GMT"))
      (.getTime))
  => #inst "1970-01-01T00:00:00.000-00:00")

^{:refer hara.time.data.common/to-proxy :added "2.2"}
(fact "uses a proxy class for retrieving date representation"
  (to-proxy 0 {} (-> (time/-time-meta Long)
                     :rep :to))
  => (contains
      [#(instance? Calendar %)
       (contains {:fn #(every? % [:day :hour :timezone :second
                                  :day-of-week :month :year
                                  :millisecond :minute])})]))

^{:refer hara.time.data.common/local-timezone :added "2.2"}
(fact "returns the current timezone as a string")
