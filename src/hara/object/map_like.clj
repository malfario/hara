(ns hara.object.map-like
  (:require [hara.protocol.map :as map]
            [hara.protocol.data :as data]
            [hara.object.base :as base]
            [hara.object.util :as util]))

(defn map-functions [obj {:keys [type func default exclude include extra] :as opts}]
  (let [fns (if-not (false? default) (func obj) {})
        fns (if include
              (select-keys fns include)
              fns)
        fns (apply dissoc fns :class exclude)
        fns (if extra
              (merge extra fns)
              fns)]
    fns))

(defmacro extend-maplike-class [cls {:keys [tag meta to from proxy getters setters default hide] :as opts}]
  `(vector
    
    (defmethod data/-meta-object ~cls
      [type#]
      {:class      type#
       :types      #{java.util.Map}
       :to-data    map/-to-map
       :from-data  map/-from-map
       :getters    (map-functions type# (assoc ~opts
                                               :type :get
                                               :func util/object-getters
                                               :extra ~getters))
       
       :setters    (map-functions type# (assoc ~opts
                                               :type :set
                                               :func util/object-setters
                                               :extra ~setters))})
    
    (extend-protocol map/IMap
      ~cls
      ~(if to
         `(-to-map [obj#]
                   (~to obj#))
         `(-to-map [obj#]
                   (let [getters# (:getters (data/-meta-object (type obj#)))]
                     (util/object-apply getters# obj# base/to-data))))
      
      ~(if meta
         `(-to-map-meta
           [obj#]
           (~meta obj#))
         `(-to-map-meta
           [obj#]
           {:class ~cls})))

      ~(if from
         `(defmethod map/-from-map ~cls
            [data# type#]
            (~from data# type#))
         `(defmethod map/-from-map ~cls
            [data# type#]
            (throw (Exception. (str "Cannot create " type# " from map.")))))

    (defmethod print-method ~cls
      [v# ^java.io.Writer w#]
      (.write w# (str "#" ~(or tag cls) ""
                      (dissoc (map/-to-map v#) ~@hide))))))
