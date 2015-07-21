(ns hara.object.meta)

(defmulti -meta-object (fn [type] type))

(defmethod -meta-object :default
  [type]
  {:class type
   :types #{}})
