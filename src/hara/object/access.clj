(ns hara.object.access
  (:require [hara.data.nested :as nested]
            [hara.object.base :as base]
            [hara.object.util :as util]
            [hara.reflect.util :as reflect-util]
            [hara.reflect :as reflect])
  (:import hara.reflect.types.element.Element))

(defn access-get
  "access data within a class through keyword getters"
  {:added "2.2"} [obj k]
  (if-let [getter (-> obj base/meta-object :getters k)]
    (getter obj)
    (if (instance? java.util.Map obj)
      (get obj k))))

(defn coerce
  "coerces data into the right class
   (-> (coerce  Authentication {:username \"chris\"})
       map/-to-map)
   => {:username \"chris\"}"
  {:added "2.2"} [^Class param arg]
  (let [^Class targ (type arg)
        {:keys [types from-data]} (base/meta-object param)]
    (cond (and (vector? arg)
               (.isArray param))
          (let [cls (.getComponentType targ)]
            (->> arg
                 (map #(coerce cls %))
                 (into-array cls)))

          (reflect-util/param-arg-match param targ) arg

          :else
          (if (-> from-data nil? not)
            (from-data arg param)
            (throw (Exception. (str "Cannot convert value " arg
                                    " of type " (.getName targ) " to " (.getName param)) ))))))

(defn access-set-coerce
  "function that allows access-set to use coercion"
  {:added "2.2"}
  ([{:keys [params] :as ele} args]
   (cond (nil? params)
         (apply ele args)

         :else
         (apply ele (map coerce params args)))))

(defn access-set
  "access data within a class through keyword setters"
  {:added "2.2"} [obj k v]
  (if-let [setter (-> obj base/meta-object :setters k)]
    (let [v (if (vector? v) v [v])]
      (cond (instance? Element setter)
            (-> obj
                (reflect/query-class obj [(util/clojure->java (name k) :set) :#]) 
                (apply access-set-coerce obj v))

            :else
            (apply setter obj v))))
  obj)

(defn access-get-nested
  "access data within nested classes"
  {:added "2.2"} [obj [k & more]]
  (cond (nil? obj) nil

        (empty? more) (access-get obj k)

        :else (access-get-nested (access-get obj k) more)))

(defn access-set-nested
  "set data within nested classes"
  {:added "2.2"} [obj [k & more] v]
  (cond (or (nil? obj) (nil? k)) nil

        (empty? more) (access-set obj k v)

        :else (access-set-nested (access-get obj k) more v))
  obj)

(defn access-set-map
  "sets class data using a map"
  {:added "2.2"} [obj m]
  (reduce-kv (fn [obj k v]
               (access-set obj k v))
             obj
             m))

(defn access
  "generic interface for getters and setters"
  {:added "2.2"}
  ([obj]
   (-> obj
       base/meta-object
       (select-keys [:getters :setters])
       (nested/update-vals-in [] keys)))
  ([obj k]
   (cond (keyword? k) (access-get obj k)
         (vector? k)  (access-get-nested obj k)
         (map? k)     (access-set-map obj k)))
  ([obj k v]
   (cond (keyword? k) (access-set obj k v)
         (vector? k)  (access-set-nested obj k v))))
