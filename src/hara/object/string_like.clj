(ns hara.object.string-like
  (:require [hara.protocol.string :as string]
            [hara.protocol.object :as object]))

(defmacro extend-string-like [cls {:keys [tag read write meta] :as opts}]
  `(vector
    (defmethod object/-meta-read ~cls
      [~'_]
      {:to-string string/-to-string})

    (defmethod object/-meta-write ~cls
      [~'_]
      {:from-string string/-from-string})

    (extend-protocol string/IString
      ~cls
      (-to-string [obj#]
        (~(or read `str) obj#))

      (-to-string-meta [obj#]
        ~(if meta
           (list meta `obj#)
           {:class cls})))

    ~(if write
       `(defmethod string/-from-string ~cls
          [data# type#]
          (~write data# type#))
       `(defmethod string/-from-string ~cls
          [data# type#]
          (throw (Exception. (str "Cannot create " type# " from string.")))))

    (defmethod print-method ~cls
      [v# ^java.io.Writer w#]
      (.write w# (str "#" (or ~tag
                              (.getName ~cls))
                      " \"" (string/-to-string v#) "\"")))))
