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

^{:refer hara.io.scheduler/simulate :added "2.2"}
(fact "simulates the scheduler running for a certain interval:"

  (simulate
   (scheduler {:print-task {:handler (fn [t params instance]
                                       (str t params))
                            :schedule "/2 * * * * * *"
                            :params   {:value "hello world"}}})
   {:start (java.util.Date. 0)
    :end   (java.util.Date. 100000)
    :pause 10}))

^{:refer hara.io.scheduler/uptime :added "2.2"}
(fact "checks to see how long the scheduler has been running")

^{:refer hara.io.scheduler/list-tasks :added "2.2"}
(fact "lists all tasks in the scheduler")

^{:refer hara.io.scheduler/get-task :added "2.2"}
(fact "retruns a specific task in the scheduler")

^{:refer hara.io.scheduler/enable-task :added "2.2"}
(fact "enables a specific task in the scheduler")

^{:refer hara.io.scheduler/disable-task :added "2.2"}
(fact "disables a specific task in the scheduler")

^{:refer hara.io.scheduler/delete-task :added "2.2"}
(fact "deletes a specific task in the scheduler")

^{:refer hara.io.scheduler/empty-tasks :added "2.2"}
(fact "clears all tasks in the scheduler")

^{:refer hara.io.scheduler/add-task :added "2.2"}
(fact "add a task to the scheduler"
  (add-task (scheduler {})
            :hello {:handler (fn [t params] (println params))
                    :schedule "* * * * * * *"
                    :params {:data "foo"}}))

^{:refer hara.io.scheduler/reschedule-task :added "2.2"}
(fact "changes the schedule for an already existing task"
  (-> (scheduler {:hello {:handler (fn [t params] (println params))
                                       :schedule "* * * * * * *"
                          :params {:data "foo"}}})
      (reschedule-task :hello "/5 * * * * * *")))

^{:refer hara.io.scheduler/reparametise-task :added "2.2"}
(fact "changes the schedule for an already existing task"
  (-> (scheduler {:hello {:handler (fn [t params] (println params))
                                         :schedule "* * * * * * *"
                          :params {:data "foo"}}})
      (reparametise-task :hello {:data "bar"})))


^{:refer hara.io.scheduler/trigger! :added "2.2"}
(fact "manually executes a task, bypassing the scheduler")

^{:refer hara.io.scheduler/list-instances :added "2.2"}
(fact "lists all running instances of a tasks in the scheduler")

^{:refer hara.io.scheduler/kill-instance :added "2.2"}
(fact "kills a single instance of the running task")

^{:refer hara.io.scheduler/kill-instances :added "2.2"}
(fact "kills all instances of the running task")

^{:refer hara.io.scheduler/kill-all :added "2.2"}
(fact "kills all instances of all tasks in the scheduler")

^{:refer hara.io.scheduler/shutdown! :added "2.2"}
(fact "forcibly shuts down the scheduler, immediately killing all running threads")

^{:refer hara.io.scheduler/restart! :added "2.2"}
(fact "restarts the scheduler after a forced shutdown")
