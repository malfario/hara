(ns hara.object.print
  (:require [hara.protocol.object :as object]
            [hara.object.read :as read]))

(defn assoc-print-vars [read {:keys [tag print display]}]
  (cond-> read
    tag   (assoc :tag tag)
    print (assoc :print print)
    display (assoc :display display)))

(defn format-value [v {:keys [tag print display]}]
  (str "#" (or tag (.getName ^Class (type v)))
       (let [out (if print
                   (print v)
                   (cond-> (read/to-data v)
                     display display))]
         (if (string? out)
           (str " \"" out "\"")
           out))))

(defmacro extend-print [cls]
  `(defmethod print-method ~cls
     [~'v ^java.io.Writer w#]
     (let [read# (object/-meta-read ~cls)]
       (.write w# (format-value ~'v read#)))))
