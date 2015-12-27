(ns hara.sort.hierarchical
  (:require [clojure.set :as set]))

(defn top-node
  "find the top node for the hierarchy of descendants
   (top-node {1 #{2 3 4 5 6}
              2 #{3 5 6}
              3 #{5 6}
              4 #{}
              5 #{6} 
              6 #{}})
   => 1"
  {:added "2.2"}
  [idx]
  (let [rest (apply set/union (vals idx))]
    (ffirst (filter (fn [[k v]]
                     (not-empty (set/difference (conj v k) rest)))
                    idx))))

(defn hierarchical-sort
  "prunes a hierarchy of descendants into a directed graph
   (hierarchical-sort {1 #{2 3 4 5 6}
                       2 #{3 5 6}
                       3 #{5 6}
                       4 #{}
                       5 #{6} 
                       6 #{}})
   => {1 #{4 2}
      2 #{3}
       3 #{5}
       4 #{}
       5 #{6}
       6 #{} }"
  {:added "2.2"}
  [idx]
  (let [top (top-node idx)]
    (loop [out {}
          candidates (dissoc idx top)
           level #{top}]
      (if (empty? level)
        out
        (let [base  (apply set/union (vals candidates))
              out   (reduce (fn [out i]
                              (assoc out i (set/difference (get idx i) base)))
                            out
                            level)
              nlevel (mapcat #(get out %) level)
              ncandidates (apply dissoc idx (concat (keys out) nlevel))]
          (recur out
                 ncandidates
                 nlevel))))))
