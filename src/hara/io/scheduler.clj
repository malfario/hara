(ns hara.io.scheduler
  (:require [hara
             [time :as time]
             [component :as component]]
            [hara.concurrent
             [ova :as ova]
             [procedure :as procedure]]
            [hara.concurrent.procedure.data :as data]
            [hara.data.nested :as nested]
            [hara.io.scheduler
             [array :as array]
             [clock :as clock]
             [tab :as tab]]))

(defonce ^:dynamic *defaults*
  {:clock    {:type     "java.util.Date"
              :region   (time/system-timezone)
              :interval 1
              :truncate :milli}
   :registry {}
   :cache    {}
   :ticker   {}})

(defn scheduler
  "creates a schedular from handlers, or both handlers and config"
  {:added "2.2"}
  ([handlers] (scheduler handlers {}))
    ([handlers config] (scheduler handlers config {}))
    ([handlers config global]
     (component/system
      {:array     [{:constructor (array/seed-fn handlers)
                    :initialiser array/initialise} :cache :registry :ticker]
       :clock     [clock/clock :ticker]
       :ticker    [(fn [_] (atom {:time nil :array nil}))]
       :registry  [(fn [_] (data/registry))]
       :cache     [(fn [_] (data/cache))]}
      (-> global
          (update-in [:array] nested/merge-nested config)
          (nested/merge-nil-nested *defaults*))
      "scheduler")))

(defn create
  "function for use with the component framework."
  {:added "2.2"}
  [handlers]
  (fn [config]
    (scheduler handlers {} config)))

(defn start!
  "starts the scheduler"
  {:added "2.2"}
  [scheduler]
  (component/start scheduler))

(defn stop!
  "stops the scheduler"
  {:added "2.2"}
  [scheduler]
  (component/stop scheduler))

(defn stopped?
  "checks to see if the scheduler is stopped"
  {:added "2.2"}
  [scheduler]
  (component/stopped? (:clock scheduler)))

(defn running?
  "checks to see if the scheduler is running"
  {:added "2.2"}
  [scheduler]
  (component/started? (:clock scheduler)))

(defn simulate
  "simulates the scheduler running for a certain interval:

  (simulate
   (scheduler {:print-task {:handler (fn [t params instance]
                                       (println t params))
                            :schedule \"/2 * * * * * *\"
                            :params   {:value \"hello world\"}}})
   {:start (java.util.Date. 0)
    :end   (java.util.Date. 100000)
    :pause 10})"
  {:added "2.2"}
  [scheduler {:keys [start end step pause mode]}]
  (swap! (-> scheduler :clock :state) assoc :disabled true)
  (let [scheduler (component/start scheduler)
        tz   (-> scheduler :clock :meta :region)
        type (-> scheduler :clock :meta :type)
        start-val (time/to-long start)
        end-val   (time/to-long end)
        step      (cond (nil? step) 1000
                        (number? step) step
                        :else (time/to-long step))
        pause     (or pause 0)
        mode      (or mode :sync)
        timespan  (range start-val end-val step)]
    (doseq [t-val timespan]
      (let [t (time/from-long type t-val tz)]
        (reset! (:ticker scheduler)
                {:time t :array (tab/to-time-array t) :instance {:mode mode}})
        (if-not (zero? pause)
          (Thread/sleep pause))))
    (swap! (-> scheduler :clock :state) dissoc :disabled)
    (component/stop scheduler)))

(defn uptime
  "checks to see how long the scheduler has been running"
  {:added "2.2"}
  [scheduler]
  (if-let [start (-> scheduler :clock deref :start-time)]
    (-  (System/currentTimeMillis)
        (time/to-long start))))

(defn list-tasks [scheduler]
  (persistent! (-> scheduler :array :handlers)))

(defn get-task
  [scheduler name]
  (first (ova/select (-> scheduler :array :handlers) [:name name])))

(defn enable-task [scheduler name]
  (dosync (ova/smap! (-> scheduler :array :handlers) [:name name]
                     dissoc :disabled)))

(defn disable-task [scheduler name]
  (dosync (ova/smap! (-> scheduler :array :handlers) [:name name]
                     assoc :disabled true)))

(defn list-instances
  [scheduler name]
  (-> (get-task scheduler name)
      :registry
      :store
      deref
      (get name)
      vals))

(defn shutdown!
  [scheduler]
  (doall (for [tsk  (list-tasks scheduler)
               inst (procedure/list-instances (:registry tsk))]
           (procedure/kill (:registry tsk) (:name tsk) (:id inst))))
  (stop! scheduler))

(defn restart!
  [scheduler]
  (shutdown! scheduler)
  (start! scheduler))

