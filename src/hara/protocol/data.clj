(ns hara.protocol.data)

(defprotocol IData
  (-to-data [obj]))

(defmulti -meta-object (fn [type] type))

(defmethod -meta-object :default
  [type]
  {:class type
   :types #{}})
