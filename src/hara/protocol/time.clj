(ns hara.protocol.time)

(defprotocol IRegion
  (-timezone     [t]))

(defprotocol IInstant
  (-to-long      [t])
  (-timezone?    [t])
  (-milli        [t tz])
  (-second       [t tz])
  (-minute       [t tz])
  (-hour         [t tz])
  (-day          [t tz])
  (-day-of-week  [t tz])
  (-month        [t tz])
  (-year         [t tz]))

(defprotocol IPeriod
  (-duration     [t])
  (-millis       [t])
  (-seconds      [t])
  (-minutes      [t])
  (-hours        [t])
  (-days         [t])
  (-months       [t])
  (-years        [t]))
