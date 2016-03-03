(ns hara.time.data.vector
  (:require [hara.protocol
             [time :as time]]
            [hara.time.data
             [common :as common]]))

(defn to-vector
  "converts an instant to an array representation
   (to-vector 0 {:timezone \"GMT\"})
   => [1970 1 1 0 0 0 0]
 
   (to-vector (Date. 0) {:timezone \"GMT\"} :day)
   => [1970 1 1]
 
   (to-vector (Date. 0) {:timezone \"GMT\"} [:month :day :year])
   => [1 1 1970]"
  {:added "2.2"}
  ([t]
   (to-vector t nil))
  ([t opts]
   (to-vector t opts :all))
  ([t opts ks]
   (let [tmeta (-> (class t)
                   (time/-time-meta)
                   :rep
                   :to)
         [p pmeta] (common/to-proxy t opts tmeta) 
         ks   (cond (vector? ks) ks

                    (= :all ks)
                    (reverse common/+default-keys+)
                    
                    (keyword? ks)
                    (->> common/+default-keys+
                         (drop-while #(not= % ks))
                         (reverse)))
         rep  (reduce (fn [out k]
                        (let [t-fn (get-in pmeta [:fn k])]
                          (conj out (t-fn p opts))))
                      []
                      ks)]
     rep)))
