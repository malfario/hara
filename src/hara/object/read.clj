(ns hara.object.read
  (:require [hara.protocol.object :as object]
            [hara.string.case :as case]
            [hara.reflect :as reflect]
            [clojure.walk :as walk]))

(defn meta-read
  [^Class cls]
  (assoc (object/-meta-read cls) :class cls))

(defn read-reflect-fields
  [cls]
  (->> (reflect/query-class cls [:field])
       (map (juxt (comp keyword case/spear-case :name)
                  identity))
       (into {})))

(defn read-getters
  ([cls] (read-getters cls {:prefix "get"
                            :template '(fn <method> [obj] (. obj (<method>)))}))
  ([cls {:keys [prefix template]}]
   (->> [:method :instance (re-pattern (str "^" prefix ".+")) 1]
        (reflect/query-class cls)
        (reduce (fn [out ele]
                  (assoc out
                         (-> (:name ele) (subs (count prefix)) case/spear-case keyword)
                         (eval (walk/postwalk-replace {'<method> (symbol (:name ele))}
                                                      template))))
                {}))))

(defn to-data
  [obj]
  (let [cls (type obj)
        {:keys [to-string to-map methods]} (meta-read cls)]
    (cond (instance? java.util.Map obj)
          obj

          to-string (to-string obj)
          to-map (to-map obj)
          methods (reduce-kv (fn [out k func]
                               (if-let [v (func obj)]
                                 (assoc out k (to-data v))
                                 out))
                             {}
                             methods)
          (.isArray ^Class cls)
          (->> (seq obj)
               (mapv to-data))

          (instance? java.lang.Iterable obj)
          (mapv to-data obj)

          (instance? java.util.Iterator obj)
          (->> obj iterator-seq (mapv to-data))

          (instance? java.util.AbstractCollection obj)
          (to-data (.iterator obj))

          :else obj)))