(ns hara.concurrent.procedure.retry
  (:require [hara.data.map :as map]
            [hara.event.common :as event]
            [hara.expression.shorthand :as shorthand])
  (:import clojure.lang.ExceptionInfo))

(defn retry-wait
  [{:keys [wait state count] :as handler}] 
  (let [wait (cond (nil? wait) 0

                   (number? wait) wait
                   
                   (fn? wait)
                   (wait state count))]
    (if (pos? wait)
      (Thread/sleep wait))
    wait))

(defn retry-pick
  [{:keys [on] :as handler} e]
  (cond (class? on)
        (instance? on e)

        (set? on)
        (->> on
             (map #(retry-pick (assoc handler :on %) e))
             (some true?))

        (or (fn? on) (map? on) (keyword? on) (vector? on) (= '_ on))
        (and (instance? ExceptionInfo e)
             (event/check-data (ex-data e) on))))

(defn retry-args
  [args arglist retry instance]
  (map (fn [arg type]
         (cond (= type :retry)
               retry
               
               (= type :instance)
               instance

               :else arg))
       args
       arglist))

(defn retry-check
  [{:keys [limit state count] :as handler}]
  (cond (number? limit)
        (> limit count)
        
        (fn? limit)
        (limit state count)

        (= :none limit)
        true))

(defn retry-state [{:keys [apply state] :as handler} e]
  (cond (nil? apply)
        state

        (fn? apply)
        (apply state e)

        :else state))

(defn retry
  [{:keys [retry arglist] :as instance} args e]
  (let [handlers  (:handlers retry)
        default  (-> retry
                     (dissoc :handlers)
                     (map/merge-nil {:on Throwable}))
        _        (println handlers)
        handler  (->> (conj handlers default)
                      (filter #(retry-pick % e))
                      (first))
        handler  (if handler
                   (map/merge-nil handler default))]
    (if handler      
      (let [_       (retry-wait handler)
            nstate  (retry-state handler e)
            nretry  (-> retry
                        (assoc :state nstate)
                        (update-in [:count] (fnil inc 0)))]
        (retry-args args arglist nretry (assoc instance :retry nretry))))))

(comment
  "Sample `:retry` options:"
  
  {:retry {:handlers [{:on #{Exception}
                       :apply   (fn [state e])
                      :limit   (fn [state count])
                       :wait    (fn [state count])}
                      {:on Error
                       :apply (fn [state e])
                       :limit :no}
                      {:on (fn [e] (instance? Throwable e))
                       :apply (fn [state e])
                       :limit :no}]
           :on Throwable
           :count 0
           :state  {:a 1 :b 2}
           :limit 10
           :wait  100}})
