(ns hara.time.data.zone
  (:require [hara.time.data.common :as common])
  (:import [java.util Date TimeZone Calendar]))

(def by-offset
  (->> (reduce (fn [out ^String id]
                 (update-in out [(.getRawOffset (TimeZone/getTimeZone id))]
                            (fnil #(conj % id) #{})))
               {}
               (TimeZone/getAvailableIDs))
       (reduce-kv (fn [out k ids]
                    (let [id (or (first (filter (fn [^String id] (.startsWith id "Etc")) ids))
                                 (first (sort-by count ids)))]
                      (assoc out k id)))
                  {})))

(defn pad-zeros [s]
  (if (= 1 (count s))
    (str 0 s)
    s))

(defn generate-offsets []
  (for [i (range 0 12)
        j (range 0 60 15)]
    [(format "%s:%s"
             (pad-zeros (str i))
             (pad-zeros (str j)))
     (+ (* 3600000 i)
        (* 60000 j))]))

(def by-string-offset
  (let [half (generate-offsets)
        out  {}
        out  (reduce (fn [out [s val]]
                       (assoc out (str "+" s) (- val)))
                     out
                     half)
        out  (reduce (fn [out [s val]]
                       (assoc out (str "-" s) val))
                     out
                     half)]
    (assoc out "Z" 0)))


(comment
  
  ;; All the uncommon ones
  (->> (filter (fn [[_ d]]
               (not (.startsWith d "Etc"))) (seq by-offset))
     (map second)
     (map (juxt identity #(common/calendar (Date. 0) (TimeZone/getTimeZone %)))))
  (by-id "Asia/Kolkata"))



