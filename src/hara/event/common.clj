(ns hara.event.common
  (:require [hara.common.checks :refer [hash-map?]]
            [hara.data.map :as map]
            [hara.data.seq :as seq]
            [hara.common.primitives :refer [uuid]]))

(defn new-id []
  (keyword (str (uuid))))

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


(defrecord Manager [id store options])

(defn manager
  ([] (Manager. (new-id) [] {}))
  ([id store options] (Manager. id store options)))

(defn remove-handler [manager id]
  (if-let [position (first (seq/positions #(-> % :id (= id)) (:store manager)))]
    (update-in manager [:store] seq/remove-index position)
    manager))

(defn add-handler
  ([manager handler]
   (let [handler (if (:id handler)
                   handler
                   (assoc handler :id (new-id)))]
     (-> manager
         (remove-handler (:id handler))
         (update-in [:store] conj handler))))
  ([manager checker handler]
   (let [handler (cond (fn? handler)
                       {:checker checker
                        :fn handler}

                       (map? handler)
                       (assoc handler :checker checker))]
     (add-handler manager handler))))

(defn list-handlers
  ([manager]
   (:store manager))
  ([manager checker]
   (->> (list-handlers manager)
        (filter #(check-data (:checker %) checker)))))

(defn match-handlers [manager data]
  (filter #(check-data data (:checker %))
          (:store manager)))

(defn handler-form [bindings body]
  (let [bind (cond (vector? bindings)    [{:keys bindings}]
                   (hash-map? bindings)  [bindings]
                   (symbol? bindings)    [bindings]
                   :else (throw (Exception. (str "bindings " bindings " should be a vector, hashmap or symbol"))))]
    `(fn ~bind ~@body)))

(-> (manager)
    (add-handler :from-cache
                 (fn [event]
                   (println event)))
    (add-handler [:from-cache]
                 {:id "hello"
                  :handler (fn [event]
                             (println event))})
    (remove-handler "hello")
    ;;(list-handlers)
    (match-handlers {:from-cache "true"})
    )

