(ns hara.data.diff
  (:require [hara.common.checks :refer [hash-map?]]
            [hara.data.nested :as nested]
            [hara.data.map :as map]))

(defn diff-changes
  "Finds changes in nested maps, does not consider new elements
  
  (diff-changes {:a 2} {:a 1})
  => {[:a] 2}

  (diff-changes {:a {:b 1 :c 2}} {:a {:b 1 :c 3}})
  => {[:a :c] 2}

  "
  {:added "2.1"}
  ([m1 m2]
   (diff-changes m1 m2 [] {}))
  ([m1 m2 arr output]
   (reduce-kv (fn [output k1 v1]
                (if-let [v2 (and (contains? m2 k1)
                                 (get m2 k1))]
                  (cond (and (hash-map? v1) (hash-map? v2))
                        (diff-changes v1 v2 (conj arr k1) output)
                        
                        (= v1 v2)
                        output

                        :else
                        (assoc output (conj arr k1) v1))
                  output))
              {}
              m1)))

(defn diff-new
  "Finds new elements in nested maps, does not consider changes
  
  (diff-new {:a 2} {:a 1})
  => {}

  (diff-new {:a {:b 1}} {:a {:c 2}})
  => {[:a :b] 1}

  "
  {:added "2.1"}
  ([m1 m2]
   (diff-new m1 m2 [] {}))
  ([m1 m2 arr output]
   (reduce-kv (fn [output k1 v1]
                 (let [v2 (get m2 k1)]
                   (cond (and (hash-map? v1) (hash-map? v2))
                         (diff-new v1 v2 (conj arr k1) output)

                         (not (contains? m2 k1))
                         (assoc output (conj arr k1) v1)

                         :else output)))
               {}
               m1)))

(defn diff
  "Finds the difference between two maps
  
  (diff {:a 2} {:a 1})
  => {:+ {} :- {} :> {[:a] 2} :< {[:a] 1}}

  (diff {:a {:b 1 :d 3}} {:a {:c 2 :d 4}})
  => {:+ {[:a :b] 1}
      :- {[:a :c] 2}
      :> {[:a :d] 3}
      :< {[:a :d] 4}}"
  {:added "2.1"}
  [m1 m2]
  (hash-map :+ (diff-new m1 m2)
            :- (diff-new m2 m1)
            :> (diff-changes m1 m2)
            :< (diff-changes m2 m1)))

(defn patch
  "Use the diff to convert one map to another in the forward 
  direction based upon changes between the two.
  
  (let [m1  {:a {:b 1 :d 3}}
        m2  {:a {:c 2 :d 4}}
        df  (diff m2 m1)]
    (patch m1 df)
    => m2)"
  {:added "2.1"}
  [m diff]
  (->> m
       (#(reduce-kv (fn [m arr v]
                       (assoc-in m arr v))
                    %
                    (merge (:+ diff) (:> diff))))
       (#(reduce (fn [m arr]
                   (map/dissoc-in m arr))
                    %
                    (keys (:- diff))))))

(defn unpatch
  "Use the diff to convert one map to another in the reverse 
  direction based upon changes between the two.
  
  (let [m1  {:a {:b 1 :d 3}}
        m2  {:a {:c 2 :d 4}}
        df  (diff m2 m1)]
    (unpatch m2 df)
    => m1)"
  {:added "2.1"}
  [m diff]
  (->> m
       (#(reduce-kv (fn [m arr v]
                       (assoc-in m arr v))
                    %
                    (merge (:- diff) (:< diff))))
       (#(reduce (fn [m arr]
                   (map/dissoc-in m arr))
                    %
                    (keys (:+ diff))))))

