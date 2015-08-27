(ns hara.concurrent.procedure.retry
  (:require [hara.data.map :as map]
            [hara.event.common :as event]
            [hara.expression.shorthand :as shorthand])
  (:import clojure.lang.ExceptionInfo))

(defn retry-wait
  "waits in milliseconds depending upon the handler
 
   (retry-wait {}) => 0
 
   (retry-wait {:wait 100}) => 100
 
   (retry-wait {:wait (fn [state count]
                        (if (> (:expiry state) count)
                          1000
                          0))
                :state {:expiry 4}
                :count 5})
   => 0"
  {:added "2.2"}
  [{:keys [wait state count] :as handler}] 
  (let [wait (cond (nil? wait) 0

                   (number? wait) wait
                   
                   (fn? wait)
                   (wait state count))]
    (if (pos? wait)
      (Thread/sleep wait))
    wait))

(defn retry-pick
  "chooses the exception handler based upon the exception
   (retry-pick {:on #{Exception}} (Exception.))
   => true
 
   (retry-pick {:on Exception} (Exception.))
   => true
 
   (retry-pick {:on {:a #(= :error %)}} (ex-info \"error\" {:a :error}))
   => true"
  {:added "2.2"}
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
  "injects new args based on the instance arglist
   (retry-args [1 2 {}]
               [:a :b :retry]
               {:wait 5}
               {})
   => '(1 2 {:wait 5})"
  {:added "2.2"}
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
  "checks to see if a retry is needed
 
   (retry-check {:limit 2 :count 3})
   => false
 
   (retry-check {:limit (fn [state count]
                          (> (/ (inc count) (:restarted state))
                             2))
                 :state {:restarted 3}
                 :count 8})
   => true"
  {:added "2.2"}
  [{:keys [limit state count] :as handler}]
  (cond (number? limit)
        (> limit count)
        
        (fn? limit)
        (limit state count)

        (= :none limit)
        true))

(defn retry-state
  "calculates the next retry state
 
   (retry-state {:apply (fn [state e]
                          (update-in state [:file-errors] (fnil inc 0)))
                 :state {}}
                (Exception.))
   => {:file-errors 1}"
  {:added "2.2"} [{:keys [apply state] :as handler} e]
  (cond (nil? apply)
        state

        (fn? apply)
        (apply state e)

        :else state))

(defn retry
  "sets up arguments if retrying. if no retry, returns nil
 
   (retry {:arglist [:age :gender :retry]
               :retry {:handlers [{:on {:cats odd?}
                                   :apply   (fn [state e]
                                              (update-in state [:types (type e)] (fnil inc 0)))
                                   :wait    (fn [state count]
                                              0)}]
                      :on Throwable
                       :count 0
                       :state  {:a 1 :b 2}
                       :limit 10
                       :wait  100}}
              [:a :b {}]
              (ex-info \"hello\" {:cats 11}))
   => (contains [:a :b (contains
                        {:handlers
                         (contains
                          [(contains {:on {:cats odd?}, :apply fn?, :wait fn?})])
                         :on java.lang.Throwable,
                         :count 1,
                         :state {:types {clojure.lang.ExceptionInfo 1},
                                 :b 2,
                                 :a 1},
                         :limit 10,
                         :wait 100})])"
  {:added "2.2"}
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
