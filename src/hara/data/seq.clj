(ns hara.data.seq)

(defn positions
  [pred coll]
  (keep-indexed (fn [idx x]
                  (when (pred x)
                    idx))
                coll))

(defn remove-index
  [coll i]
  (cond (vector? coll)
        (persistent!
         (reduce conj!
                 (transient (vec (subvec coll 0 i)))
                 (subvec coll (inc i) (count coll))))

        :else
        (keep-indexed #(if (not= %1 i) %2) coll)))
