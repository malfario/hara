(ns hara.concurrent.procedure.retry
  (:require [hara.data.map :as map]))

(defn retry-wait
  [{:keys [wait state count] :as handler}] 
  (let [wait (cond (nil? wait) 0

                   (number? wait) wait
                   
                   (fn? wait)
                   (wait state count))]
    (if (pos? wait)
      (Thread/sleep wait))
    wait))

(defn retry-handler
  [{:keys [on] :as handler} e]
  (cond (set? on)
        (->> on
             (map #(retry-handler (assoc handler :on %) e))
             (some true?))

        (class? on)
        (instance? on e)

        (fn? on)
        (on e)))

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
        (= limit count)
        
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
  (let [handles  (:handles retry)
        default  (dissoc retry :handles)
        handler  (->> (conj handles default)
                      (filter #(retry-handler % e))
                      (first))]
    (if (and handler (retry-check handler))
      (let [handler (map/merge-nil handler default)
            _       (retry-wait handler)
            nstate  (retry-state handler)
            nretry  (-> retry
                        (assoc :state nstate)
                        (update-in [:count] (fnil inc 0)))]
        (retry-args args arglist nretry (assoc instance :retry nretry))))))


(comment
  {:retry {:handles [{:on #{Exception}
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
