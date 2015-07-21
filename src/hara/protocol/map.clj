(ns hara.protocol.map)

(defprotocol ILookup
  (-get [obj k])
  (-get-in [obj ks]))

(defprotocol IMap
  (-to-map [obj])
  (-to-map-meta [obj]))

(defmulti -from-map (fn [map type] type))
