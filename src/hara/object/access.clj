(ns hara.object.access
  (:require [hara.object.base :as base]
            [hara.object.util :as object]
            [hara.reflect.util :as util]
            [hara.reflect :as reflect]))

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

(defn access
  ([obj]
   (let [rfunc (fn [m ks val]
                 (reduce-kv (fn [out k v]
                              (update-in out [k] (fnil #(conj % val) #{})))
                         m
                         ks))]
     (-> {}
         (rfunc (object/object-getters obj) :get)
         (rfunc (object/object-setters obj) :set))))
  ([obj k]
   (base/to-data (reflect/apply-element obj (object/clojure->java (name k)) [])))
  ([obj k v]
   (apply-with-coercion
    (reflect/query-class obj [(object/clojure->java (name k) :set) 2 :#]) [obj v])))



(comment
  (ns-unalias 'hara.object.access 'reflect))
