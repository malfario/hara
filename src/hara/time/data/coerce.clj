(ns hara.time.data.coerce
  (:require [hara.protocol
             [time :as time]
             [string :as string]]
            [hara.time.data
             [common :as common]
             [map :as map]]))

(defn coerce-zone
  "coercion of one zone object to another
   (-> (coerce-zone \"Asia/Kolkata\" {:type TimeZone})
       (string/-to-string))
   => \"Asia/Kolkata\"
 
   (-> (coerce-zone nil {:type TimeZone})
       (string/-to-string))
   => (-> (TimeZone/getDefault)
          (string/-to-string))"
  {:added "2.2"}
  [value {:keys [type] :as opts}]
  (cond (nil? value)
        (string/-from-string (common/default-timezone)
                             type)
        
        (instance? type value)
        value
        
        (string? value)
        (string/-from-string value type)
        
        :else
        (->  value
             (string/-to-string)
             (string/-from-string type))))

(defn coerce-instant
  "coercion of one instant object to another
   (-> ^Calendar (coerce-instant 0 {:type Calendar})
       (.getTime)
       (.getTime))
   => 0"
  {:added "2.2"}
  [value {:keys [type] :as opts}]
  (cond (instance? type value)
        value
    
        (or (= type Long) (nil? type))
        (time/-to-long value)

        (instance? Long value)
        (time/-from-long value opts)

        (instance? clojure.lang.APersistentMap value)
        (map/from-map value opts)
        
        :else
        (-> value
            (time/-to-long)
            (time/-from-long opts))))
