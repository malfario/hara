(ns hara.object.write
  (:require [clojure.walk :as walk]
            [hara.protocol.object :as object]
            [hara.protocol.map :as map]
            [hara.data.map :as data]
            [hara.string.case :as case]
            [hara.reflect :as reflect]
            [hara.reflect.util :as reflect-util]))

(defn meta-write
  [^Class cls]
  (assoc (object/-meta-write cls) :class cls))

(declare from-data)

(defn write-reflect-fields
  [cls]
  (->> (reflect/query-class cls [:field])
       (reduce (fn [out ele]
                 (let [k (-> ele :name case/spear-case keyword)
                       cls (.getType (get-in ele [:all :delegate]))]
                   (assoc out k {:type cls :fn ele})))
               {})))

(defn write-setters
  ([cls] (write-setters cls {:prefix "set"
                             :template '(fn <method> [obj val]
                                          (. obj (<method> val))
                                          obj)}))
  ([cls {:keys [prefix template]}]
   (->> [:method :instance (re-pattern (str "^" prefix ".+")) 2]
        (reflect/query-class cls)
        (reduce (fn [out ele]
                  (assoc out
                         (-> (:name ele) (subs (count prefix)) case/spear-case keyword)
                         {:type (-> ele :params second)
                          :fn (eval (walk/postwalk-replace {'<method> (symbol (:name ele))}
                                                                template))}))
                {}))))

(defn from-empty [m empty methods]
  (let [obj (empty m)]
    (reduce-kv (fn [obj k v]
                 (if-let [{:keys [type] func :fn} (get methods k)]
                   (func obj (from-data v type))
                   obj))
               obj
               m)))

(defn from-map
  [m ^Class cls]
  (let [m (if-let [rels (get object/*transform* type)]
            (data/transform-in m rels)
            m)
        {:keys [empty methods from-map] :as mobj} (meta-write cls)]
    (cond from-map
          (from-map m)

          (and empty methods)
          (from-empty m empty methods)

          :else
          (map/-from-map m cls))))

(defn from-data
  {:added "2.2"}
  [arg ^Class cls]
  (let [^Class targ (type arg)]
    (cond
      ;; If there is a direct match
      (reflect-util/param-arg-match cls targ)
      arg

      ;; If there is a vector
      (and (vector? arg)
           (.isArray cls))
      (let [cls (.getComponentType cls)]
        (->> arg
             (map #(from-data % cls))
             (into-array cls)))

      :else
      (let [{:keys [from-string] :as mobj} (meta-write cls)]
        (cond
          ;; If input is a string and there is a from-string method
          (and (string? arg) from-string)
          (from-string arg cls)

          ;; If the input is a map
          (map? arg)
          (from-map arg cls)

          :else
          (throw (Exception. (format "Problem converting %s to %s" arg targ))))))))
