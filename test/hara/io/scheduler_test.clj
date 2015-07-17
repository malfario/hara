(ns hara.io.scheduler-test
  (:use midje.sweet)
  (:require [hara.io.scheduler :refer :all]
            [hara.component :as component]
            [hara.concurrent.procedure :as procedure]))


^{:refer hara.io.scheduler/scheduler :added "2.2"}
(fact "creates a schedular from handlers, or both handlers and config")

^{:refer hara.io.scheduler/create :added "2.2"}
(fact "function for use with the component framework.")

^{:refer hara.io.scheduler/start! :added "2.2"}
(fact "starts the scheduler")

^{:refer hara.io.scheduler/stop! :added "2.2"}
(fact "stops the scheduler")

^{:refer hara.io.scheduler/stopped? :added "2.2"}
(fact "checks to see if the scheduler is stopped")

^{:refer hara.io.scheduler/running? :added "2.2"}
(fact "checks to see if the scheduler is running")

^{:refer hara.io.scheduler/uptime :added "2.2"}
(fact "checks to see how long the scheduler has been running")

^{:refer hara.io.scheduler/simulate :added "2.2"}
(comment "simulates the scheduler running for a certain interval:"

  (simulate
   (scheduler {:print-task {:handler (fn [t params instance]
                                       (println t params))
                            :schedule "/2 * * * * * *"
                            :params   {:value "hello world"}}})
   {:start (java.util.Date. 0)
    :end   (java.util.Date. 100000)
    :pause 10}))


(comment
  
  (def sch1 (component/start ((create
                               {:print-task (fn [t params]
                                              (println t params)
                                              (Thread/sleep 10000))})
                              {:array {:print-task {:schedule "/2 * * * * * *"                         
                                                    :params  {:hello "world"}}}})))

  (disable-task sch1 :print-task)
  (get-task sch1 :print-task)
  (ova/select (-> sch1 :array :handlers) [:name :print-task])
  
  (uptime sch1)
  (component/stop sch1)
  (component/start sch1)
  
  (defn scheduler [])
  
  (system {:array <>
           :clock <>}

          {:array <>
           :clock <>}
          
          "schedular")

  
  

  

  (def sch (component/start (scheduler {:print-task (fn [t params]
                                                      ;(println "PRINTING:" t params)
                                                      )}
                                       {:print-task {:schedule "/2 * * * * * *"
                                                     :params  {:hello "world"}}})))

  (def sch 
    (scheduler {:print-task {:handler (fn [t params instance]
                                        (Thread/sleep 100000)
                                        (println "PRINTING:" (:id instance) params))
                             :schedule "/2 * * * * * *"
                             :params  {:hello "world"}}}))

  (list-instances sch :print-task)
  (enable-task sch :print-task)
  (:store
   (:registry (get-task sch :print-task)))
  (list-tasks sch)
  ((get-task sch :print-task) 1 {} {})
  ((get-task sch :print-task) 2 {} {})

  (component/start sch)
  (component/stop sch)
  
  
  
  
  (type (read-string "java.util.Date"))
  (schedule tasks)
  => scheduler

  

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
