(ns hara.object.map-like
  (:require [hara.protocol.map :as map]
            [hara.protocol.data :as data]
            [hara.object.base :as base]
            [hara.object.util :as util]))

(defn generic-map [obj {:keys [select exclude getters] :as opts}]
  (-> (if getters
        (eval getters)
        (util/object-getters obj))
      (#(apply dissoc % :class exclude))
      (#(if select
          (select-keys % select)
          %))
      (util/object-apply obj base/to-data)))

(defmacro extend-maplike-class [cls {:keys [tag to from meta] :as opts}]
  `(vector
    (defmethod data/-meta-object ~cls
      [type#]
      (hash-map :class     type#
               :types     #{java.util.Map}
               :to-data   map/-to-map
               ~@(if from [:from-data ~from] [])))

    (extend-protocol map/IMap
      ~cls
      ~(if to
         `(-to-map [entry#]
                   (~to entry#))
         `(-to-map [entry#]
                   (generic-map entry# ~opts)))

      ~(if meta
         `(-to-map-meta
           [entry#]
           (~meta entry#))
         `(-to-map-meta
           [entry#]
           {:class ~cls})))

    ~@(if from
        [`(defmethod map/-from-map ~cls
            [data# type#]
            (~from data# type#))])

    (defmethod print-method ~cls
      [v# ^java.io.Writer w#]
      (.write w# (str "#" ~(or tag cls) "" (map/-to-map v#))))))
