(ns hara.protocol.time)

(defmulti -time-meta (fn [cls] cls))

(defprotocol IInstant
  (-to-long       [t])
  (-has-timezone? [t])
  (-get-timezone  [t])
  (-with-timezone [t tz]))

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

(defmulti -formatter (fn [pattern opts] (:type opts)))

(defmulti -format (fn [formatter t opts]
                    [(class formatter) (class t)]))

(defmulti -parser (fn [pattern opts] (:type opts)))

(defmulti -parse  (fn [parser s opts]
                    [(class parser) (:type opts)]))
