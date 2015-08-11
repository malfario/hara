(ns hara.object.access
  (:require [hara.data.nested :as nested]
            [hara.object.base :as base]
            [hara.object.util :as object]
            [hara.reflect.util :as util]
            [hara.reflect :as reflect])
  (:import hara.reflect.types.element.Element))

(defn may-coerce [^Class param arg]
  (let [^Class targ (type arg)
        {:keys [types from-data]} (base/meta-object param)]
    (cond (util/param-arg-match param targ) arg

          :else
          (if (and (-> types empty? not)
                   (-> from-data nil? not)
                   (types targ))
            (from-data arg param)
            (throw (Exception. (str "Cannot convert value " arg
                                    " of type " (.getName targ) " to " (.getName param)) ))))))

(defn apply-with-coercion
  ([{:keys [params] :as ele} args]
   (apply ele (map may-coerce params args))))

(defn access-get [obj k]
  (if-let [getter (-> obj base/meta-object :getters k)]
    (getter obj)
    (if (instance? java.util.Map obj)
      (get obj k))))

(defn access-set [obj k v]
  (if-let [setter (-> obj base/meta-object :setters k)]
    (cond (instance? Element setter)
          (apply-with-coercion
           (reflect/query-class obj [(object/clojure->java (name k) :set) 2 :#]) [obj v])

          :else
          (setter obj v)))
  obj)

(defn access-get-nested [obj [k & more]]
  (cond (nil? obj) nil
        
        (empty? more) (access-get obj k)

        :else (access-get-nested (access-get obj k) more)))

(defn access-set-nested [obj [k & more] v]
  (cond (or (nil? obj) (nil? k)) nil
        
        (empty? more) (access-set obj k v)

        :else (access-set-nested (access-get obj k) more v))
  obj)

(defn access-set-map [obj m]
  (reduce-kv (fn [obj k v]
               (access-set obj k v))
             obj
             m))

(defn access
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
