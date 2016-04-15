(ns hara.object.base
  (:require [hara.protocol.data :as data]
            [hara.protocol.map :as map]
            [hara.protocol.string :as string]
            [hara.event :as event]
            [hara.reflect :as reflect]
            [hara.data.map])
  (:import java.util.Map))

(defonce ^:dynamic *transform* nil)

(defn map-transform-to
  "transforms a map back to the structure that it came from
   (binding [*transform* {Class {[:authority :username] [:user]
                                 [:authority :password] [:pass]}}]
     (map-transform-to {:authority {:username \"chris\"
                                    :password \"pass\"}} Class))
   => {:pass \"pass\", :user \"chris\"}"
  {:added "2.2"} [m type]
  (if-let [rels (get *transform* type)]
    (hara.data.map/retract-in m rels)
    m))

(defn map-transform-from
  "transforms map to a new structure:
   (binding [*transform* {Class {[:authority :username] [:user]
                                 [:authority :password] [:pass]}}]
     (map-transform-from {:pass \"pass\", :user \"chris\"} Class))
   =>  {:authority {:username \"chris\"
                    :password \"pass\"}}"
  {:added "2.2"} [m type]
  (if-let [rels (get *transform* type)]
    (hara.data.map/transform-in m rels)
    m))

(defn meta-object
  "gets meta-object for a particular class
   (meta-object Class)
   => {:class java.lang.Class, :types #{}}"
  {:added "2.2"} [type]
  (data/-meta-object (reflect/context-class type)))

(defn to-data
  "returns data from a java type
   (to-data (into-array Object [{:a 1} 2 3]))
   => [{:a 1} 2 3]"
  {:added "2.2"} [obj]
  (cond (.isArray ^Class (type obj))
        (->> (seq obj)
             (mapv to-data))

        (instance? java.util.Map obj)
        obj

        :else
        (let [t (type obj)
              {:keys [types to-data] :as mobj} (data/-meta-object t)
              result (cond to-data (to-data obj)
                           (get types Map) (map/-to-map obj)
                           (get types String) (string/-to-string obj)
                           :else (data/-to-data obj))]
          (if (map? result)
            (map-transform-to result t)
            result))))

(extend-protocol data/IData
  nil
  (-to-data [obj] obj)

  java.util.Map
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

(defn from-data
  "creates a java type from data"
  {:added "2.2"} [data type]
  (let [{:keys [from-data] :as mobj} (meta-object type)
        data (map-transform-from data type)]
    (cond from-data
          (from-data data type)

          (map? data)
          (map/-from-map data type)

          (string? data)
          (string/-from-string data type))))
