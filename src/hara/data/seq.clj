(ns hara.data.seq)

(defn positions
  "find positions of elements matching the predicate
   (positions even? [5 5 4 4 3 3 2 2])
   => [2 3 6 7]"
  {:added "2.2"}
  [pred coll]
  (keep-indexed (fn [idx x]
                  (when (pred x)
                    idx))
                coll))

(defn remove-index
  "removes element at the specified index
   (remove-index [:a :b :c :d] 2)
   => [:a :b :d]"
  {:added "2.2"}
  [coll i]
  (cond (vector? coll)
        (reduce conj
                (subvec coll 0 i)
                (subvec coll (inc i) (count coll)))

        :else
        (keep-indexed #(if (not= %1 i) %2) coll)))

