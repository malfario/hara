(ns hara.data.transform
  (:require [hara.data.nested :as nested]
            [hara.data.map :as map]
            [clojure.string :as string]))

(defn template-rel
  [v]
  (->> (map name v)
       (string/join "/")
       (format "<%s>")
       keyword))

(defn forward-rel
  ([trans] (forward-rel trans [] {}))
  ([trans kv out]
   (reduce-kv (fn [out k v]
                (let [nkv (conj kv k)]
                  (cond (map? v)
                        (forward-rel v nkv out)

                        :else
                        (assoc-in out nkv (template-rel nkv)))))
              out
              trans)))

(defn backward-rel
  ([trans] (backward-rel trans [] {}))
  ([trans kv out]
   (reduce-kv (fn [out k v]
                (let [nkv (conj kv k)]
                  (cond (map? v)
                        (backward-rel v nkv out)

                        :else
                        (assoc-in out v (template-rel nkv)))))
              out
              trans)))

(defn full-rel
  [trans]
  [(forward-rel trans)
   (backward-rel trans)])

(defn collect-paths
  ([m] (collect-paths m [] {}))
  ([m kv out]
   (reduce-kv (fn [out k v]
                (cond (map? v)
                      (collect-paths v (conj kv k) out)

                      :else
                      (assoc out v (conj kv k))))
              out
              m)))

(defn collect-rel [rel]
  (->> (map collect-paths rel)
       (apply merge-with vector)))

(collect-rel
 (full-rel {:authority {:username [:user]
                        :password [:pass]}}))
{:<authority/username> [[:authority :username] [:user]], :<authority/password> [[:authority :password] [:pass]]}

(defn apply-rel [m rel]
  (reduce-kv (fn [out k [to from]]
               (let [v (get-in m from)]
                 (-> out
                     (assoc-in to v)
                     (map/dissoc-in from))))
             m
             rel))

(defn retract-rel [m rel]
  (reduce-kv (fn [out k [to from]]
               (let [v (get-in m to)]
                 (-> out
                     (assoc-in from v)
                     (map/dissoc-in to))))
             m
             rel))

(comment

  (->> {:authority {:username [:user]
                    :password [:pass]}}
       (full-rel)
       (collect-rel)
       (apply-rel {:user "chris" :pass "hello"})
       )
  (->> {:authority {:username [:user]
                    :password [:pass]}}
       (full-rel)
       (collect-rel)
       (retract-rel {:authority {:username "chris"
                                 :password "hello"}}))

  [(forward-rel
    {:authority {:username [:user]
                 :password [:pass]}})
   (backward-rel
    {:authority {:username [:user]
                 :password [:pass]}})]
  => [{:authority {:username :<authority/username>
                   :password :<authority/password>}}
      {:user :<authority/username>
       :pass :<authority/password>}]

  [(forward-rel
    {:user [:authority :username]
     :pass [:authority :password]})
   (backward-rel
    {:user [:authority :username]
     :pass [:authority :password]})]
  => [{:user :<user>
       :pass :<pass>}
      {:authority {:username :<user>
                   :password :<pass>}}]


  (def rel [{:user :<user>}
            {:username :<user>}])

  {:<user> [[:user]
            [:username]]}

  (defn forward [rel]
    (fn [m]
      ))

  

  (defn collect [rel]
    (->> (map collect-paths rel)
         (apply merge-with vector)))

  (merge-with vector
              (collect-paths forward)
              (collect-paths ))

  (vals (merge-with vector
                    (collect-paths {:user :<user>})
                    (collect-paths {:username :<user>})))

  )
