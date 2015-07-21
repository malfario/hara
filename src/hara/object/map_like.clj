(ns hara.object.map-like
  (:require [hara.protocol.map :as map]
            [hara.object.meta :as meta]
            [hara.object.util :as util]))

(defn generic-map [obj {:keys [select exclude] :as opts}]
  (-> (util/object-methods obj)
      (#(apply dissoc % :class exclude))
      (#(if select
          (select-keys % select)
          %))
      (util/object-apply obj map/-to-map)))

(defmacro extend-maplike-class [cls {:keys [tag to from meta] :as opts}]
  `(vector
    (defmethod meta/-meta-object ~cls
      [type#]
      (hashmap :class     type#
               :types     #{java.util.Map}
               :to-data   map/-to-map
               ~@(if from [:from-data ~from] [])))

    (extend-protocol map/IMap
      ~cls
      (-to-map [entry#]
        (if to
          (~to entry#)
          (generic-map entry#)))

      (-to-map-meta [entry#]
        (if meta
          (~meta entry#)
          {:type ~cls})))

    ~@(if from
        [`(defmethod map/-from-map ~cls
            [data# type#]
            (~from data# type#))])

    (defmethod print-method ~cls
      [v# ^java.io.Writer w#]
      (.write w# (str "#" ~(or tag cls) "" (map/-to-map v#))))))
