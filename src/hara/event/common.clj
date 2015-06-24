(ns hara.event.common
  (:require [hara.common.checks :refer [hash-map?]]
            [hara.data.map :as map]
            [hara.common.primitives :refer [uuid]]))

(defn expand-data [data]
  (cond (hash-map? data) data
        (keyword? data) {data true}
        (vector? data)  (apply merge (map expand-data data))
        :else (throw (Exception. (str data " should be a keyword, hash-map or vector")))))

(defn check-data [data chk]
  (cond (hash-map? chk)
        (every? (fn [[k vchk]]
                  (let [vcnt (get data k)]
                    (cond (keyword? vchk) (= vchk vcnt)
                          (fn? vchk) (vchk vcnt)
                          :else (= vchk vcnt))))
                chk)

        (vector? chk)
        (every? #(check-data data %) chk)

        (or (fn? chk) (keyword? chk))
        (chk data)
        
        (set? chk)
        (some #(check-data data %) chk)
        
        (= '_ chk) true
        
        :else
        (throw (Exception. (str chk " is not a valid checker")))))


(defrecord Manager [index store])

(defn manager [] (Manager. {} {}))

(defn add-handler [manager checker handler]
  (let [checker (expand-data checker)
        [id trigger]  (cond (fn? handler)
                            (let [id (str (uuid))]
                              [id
                               {:id id
                                :checker checker
                                :handler handler}])
                              
                            (map? handler)
                            [(:id handler)
                             (assoc handler :checker checker)])     
        ks   (keys checker)
        manager (assoc-in manager [:store id] trigger)]
    (reduce (fn [m k]
              (update-in m [:index k] (fnil #(conj % id) #{})))
            manager
            ks)))

(defn list-handlers
  ([manager]
   (->> (vals (:store manager))
        (map #(dissoc % :handler))))
  ([manager checker]
   (->> (list-handlers manager)
        (filter #(check-data (:checker %) checker)))))

(defn remove-handler [manager id]
  (if-let [handler (get-in manager [:store id])]
    (let [ks (-> handler :checker keys)
          manager (update-in manager [:store] dissoc id)]
      (reduce (fn [m k]
                (update-in m [:index k] disj id))
              manager
              ks))
    manager))

(defn match-handlers [manager data]
  (reduce-kv (fn [output id trigger]
               (if (check-data data (:checker trigger))
                 (conj output trigger)
                 output))
             []
             (:store manager)))

(-> (manager)
    (add-handler [:from-cache]
                 (fn [event]
                   (println event)))
    (add-handler [:from-cache]
                 {:id "hello"
                  :handler (fn [event]
                             (println event))})
    (remove-handler "hello")
    (match-handlers {:from-cache true}))

