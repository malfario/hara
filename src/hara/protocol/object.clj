(ns hara.protocol.object)

(defonce ^:dynamic *transform* nil)

(defmulti -meta-read identity)

(defmethod -meta-read :default
  [_]
  {})

(defmulti -meta-write identity)

(defmethod -meta-write :default
  [_]
  {})