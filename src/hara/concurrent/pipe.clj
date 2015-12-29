(ns hara.concurrent.pipe
  (:require [hara.common.primitives :as common])
  (:refer-clojure :exclude [send]))

(defrecord Pipe [])

(defn- handle-task [queue handler thread]
  (let [hold (promise)
        current (future
                  (loop []
                    (let [_ @hold]
                      (handler (first @queue))
                      (swap! queue pop))
                    (if (empty? @queue)
                      (reset! thread nil)
                      (recur))))]
    (reset! thread current)
    (deliver hold true)))

(defn- add-handler [queue handler thread]
  (add-watch queue
             :handler
             (fn [_ _ p n]
               (when (and (< (count p) (count n))
                        (nil? @thread))
                 (handle-task queue handler thread)))))

(defn pipe
  "creates a pipe so that tasks can be acted upon asynchronously in order in which they were sent
 
   (def atm (atom []))
   (def p (pipe (fn [msg]
                  (swap! atm conj msg))))
 
   (pipe/send p 1)
   (pipe/send p 2)
   (pipe/send p 3)
   @atm => [1 2 3]"
  {:added "2.2"}
  [handler]
  (let [queue  (atom (common/queue))
        thread (atom nil)
        _      (add-handler queue handler thread)]
    (map->Pipe {:handler handler
                :queue   queue
                :thread  thread})))

(defn send
  "sends a task to the pipe for it's handler to act upon"
  {:added "2.2"}
  [pipe task]
  (swap! (:queue pipe) conj task))
