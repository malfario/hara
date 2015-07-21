(ns hara.object.string-like
  (:require [hara.protocol.string :as string]
            [hara.object.meta :as meta]))

(defmacro extend-stringlike-class [cls {:keys [meta to from] :as opts}]
  `(vector
    (defmethod meta/-meta-object ~cls
      [type#]
      {:class     type#
       :types     #{String}
       :to-data   string/-to-string
       :from-data string/-from-string})
    
    (extend-protocol string/IString
      ~cls
      (-to-string [obj#]
        (~(or to `str) obj#))
      
      (-to-string-meta [obj#]
        ~(if meta
           (list meta `obj#)
           {:class cls})))

    ~(if from
       `(defmethod string/-from-string ~cls
          [data# type#]
          (~from data# type#))
       `(defmethod string/-from-string ~cls
          [data# type#]
          (throw (Exception. (str "Cannot create " type# " from string.")))))

    (defmethod print-method ~cls
      [v# ^java.io.Writer w#]
      (.write w# (str "#" ~(or (:tag opts) cls) "::" (string/-to-string v#))))))
