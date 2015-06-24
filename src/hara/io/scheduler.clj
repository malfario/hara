(ns hara.io.scheduler)

(comment

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
