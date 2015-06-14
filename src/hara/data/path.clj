(ns hara.data.path
  (:require [clojure.set :as set]
            [hara.common.checks :refer [hash-map?]]
            [hara.data.nested :as nested]
            [hara.data.map :as map]
            [hara.string.path :as path]))

(defn list-ns-keys
  "Returns the set of keyword namespaces within a map

  (list-ns-keys {:hello/a 1 :hello/b 2
                 :there/a 3 :there/b 4})
  => #{:hello :there}"
  {:added "2.1"}
  [fm]
  (let [ks (keys fm)]
    (set (map path/path-ns ks))))

(defn contains-ns-key?
  "Returns `true` if any key in map contains a namespace value

  (contains-ns-key? {:hello/a 1 :hello/b 2
                     :there/a 3 :there/b 4} :hello)
  => true"
  {:added "2.1"}
  [fm ns]
  (some #(path/path-ns? % ns) (keys fm)))

(defn group-by-set
  "Returns a map of the elements of coll keyed by the result of
  f on each element. The value at each key will be a set of the
  corresponding elements, in the order they appeared in coll.

  (group-by-set even? [1 2 3 4 5])
  => {false #{1 3 5}, true #{2 4}} "
  {:added "2.1"}
  [f coll]
  (persistent!
   (reduce
    (fn [ret x]
      (let [k (f x)]
        (assoc! ret k (conj (get ret k #{}) x))))
    (transient {}) coll)))

(defn group-keys
  "Returns the set of keys in `fm` that has keyword namespace
  of `ns`
  (group-keys {:hello/a 1 :hello/b 2
               :there/a 3 :there/b 4})
  => {:there #{:there/a :there/b}, :hello #{:hello/b :hello/a}}

  (group-keys {:hello/a 1 :hello/b 2
               :there/a 3 :there/b 4} :hello)
  => #{:hello/a :hello/b}"
  {:added "2.1"}
  ([fm] (let [ks (keys fm)]
          (group-by-set #(path/path-ns %) ks)))
  ([fm ns]
     (let [ks (keys fm)]
       (->> ks
            (filter #(= ns (path/path-ns %)))
            set))))

(defn flatten-keys
  "takes map `m` and flattens the first nested layer onto the root layer.

  (flatten-keys {:a {:b 2 :c 3} :e 4})
  => {:a/b 2 :a/c 3 :e 4}

  (flatten-keys {:a {:b {:c 3 :d 4}
                     :e {:f 5 :g 6}}
                 :h {:i 7}
                 :j 8})
  => {:a/b {:c 3 :d 4} :a/e {:f 5 :g 6} :h/i 7 :j 8}"
  {:added "2.1"}
  ([m]
   (reduce-kv (fn [m k v]
                (if (hash-map? v)
                  (reduce-kv (fn [m sk sv]
                               (assoc m (path/join [k sk]) sv))
                             m
                             v)
                  (assoc m k v)))
              {}
              m)))

(defn pathify-keys-nested
  ([m] (pathify-keys-nested m -1 false []))
  ([m max] (pathify-keys-nested m max false []))
  ([m max keep-empty] (pathify-keys-nested m max keep-empty []))
  ([m max keep-empty arr]
   (reduce-kv (fn [m k v]
                (if (or (and (not (> 0 max))
                             (<= max 1))
                        (not (hash-map? v))
                        (and keep-empty
                             (empty? v)))
                  (assoc m (conj arr k) v)
                  (merge m (pathify-keys-nested v (dec max) keep-empty (conj arr k)))))
              {}
              m)))

(defn flatten-keys-nested
  "Returns a single associative map with all of the nested
   keys of `m` flattened. If `keep` is added, it preserves all the
   empty sets

  (flatten-keys-nested {\"a\" {\"b\" {\"c\" 3 \"d\" 4}
                               \"e\" {\"f\" 5 \"g\" 6}}
                          \"h\" {\"i\" {}}})
  => {\"a/b/c\" 3 \"a/b/d\" 4 \"a/e/f\" 5 \"a/e/g\" 6}

  (flatten-keys-nested {\"a\" {\"b\" {\"c\" 3 \"d\" 4}
                               \"e\" {\"f\" 5 \"g\" 6}}
                          \"h\" {\"i\" {}}}
                       -1 true)
  => {\"a/b/c\" 3 \"a/b/d\" 4 \"a/e/f\" 5 \"a/e/g\" 6 \"h/i\" {}}"
  {:added "2.1"}
  ([m] (flatten-keys-nested m -1 false))
  ([m max keep-empty]
   (-> (pathify-keys-nested m max keep-empty)
       (nested/update-keys-in [] path/join))))

(defn treeify-keys
  "Returns a nested map, expanding out the first
   level of keys into additional hash-maps.

  (treeify-keys {:a/b 2 :a/c 3})
  => {:a {:b 2 :c 3}}

  (treeify-keys {:a/b {:e/f 1} :a/c {:g/h 1}})
  => {:a {:b {:e/f 1}
          :c {:g/h 1}}}"
  {:added "2.1"}
  [m]
  (reduce-kv (fn [m k v]
               (assoc-in m (path/split k) v))
             {}
             m))

(defn treeify-keys-nested
  "Returns a nested map, expanding out all
 levels of keys into additional hash-maps.

  (treeify-keys-nested {:a/b 2 :a/c 3})
  => {:a {:b 2 :c 3}}

  (treeify-keys-nested {:a/b {:e/f 1} :a/c {:g/h 1}})
  => {:a {:b {:e {:f 1}}
          :c {:g {:h 1}}}}"
  {:added "2.1"}
  [m]
  (reduce-kv (fn [m k v]
               (if (hash-map? v)
                 (update-in m (path/split k) nested/merge-nested (treeify-keys-nested v))
                 (assoc-in m (path/split k) v)))
             {}
             m))

(defn nest-keys
  "Returns a map that takes `m` and extends all keys with the
  `nskv` vector. `ex` is the list of keys that are not extended.

  (nest-keys {:a 1 :b 2} [:hello :there])
   => {:hello {:there {:a 1 :b 2}}}

   (nest-keys {:there 1 :b 2} [:hello] [:there])
   => {:hello {:b 2} :there 1}"
  {:added "2.1"}
  ([m nskv] (nest-keys m nskv []))
  ([m nskv ex]
    (let [e-map (select-keys m ex)
          x-map (apply dissoc m ex)]
      (merge e-map (if (empty? nskv)
                     x-map
                     (assoc-in {} nskv x-map))))))

(defn unnest-keys
  "The reverse of `nest-keys`. Takes `m` and returns a map
  with all keys with a `keyword-nsvec` of `nskv` being 'unnested'

  (unnest-keys {:hello/a 1
                :hello/b 2
                :there/a 3
                :there/b 4} [:hello])
  => {:a 1 :b 2
      :there {:a 3 :b 4}}

  (unnest-keys {:hello {:there {:a 1 :b 2}}
                :again {:c 3 :d 4}} [:hello :there] [:+] )
  => {:a 1 :b 2
      :+ {:again {:c 3 :d 4}}}"
  {:added "2.1"}
  ([m nskv] (unnest-keys m nskv []))
  ([m nskv ex]
   (let [tm     (treeify-keys-nested m)
         c-map  (get-in tm nskv)
         x-map  (map/dissoc-in tm nskv)]
    (merge c-map (if (empty? ex)
                   x-map
                   (assoc-in {} ex x-map))))))
