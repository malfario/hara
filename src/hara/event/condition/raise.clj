(ns hara.event.condition.raise
  (:require [hara.data.map :as map]
            [hara.event.common :as common]
            [hara.event.condition.data :as data]))

(defn default-unhandled-fn [issue]
  (let [ex (data/exception issue)]
    (throw ex)))

(declare raise-loop)

(defn raise-catch [manager value]
  (throw (data/catch-condition (:id manager) value)))

(defn raise-choose [issue label args optmap]
  (let [target (get optmap label)]
    (cond (nil? target)
          (throw (Exception. (str "RAISE_CHOOSE: the label " label
                                  " has not been implemented")))
          
          (= target (:id issue))
          (try
            (apply (-> issue :options label) args)
            (catch clojure.lang.ArityException e
              (throw (Exception.
                      (str "RAISE_CHOOSE: Wrong number of arguments to option key " label)))))

          :else
          (throw (data/choose-condition target label args)))))

(defn- raise-unhandled [issue optmap]
  (if-let [[label & args] (:default issue)]
    (raise-choose issue label args optmap)
    (default-unhandled-fn issue)))

(defn raise-fail [issue data]
  (throw (data/exception issue (common/expand-data data))))

(defn- raise-escalate [issue res managers optmap]
  (let [ndata     (common/expand-data (:data res))
        noptions  (:options res)
        noptmap   (zipmap (keys noptions) (repeat (:id issue)))
        ndefault  (:default res)
        nissue (-> issue
                   (update-in [:data] merge ndata)
                   (update-in [:options] merge noptions)
                   (map/assoc-if :default ndefault))]
    (raise-loop nissue (next managers) (merge noptmap optmap))))

(defn raise-loop [issue [manager & more :as managers] optmap]
  (if manager
    (if-let [handler (first (common/match-handlers manager (:data issue)))]
      (let [data (:data issue)
            result  ((:fn handler) data)]
        (condp = (:type result)
          :continue   (:value result)
          :choose     (raise-choose issue (:label result) (:args result) optmap)
          :default    (raise-unhandled issue optmap)
          :fail       (raise-fail issue (:data result))
          :escalate   (raise-escalate issue result managers optmap)
          (raise-catch manager result)))
      (recur issue more optmap))
    (raise-unhandled issue optmap)))
