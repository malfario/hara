(ns hara.time.data.coerce
  (:require [hara.protocol
             [time :as time]
             [string :as string]]
            [hara.time.data
             [common :as common]]))

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
        (time/-timezone (or common/*default-timezone*
                            (common/local-timezone))
                        {:type type})
    
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
  (cond (or (= type Long) (nil? type))
        (time/-to-long value)

        (instance? Long value)
        (time/-from-long value opts)
        
        :else
        (-> value
            (time/-to-long)
            (time/-from-long opts))))

(defn coerce
  "general coercion function
   (coerce (Date. 0) {:type Long})
   => 0
 
   (-> (coerce \"Asia/Kolkata\" {:type TimeZone})
       (string/-to-string))
   => \"Asia/Kolkata\""
  {:added "2.2"}
  ([value {:keys [type] :as opts}]
   (cond (= (class value) type)
         value

         (instance? type value)
         value

         :else
         (let [base (:base (time/-time-meta type))]
           (case base
             :instant (coerce-instant value opts)
             :zone    (coerce-zone value opts))))))
