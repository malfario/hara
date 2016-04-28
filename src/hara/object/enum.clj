(ns hara.object.enum
  (:require [hara.protocol.string :as string]
            [hara.protocol.object :as object]
            [hara.reflect :as reflect]
            [hara.class.inheritance :as inheritance]))

(defn enum?
  "Check to see if class is an enum type

   (enum? java.lang.annotation.ElementType) => true

   (enum? String) => false"
  {:added "2.2"}
  [type]
  (if (-> (inheritance/ancestor-list type)
          (set)
          (get java.lang.Enum))
    true false))

(defn enum-values
  "Returns all values of an enum type
   
   (->> (enum-values ElementType)
        (map str))
   => (contains \"TYPE\" \"FIELD\" \"METHOD\" \"PARAMETER\" \"CONSTRUCTOR\"
                :in-any-order :gaps-ok)"
  {:added "2.2"}
  [type]
  (let [vf (reflect/query-class type ["$VALUES" :#])]
    (->> (vf type) (seq))))

(defmethod object/-meta-read Enum
  [_]
  {:to-string string/-to-string})

(defmethod object/-meta-write Enum
  [_]
  {:from-string string/-from-string})

(extend-type Enum
  string/IString
  (-to-string
    [enum]
    (str enum)))

(defmethod string/-from-string Enum
  [data ^Class type]
  (if-let [field (reflect/query-class type [data :#])]
    (field type)
    (throw (Exception. (str "Options for " (.getName type) " are: "
                            (mapv str (enum-values type)))))))

(defmethod print-method Enum
  [v w]
  (.write w (format "#enum[%s %s]"
                    (.getName (class v))
                    (string/-to-string v))))
(comment

  (string/-from-string
   (string/-to-string
    java.lang.annotation.ElementType/FIELD)
   java.lang.annotation.ElementType))
