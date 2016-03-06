(ns hara.time.data.interval
  (:require [hara.protocol.time :as time]))

(defrecord Interval [start end]
  time/IInterval
  (-duration  [_] (- (time/-to-long end)
                     (time/-to-long start))))

(defn interval [start end]
  (Interval. start end))
