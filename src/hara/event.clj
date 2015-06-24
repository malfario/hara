(ns hara.event
  (:require [hara.common.checks :refer [hash-map?]]
            [hara.data.map :as map]
            [hara.common.primitives :refer [uuid]]))

(defn expand-data [data]
  (cond (hash-map? data) data
        (keyword? data) {data true}
        (vector? data)  (apply merge (map expand-data data))
        :else (throw (Exception. (str data " should be a keyword, hash-map or vector")))))

(defn- check-data [data chk]
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

        (hash-set? chk)
        (some #(check-data data %) chk)

        (= '_ chk) true
        
        :else 
        (error "CHECK_DATA: " chk " cannot be found")))

(defrecord Manager [index store])

(defn manager [m] (map->Manager m))

(defonce ^:dynamic *default-manager* (manager {}))

(defn attach )


(comment
  (attach-trigger [:has-data]
          (fn [event]
            (println event)))

  (detach-trigger "oeuoeuoeuoeuoeu")

  (list-triggers [:has-data])


  )

(defonce ^:dynamic *triggers* (atom {}))

(defonce ^:dynamic *trigger-index* (atom {}))



(defn attach-listener [key handler]
  (swap! *global* assoc key handler))

(defn detach-listener [key]
  (swap! *global* dissoc key))

(defn list-listeners []
  (keys *global*))

(def event-types #{:catch :choose :common})

(defrecord Event [type target value])

(defn sig
  ([value]
   (sig :common nil value))
  ([type target value]
   {::type type ::target target ::value value}))

(defn signal [value])

