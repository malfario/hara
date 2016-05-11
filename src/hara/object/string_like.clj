(ns hara.object.string-like
  (:require [hara.protocol.string :as string]
            [hara.protocol.object :as object]
            [hara.object.print :as print]))

(defmacro extend-string-like
  "creates an entry for string-like classes
 
   (extend-string-like
    java.io.File
    {:tag \"path\"
     :read .getPath
     :write (fn [^String path _] (java.io.File. path))})
 
   (with-out-str
     (prn (java.io.File. \"/home\")))
   => \"#path \"/home\"\""
  {:added "2.3"} 
  [cls {:keys [read write meta] :as opts}]
  `(vector
    (defmethod object/-meta-read ~cls
      [~'_]
      ~(-> {:to-string `string/-to-string}
           (print/assoc-print-vars opts)))

    (defmethod object/-meta-write ~cls
      [~'_]
      {:from-string (fn [s#] (string/-from-string s# ~cls))})

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
          [data# ~'_]
          (~write data#))
       `(defmethod string/-from-string ~cls
          [data# type#]
          (throw (Exception. (str "Cannot create " type# " from string.")))))
    
    (print/extend-print ~cls)))
