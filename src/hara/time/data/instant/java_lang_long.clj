(ns hara.time.data.instant.java-lang-long
  (:require [hara.protocol.time :as time]
            [hara.time.common :as common]))

(extend-type Long  
  time/IInstant
  (-to-long      [t] t))

(defmethod common/from-long Long
  [type long]
  long)