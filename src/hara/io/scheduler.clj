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
  [handlers]
  (fn [config]
    (scheduler handlers {} config)))

(defn start! [scheduler]
  (component/start scheduler))

(defn stop! [scheduler]
  (component/stop scheduler))

(defn stopped? [scheduler]
  (component/stopped? (:clock scheduler)))

(defn running? [scheduler]
  (component/started? (:clock scheduler)))

(defn simulate [scheduler {:keys [start end step pause mode]}]
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

(defn uptime [scheduler]
  (if-let [start (-> scheduler :clock deref :start-time)]
    (-  (System/currentTimeMillis)
        (time/to-long start))))

(defn task [scheduler name]
  (first (ova/select (-> scheduler :array :handlers) [:name name])))

(defn enable-task [scheduler name]
  (dosync (ova/smap! (-> scheduler :array :handlers) [:name name]
                     dissoc :disabled)))

(defn disable-task [scheduler name]
  (dosync (ova/smap! (-> scheduler :array :handlers) [:name name]
                     assoc :disabled true)))

(comment
  
  (def sch1 (component/start ((create
                               {:print-task (fn [t params]
                                              (println t params)
                                              (Thread/sleep 10000))})
                              {:array {:print-task {:schedule "/2 * * * * * *"
                                                    :params  {:hello "world"}}}})))

  (disable-task sch1 :print-task)
  (task sch1 :print-task)
  (ova/select (-> sch1 :array :handlers) [:name :print-task])
  
  (uptime sch1)
  (component/stop sch1)
  
  (defn scheduler [])
  
  (system {:array <>
           :clock <>}

          {:array <>
           :clock <>}
          
          "schedular")

  
  (simulate
   (scheduler
    {:print-task (fn [t params instance]
                   (println t params))}
    {:print-task {:schedule "/2 * * * * * *"
                  :params   {:value "hello world"}}})
   {:start (java.util.Date. 0)
    :end   (java.util.Date. 100000)
    :pause 10})

  

  (def sch (component/start (scheduler {:print-task (fn [t params]
                                                      (println "PRINTING:" t params))}
                                       {:print-task {:schedule "/2 * * * * * *"
                                                     :params  {:hello "world"}}})))

  (def sch (component/start
            (scheduler {:print-task {:handler (fn [t params instance]
                                                (println "PRINTING:" (:id instance) params))
                                     :schedule "/2 * * * * * *"
                                     :params  {:hello "world"}}})))

  (component/stop sch)
  
  
  
  
  (type (read-string "java.util.Date"))
  (schedule tasks)
  => scheduler

  (defn scheduler [])

  (defn stop [])

  (defn start [])


  (topology {:scheduler new-scheduler})

  (def options {:type  #{:util-date :time-instant}
                :interval <NUM>})
  
  (def handlers {:print-task   {:handler (fn [dt params] "hello" (:value params))}
                 :file-task    (fn [dt] "hello" dt)
                 :simple-task  (fn [] (println "hello world"))})
  
  (def topology {:scheduler  (scheduler handler data config)})

  (def config   {:scheduler  {:print-task {:schedule "0-2 * * * * *"
                                           :params   {:value "hello world"}}}})
  
  )
