(ns hara.time.common)

(defmulti meta-info (fn [obj] (class obj)))

(defmulti from-long (fn [type long tz] type))

(defmulti from-map (fn [type map] type))