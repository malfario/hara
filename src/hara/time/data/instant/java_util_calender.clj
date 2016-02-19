(ns hara.time.data.instant.java-util-calender
  (:require [hara.protocol.time :as time]
            [hara.time.common :as common])
  (:import [java.util Date]))

(extend-type Date
  time/IInstant  
  (-to-long      [t]
    (.getTime t)))