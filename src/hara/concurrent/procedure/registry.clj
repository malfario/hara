(ns hara.concurrent.procedure.registry
  (:require [hara.common.state :as state]
            [hara.common.checks :refer [thread?]]
            [hara.data.map :as map]
            [hara.concurrent.procedure.data :as data]
            [hara.event :as event]))

(defonce ^:dynamic *default-registry* (data/registry))

(defn list-instances
  ([name] (list-instances *default-registry* name))
  ([registry name]
   (vals (get @registry name))))

(defn get-instance
  ([name id] (get-instance *default-registry* name id))
  ([registry name id]
   (get-in @registry [name id])))

(defn kill
    ([name id] (kill *default-registry* name id))
    ([registry name id]
     (if-let [{:keys [thread] :as instance} (get-instance registry name id)]
       (do (cond (future? thread)
                 (future-cancel thread)

                 (and (thread? thread)
                      (not= thread (Thread/currentThread)))
                 (do (.stop ^Thread thread)
                     (Thread/sleep 1)))
           (event/signal [:log {:msg "Killed Execution" :instance instance}])
           (state/update registry map/dissoc-in [name id])
           true)
       false)))
