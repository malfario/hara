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

^{:refer hara.time.data.common/local-timezone :added "2.2"}
(fact "returns the current timezone as a string")

^{:refer hara.time.data.common/default-timezone :added "2.2"}
(fact "accesses the default timezone as a string")

^{:refer hara.time.data.common/default-type :added "2.2"}
(fact "accesses the default type for datetime")
