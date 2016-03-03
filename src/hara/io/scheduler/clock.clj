(ns hara.io.scheduler.clock
  (:require [hara.time :as time]
            [hara.data.map :as map]
            [hara.event :as event]
            [hara.component :as component]
            [hara.io.scheduler.tab :as tab]))

(defn clock-loop
  "updates the clock tick, if `recur?` is true, then it keeps running
   (-> (:clock scheduler/*defaults*)
       (clock)
       (clock-loop false))
   ;;=> #clock {:start-time nil,
   ;;           :current-time #inst \"2015-07-16T15:24:42.608-00:00\",
   ;;           :running false}
   "
  {:added "2.2"}
  [clock recur?]
  (if (not (:disabled @clock))
    (let [current-array  (:current-array @clock)
          opts           (:meta clock)
          next-time      (time/now opts)
          next-array     (tab/to-time-array next-time (:timezone opts))]
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
                            (time/millisecond next-time)
                            interval)]
          (if (< 0 sleep-time)
            (Thread/sleep sleep-time)
            (Thread/sleep interval))))
      (if recur? 
        (recur clock recur?)
        clock))))

(defn clock-stopped?
  "checks if the clock has stopped"
  {:added "2.2"}
  [clock]
  (let [thread (:thread @clock)]
      (or (nil? thread)
          (true? thread)
          (future-done? thread)
          (future-cancelled? thread))))

(defn clock-started?
  "checks if the clock has started"
  {:added "2.2"}
  [clock]
  (not (clock-stopped? clock)))

(defn clock-start
  "starts the clock"
  {:added "2.2"}
  [clock]
  (if (clock-stopped? clock)
    (swap! (:state clock) assoc
           :start-time (time/now (-> clock :meta))
           :thread     (future (clock-loop clock true)))
    (event/signal [:log {:msg "The clock is already running"}]))
  clock)

(defn clock-stop
  "stops the clock"
  {:added "2.2"}
  [clock]
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
  "creates an instance of a clock
   (clock (:clock scheduler/*defaults*))
   ;;=> #clock {:start-time nil,
   ;;           :current-time nil,
   ;;           :running false}
   "
  {:added "2.2"}
  [meta]
  (Clock. (-> meta
              (update-in [:type] (fn [x] (if (string? x)
                                           (Class/forName x)
                                           x))))
          (atom {:thread          nil
                 :start-time      nil
                 :current-time    nil
                 :current-array   nil})))
