(ns hara.object.vector-like
  (:require [hara.protocol.object :as object]
            [hara.object.read :as read]))

(defmacro extend-vector-like
  {:added "2.3"}
  [cls {:keys [tag read write meta] :as opts}]
  (cond-> []
    read  (conj `(defmethod object/-meta-read ~cls
                   [~'_]
                   {:to-vector ~read}))
    write (conj `(defmethod object/-meta-write ~cls
                   [~'_]
                   {:from-vector ~write}))

    true  (conj `(defmethod print-method ~cls
                  [v# ^java.io.Writer w#]
                  (.write w# (str "#" (or ~tag
                                          (.getName ^Class ~cls))
                                  (read/to-data v#)))))))
