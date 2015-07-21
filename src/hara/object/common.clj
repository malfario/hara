(ns hara.object.common
  (:require [hara.object.meta :as meta]
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
        (let [{:keys [types to-data] :as mobj} (meta/-meta-object obj)]
          (println "TO_DATA: " to-data)
          (cond to-data (to-data obj)
                (get types java.util.Map) (map/-to-map obj)
                (get types String) (string/-to-string obj)
                :else (-to-data obj)))))

(extend-protocol IData
  nil
  (-to-data [obj] obj)

  #_java.lang.Iterable
  #_(-to-data [obj] (mapv to-data obj))

  java.util.Iterator
  (-to-data [obj] (->> obj iterator-seq (mapv to-data)))

  Object
  (-to-data [obj]
    (event/raise {:value obj
                  :msg (str "Cannot covert " obj " to data.")}
                 (option :nil [] nil)
                 (default :nil))))

