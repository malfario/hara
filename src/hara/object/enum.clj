(ns hara.object.enum
  (:require [hara.protocol.string :as string]
            [hara.protocol.data :as data]
            [hara.reflect :as reflect]
            
            [hara.class.inheritance :as inheritance]))

(defn enum? [type]
  (if (-> (inheritance/ancestor-list type)
          (set)
          (get java.lang.Enum))
    true false))

(defn enum-values [type]
  (let [vf (reflect/query-class type ["$VALUES" :#])]
    (->> (vf type) (seq))))

(defmethod data/-meta-object Enum
  [type]
  {:class java.lang.Enum
   :types #{String}
   :to-data string/-to-string
   :from-data string/-from-string})

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
