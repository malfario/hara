(ns hara.io.scheduler
  (:require [hara
             [time :as time]
             [component :as component]]
            [hara.concurrent
             [ova :as ova]
             [procedure :as procedure]]
            [hara.concurrent.procedure
             [data :as data]
             [registry :as registry]]
            [hara.data.nested :as nested]
            [hara.io.scheduler
             [array :as array]
             [clock :as clock]
             [tab :as tab]]))

(defonce ^:dynamic *defaults*
  {:clock    {:type     "java.util.Date"
              :timezone (time/local-timezone)
              :interval 1
              :truncate :second}
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
                                        (str t params))
                             :schedule \"/2 * * * * * *\"
                             :params   {:value \"hello world\"}}})
    {:start (java.util.Date. 0)
     :end   (java.util.Date. 100000)
     :pause 10})"
  {:added "2.2"}
  [scheduler {:keys [start end step pause mode]}]
  (swap! (-> scheduler :clock :state) assoc :disabled true)
  (let [scheduler (component/start scheduler)
        clk  (-> scheduler :clock :meta)
        start-val (time/to-long start)
        end-val   (time/to-long end)
        step      (cond (nil? step) 1
                        (number? step) step
                        :else (long (/ (time/to-long step) 1000)))
        pause     (or pause 0)
        mode      (or mode :sync)
        timespan  (range start-val end-val (* 1000 step))]
    (doseq [t-val timespan]
      (let [t (time/from-long t-val clk)]
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

(defn list-tasks
  "lists all tasks in the scheduler"
  {:added "2.2"}
  [scheduler]
  (persistent! (-> scheduler :array :handlers)))

(defn get-task
  "retruns a specific task in the scheduler"
  {:added "2.2"}
  [scheduler name]
  (first (ova/select (-> scheduler :array :handlers) [:name name])))

(defn enable-task
  "enables a specific task in the scheduler"
  {:added "2.2"}
  [scheduler name]
  (dosync (ova/smap! (-> scheduler :array :handlers) [:name name]
                     dissoc :disabled)))

(defn disable-task
  "disables a specific task in the scheduler"
  {:added "2.2"}
  [scheduler name]
  (dosync (ova/smap! (-> scheduler :array :handlers) [:name name]
                     assoc :disabled true)))

(defn delete-task
  "deletes a specific task in the scheduler"
  {:added "2.2"}
  [scheduler name]
  (dosync (ova/remove! (-> scheduler :array :handlers) [:name name])))

(defn empty-tasks
  "clears all tasks in the scheduler"
  {:added "2.2"}
  [scheduler]
  (dosync (ova/empty! (-> scheduler :array :handlers))))

(defn add-task
  "add a task to the scheduler
   (add-task (scheduler {})
             :hello {:handler (fn [t params] (println params))
                    :schedule \"* * * * * * *\"
                     :params {:data \"foo\"}})"
  {:added "2.2"}
  [scheduler name props]
  (dosync (ova/append! (-> scheduler :array :handlers)
                       (array/build-handler name props {}))))

(defn reschedule-task
  "changes the schedule for an already existing task
   (-> (scheduler {:hello {:handler (fn [t params] (println params))
                                        :schedule \"* * * * * * *\"
                          :params {:data \"foo\"}}})
       (reschedule-task :hello \"/5 * * * * * *\"))"
  {:added "2.2"}
  [scheduler name schedule]
  (dosync (ova/smap! (-> scheduler :array :handlers) [:name name]
                     assoc
                     :schedule schedule
                     :schedule-array (tab/parse-tab schedule))))

(defn reparametise-task
  "changes the schedule for an already existing task
   (-> (scheduler {:hello {:handler (fn [t params] (println params))
                                          :schedule \"* * * * * * *\"
                          :params {:data \"foo\"}}})
       (reparametise-task :hello {:data \"bar\"}))"
  {:added "2.2"}
  [scheduler name opts]
  (dosync (ova/smap! (-> scheduler :array :handlers) [:name name]
                     update-in [:params] merge opts)))

(defn trigger!
  "manually executes a task, bypassing the scheduler"
  {:added "2.2"}
  ([scheduler name]
   (let [opts   (-> scheduler :clock :meta)]
     (trigger! scheduler name (time/now opts))))
  ([scheduler name key]
   (if-let [{:keys [params] :as task} (get-task scheduler name)]
     ((-> task
          (assoc :registry (:registry scheduler)))
      key params {}))))

(defn list-instances
  "lists all running instances of a tasks in the scheduler"
  {:added "2.2"}
  ([scheduler]
   (for [tsk  (list-tasks scheduler)
         inst (list-instances scheduler (:name tsk))]
     inst))
  ([scheduler name]
   (-> scheduler
       :registry
       :store
       deref
       (get name)
       vals)))

(defn kill-instance
  "kills a single instance of the running task"
  {:added "2.2"}
  [scheduler name id]
  (registry/kill (:registry scheduler) name id))

(defn kill-instances
  "kills all instances of the running task"
  {:added "2.2"}
  [{:keys [registry] :as scheduler} name]
  (doall (for [inst (registry/list-instances registry name)]
           (registry/kill registry name (:id inst)))))

(defn kill-all
  "kills all instances of all tasks in the scheduler"
  {:added "2.2"}
  [{:keys [registry] :as scheduler}]
  (doall (for [tsk  (list-tasks scheduler)
               inst (registry/list-instances registry (:name tsk))]
           (registry/kill registry (:name tsk) (:id inst)))))

(defn shutdown!
  "forcibly shuts down the scheduler, immediately killing all running threads"
  {:added "2.2"}
  [scheduler]
  (kill-all scheduler)
  (stop! scheduler))

(defn restart!
  "restarts the scheduler after a forced shutdown"
  {:added "2.2"}
  [scheduler]
  (shutdown! scheduler)
  (start! scheduler))
