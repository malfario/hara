(ns hara.io.scheduler.clock
  (:require [hara.time :as time]
            [hara.data.map :as map]
            [hara.event :as event]
            [hara.component :as component]
            [hara.io.scheduler.tab :as tab]))

(defn clock-loop [clock recur?]
  (if (not (:disabled @clock))
    (let [current-array  (:current-array @clock)
          region         (-> clock :meta :region)
          next-time      (time/now (-> clock :meta :type) region)
          next-array     (tab/to-time-array next-time region)]
      (cond
        (or (not= current-array next-array)
            (nil? current-array))
        (do (swap! (:state clock)
                   assoc
                   :current-time  next-time
                   :current-array next-array)
            (if-let [ticker (:ticker clock)]
              (reset! ticker {:time (time/truncate next-time (-> clock :meta :truncate))
                              :array next-array})))

        :else
        (let [interval   (-> clock :meta :interval)
              sleep-time (- 1000
                            (time/milli next-time)
                            interval)]
          (if (< 0 sleep-time)
            (Thread/sleep sleep-time)
            (Thread/sleep interval))))
      (if (and recur? )
        (recur clock true)))))

(defn clock-stopped? [clock]
  (let [thread (:thread @clock)]
      (or (nil? thread)
          (true? thread)
          (future-done? thread)
          (future-cancelled? thread))))

(defn clock-started? [clock]
  (not (clock-stopped? clock)))

(defn clock-start [clock]
  (if (clock-stopped? clock)
    (swap! (:state clock) assoc
           :start-time (time/now (-> clock :meta :type) (-> clock :region :meta))
           :thread     (future (clock-loop clock true)))
    (event/signal [:log {:msg "The clock is already running"}]))
  clock)

(defn clock-stop [clock]
  (if-not (clock-stopped? clock)
    (swap! (:state clock)
           (fn [m]
             (-> (update-in m [:thread] future-cancel)
                 (assoc :thread         nil
                        :start-time     nil
                        :current-time   nil
                        :current-array  nil))))
    (event/signal [:log {:msg "The clock is already stopped."}]))
  clock)

(defrecord Clock [meta state]
  Object
  (toString [clock]
    (str "#clock " (-> @clock
                       (dissoc :current-array :thread)
                       (assoc  :running (clock-started? clock)))))
  
  clojure.lang.IDeref
  (deref [clock] @state)
  
  component/IComponent
  (-start [clock]
    (clock-start clock))

  (-stop [clock]
    (clock-stop clock))

  (-started? [clock]
    (clock-started? clock))

  (-stopped? [clock]
    (clock-stopped? clock)))

(defmethod print-method Clock
  [v ^java.io.Writer w]
  (.write w (str v)))

(defn clock
  ([] (clock nil))
  ([meta]
   (Clock. (-> meta
               (update-in [:type] (fn [x] (if (string? x)
                                            (Class/forName x)
                                            x))))
           (atom {:thread          nil
                  :start-time      nil
                  :current-time    nil
                  :current-array   nil}))))
