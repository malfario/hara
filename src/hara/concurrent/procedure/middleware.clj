(ns hara.concurrent.procedure.middleware
  (:require [hara.common.state :as state]
            [hara.concurrent.procedure.registry :as registry]
            [hara.data.map :as map]
            [hara.time :as time]))

(defn wrap-instance [f]
  (fn [{:keys [arglist] :as instance} args]
    (let [args (map (fn [arg desc]
                      (if (= desc :instance)
                        instance
                        arg))
                    args
                    arglist)]
      (f instance args))))

(defn wrap-cached [f]
  (fn [{:keys [cached cache overwrite name id] :as instance} args]
    (if-not cached
      (f instance args)
      (let [prev (get-in @cache [name id args])]
        (if (and prev (not overwrite))
          (deliver (:result instance)
                   (assoc @(:result prev) :cached (-> prev :runtime deref :ended)))
          (let [current (f instance args)]
            (state/update cache assoc-in [name id args] instance)))))))

(defn wrap-interrupt [f]
  (fn [{:keys [registry name id interrupt] :as instance} args]
    (let [existing (get-in @registry [name id])]
      (cond (and interrupt existing)
            (do (registry/kill registry name id)
                (f instance args))

            existing
            existing

            :else
            (f instance args)))))

(defn wrap-timing [f]
  (fn [instance args]
    (swap! (:runtime instance) assoc :started (time/now))
    (f instance args)
    (swap! (:runtime instance) assoc :ended (time/now))))

(defn wrap-registry [f]
  (fn [{:keys [registry name id cached] :as instance} args]
    (state/update registry assoc-in [name id] instance)
    (f instance args)
    (state/update registry map/dissoc-in [name id])))

(defn wrap-mode [f]
  (fn [instance args]
    (let [instance  (update-in instance [:result]
                              (fn [result] (or result
                                               (promise))))]
      (case (:mode instance)
        :sync   (let [instance (assoc instance :thread (Thread/currentThread))]
                  (f instance args)
                  instance)
        :async  (let [thread (future (f instance args))]
                  (assoc instance :thread thread))))))

(defn wrap-id [f]
  (fn [instance args]
    (let [instance (update-in instance [:id]
                              (fn [id] (or id
                                           ((:id-fn instance) instance))))]
      (f instance args))))
