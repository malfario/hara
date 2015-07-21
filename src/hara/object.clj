(ns hara.object
  (:require [hara.object.meta :as meta]
            [hara.object.enum :as enum]
            [hara.object.map-like :as map-like]
            [hara.object.string-like :as string-like]
            [hara.protocol.map :as map]
            [hara.protocol.string :as string]
            [hara.event :as event]))

(defprotocol IData
  (-to-data [obj]))

(defn to-data [obj]
  (cond (.isArray ^Class (type obj))
        (->> (seq obj)
             (mapv to-data))

        :else
        (let [{:keys [types to-data] :as mobj} (meta/-meta-object (type obj))]
          (cond to-data (to-data obj)
                (get types java.util.Map) (map/-to-map obj)
                (get types String) (string/-to-string obj)
                :else (-to-data obj)))))

(defn from-data [data type]
  (let [{:keys [from-data] :as mobj} (meta/-meta-object type)]
    (cond from-data
          (from-data data type)

          (map? data)
          (map/-from-map data type)

          (string? data)
          (string/-from-string data type))))

(defn meta-object [type]
  (meta/-meta-object type))

(extend-protocol IData
  nil
  (-to-data [obj] obj)

  java.lang.Iterable
  (-to-data [obj]
    (mapv to-data obj))

  java.util.Iterator
  (-to-data [obj]
    (->> obj iterator-seq (mapv to-data)))

  Object
  (-to-data [obj]
    (event/raise {:value obj
                  :msg (str "Cannot covert " obj " to data.")}
                 (option :nil [] nil)
                 (default :nil))))

(defmacro extend-stringlike [& {:as classes}]
  `(vector ~@(map (fn [[cls opts]]
                    `(string-like/extend-stringlike-class ~cls ~opts))
                  classes)))

(defmacro extend-maplike [& {:as classes}]
  `(vector ~@(map (fn [[cls opts]]
                    `(map-like/extend-maplike-class ~cls ~opts))
                  classes)))

