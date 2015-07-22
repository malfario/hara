(ns hara.object.base
  (:require [hara.protocol.data :as data]
            [hara.protocol.map :as map]
            [hara.protocol.string :as string]
            [hara.event :as event]))

(defn meta-object [type]
  (data/-meta-object type))

(defn to-data [obj]
  (cond (.isArray ^Class (type obj))
        (->> (seq obj)
             (mapv to-data))

        :else
        (let [{:keys [types to-data] :as mobj} (data/-meta-object (type obj))]
          (cond to-data (to-data obj)
                (get types java.util.Map) (map/-to-map obj)
                (get types String) (string/-to-string obj)
                :else (data/-to-data obj)))))

(extend-protocol data/IData
  nil
  (-to-data [obj] obj)

  java.lang.Iterable
  (-to-data [obj]
    (mapv to-data obj))

  java.util.Iterator
  (-to-data [obj]
    (->> obj iterator-seq (mapv to-data)))

  java.util.AbstractCollection
  (-to-data [obj]
    (data/-to-data (.iterator obj)))

  Object
  (-to-data [obj]
    (event/raise {:value obj
                  :msg (str "Cannot covert " obj " to data.")}
                 (option :nil [] nil)
                 (option :none [] obj)
                 (default :none))))

(defn from-data [data type]
  (let [{:keys [from-data] :as mobj} (meta-object type)]
    (cond from-data
          (from-data data type)

          (map? data)
          (map/-from-map data type)

          (string? data)
          (string/-from-string data type))))
