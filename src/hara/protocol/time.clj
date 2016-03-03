(ns hara.protocol.time)

(defmulti -time-meta (fn [cls] cls))

(defmulti -timezone (fn [str opts] (:type opts)))

(defprotocol IInstant
  (-to-long      [t]))

(defmulti -from-long (fn [long opts] (:type opts)))

(defmulti -now (fn [opts] (:type opts)))

(defprotocol IRepresentation
  (-millisecond  [t opts])
  (-second       [t opts])
  (-minute       [t opts])
  (-hour         [t opts])
  (-day          [t opts])
  (-day-of-week  [t opts])
  (-month        [t opts])
  (-year         [t opts]))

(defprotocol IDuration
  (-to-length  [d opts]))

(defmulti -from-length (fn [long opts] (:type opts)))

(defprotocol IInterval
  (-duration  [in]))
