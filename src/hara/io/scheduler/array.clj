(ns hara.io.scheduler.array
  (:require [hara.io.scheduler.tab :as tab]
            [hara.concurrent.ova :as ova]
            [hara.concurrent.procedure :as procedure]
            [hara.component :as component]))

(defrecord TaskArray []
  Object
  (toString [arr]
    (str (-> (into {} arr)
             (update-in [:handlers] deref)))))

(defmethod print-method TaskArray
  [v ^java.io.Writer w]
  (.write w (str v)))

(defn build-handlers [handlers config]
  (reduce-kv (fn [out k v]
               (let [procmap (cond (fn? v)
                                   {:name   k
                                    :handler v}

                                   (map? v)
                                   (assoc v :name k))
                     procmap (merge procmap (get config k))
                     _       (if-not (:schedule procmap)
                               (throw (Exception. (str "The schedule for task " k " does not exist"))))
                     procmap (assoc procmap
                                    :schedule-array (tab/parse-tab (:schedule procmap))
                                    :id-fn :timestamp)]
                 (conj out (procedure/procedure procmap [:timestamp :params :instance]))))
             []
             handlers))

(defn seed-fn [handlers]
  (fn [config]
    (map->TaskArray
     {:handlers (ova/ova (build-handlers handlers config))})))

(defn initialise [{:keys [handlers registry cache ticker] :as arr}]
  ;; watch for changes in ticker
  (add-watch ticker :trigger
             (fn [_ _ _ {:keys [time array params instance]}]
               (doseq [handler (persistent! handlers)]
                 (if (and (tab/match-array? array (:schedule-array handler))
                          (not (:disabled handler)))
                   (handler time params instance)))))
  ;; update registry
  (dosync (ova/map! handlers assoc-in [:registry] registry)
          (ova/map! handlers assoc-in [:cache] cache))
  
  arr)
