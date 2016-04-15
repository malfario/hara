(ns hara.object.builder
  (:require [hara.common.string :refer [to-string]]
            [hara.string.case :as case]
            [hara.reflect :as reflect]
            [hara.object.access :as access]))

(defmulti build-template identity)

(defmethod build-template :default
  [cls]
  (throw (Exception. (str "No template found for class " cls))))

(defn build
  "creates an object from a build template"
  {:added "2.2"} [{:keys [init final setters exclude prefix] :as template} data]
  (let [builder (init data)
        builder (reduce-kv (fn [b k v]
                             (if-let [setter (setters k)]
                               (doto builder
                                 (setter v))
                               (let [setter-name (->> (to-string k)
                                                      (str prefix "-")
                                                      (case/camel-case))
                                     setter (reflect/query-hierarchy builder [setter-name :#])
                                     v (if (vector? v) v [v])]
                                 (access/access-set-coerce setter (cons builder v)))))
                           builder
                           (apply dissoc data exclude))]
    (final builder)))
