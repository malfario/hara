(ns hara.object.vector-like
  (:require [hara.protocol.object :as object]
            [hara.object.read :as read]
            [hara.object.print :as print]))

(defmacro extend-vector-like
  {:added "2.3"}
  [cls {:keys [read write] :as opts}]
  (cond-> []
    read  (conj `(defmethod object/-meta-read ~cls
                   [~'_]
                   ~(-> {:to-vector read}
                        (print/assoc-print-vars opts))))
    write (conj `(defmethod object/-meta-write ~cls
                   [~'_]
                   {:from-vector ~write}))

    true  (conj `(print/extend-print ~cls))))
