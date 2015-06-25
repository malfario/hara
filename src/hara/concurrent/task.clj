(ns hara.concurrent.task
  (:require [hara.time :as time]
            [hara.common.checks :refer [hash-map? thread?]]
            [hara.data.map :as map]
            [hara.data.nested :as nested]
            [hara.function.args :as args]))

(defonce ^:dynamic *default-registry* (atom {}))

(defonce ^:dynamic *default-cache* (atom {}))

(defonce ^:dynamic *default-settings*
  {:mode :async
   :time {:type java.util.Date
          :zone (time/system-timezone)} 
   :registry *default-registry*
   :cache    *default-cache*
   :id-fn    (fn [_] (str (java.util.UUID/randomUUID)))})

(defn max-inputs
  "finds the maximum number of inputs that a function can take
  
  (max-inputs (fn ([a]) ([a b])) 4)
  => 2

  (max-inputs (fn [& more]) 4)
  => 4
  
  (max-inputs (fn ([a])) 0)
  => throws"
  {:added "2.2"} [func num]
  (if (args/vargs? func)
    num
    (let [cargs (args/arg-count func)
          carr (filter #(<= % num) cargs)]
      (if (empty? carr)
        (throw (Exception. (str "Function needs at least " (apply min cargs) " inputs")))
        (apply max carr)))))

(defrecord TaskInstance []
  Object
  (toString [obj]
    (str "#exec" (-> (select-keys obj [:name :id :result :timestamp
                                       :mode :params :cached :runtime])
                     (->> (into {}))
                     (update-in [:runtime] (fn [x] (if x (deref x) {})))
                     (update-in [:result]  (fn [x] (if (realized? x) (deref x) :waiting))))))

  clojure.lang.IDeref
  (deref [obj]
    (let [{:keys [data type]} (deref (:result obj))]
      (case type
        :success data
        :error   (throw data)))))



(defn wrap-instance [f]
  (fn [{:keys [arglist] :as instance} args]
    (let [args (map (fn [arg desc]
                      (if (= desc :instance)
                        instance
                        arg))
                    args
                    arglist)]
      (f instance args))))

(defn wrap-cached [f]
  (fn [{:keys [cached cache overwrite name id] :as instance} args]
    (if-not cached
      (f instance args)
      (let [prev (get-in @cache (concat [name id] args))]
        (if (and prev (not overwrite))
          (deliver (:result instance)
                   (assoc @(:result prev) :cached (-> prev :runtime deref :ended)))
          (let [current (f instance args)]
            (swap! cache assoc-in (concat [name id] args) instance)))))))

(defn wrap-exist [f]
  (fn [{:keys [registry name id] :as instance} args]
    (if-let [existing (get-in @registry [name id])]
      existing
      (f instance args))))

(defn wrap-timing [f]
  (fn [instance args]
    (swap! (:runtime instance) assoc :started (time/now))
    (f instance args)
    (swap! (:runtime instance) assoc :ended (time/now))))

(defn wrap-registry [f]
  (fn [{:keys [registry name id cached] :as instance} args]
    (swap! registry assoc-in [name id] instance)
    (f instance args)
    (swap! registry map/dissoc-in [name id])))

(defn invoke-base 
  [instance args]
  (try
    (let [result (apply (:handler instance) args)]
      (deliver (:result instance) {:type :success
                                   :data result}))
    (catch Throwable t
      (deliver (:result instance) {:type :error
                                   :data t}))))

(defn wrap-mode [f]
  (fn [instance args]
    (let [instance  (update-in instance [:result]
                              (fn [result] (or result
                                               (promise))))]
      (case (:mode instance)
        :sync   (let [instance (assoc instance :thread (Thread/currentThread))]
                  (f instance args)
                  instance)
        :async  (let [thread (future (f instance args))]
                  (assoc instance :thread thread))))))

(defn wrap-id [f]
  (fn [instance args]
    (let [instance (update-in instance [:id]
                              (fn [id] (or id
                                           ((:id-fn instance) instance))))]
      (f instance args))))

(defn invoke-task [{:keys [id-fn handler arglist time] :as task} & args]
  (let [_          (if (< (count arglist) (count args))
                     (throw (Exception.
                             (str "There should be less inputs than the arglist: " arglist))))
        ninputs    (max-inputs handler (count args))
        opts       (zipmap arglist args)
        opts       (if-let [t (:instance opts)]
                     (nested/merge-nested t (dissoc opts :instance))
                     opts)
        instance   (map->TaskInstance (-> task
                                          (nested/merge-nested opts)
                                          (assoc :task task)))
        instance   (update-in instance [:timestamp]
                              (fn [t] (or t
                                          (time/now (:type time)
                                                    (:zone time)))))
        instance   (update-in instance [:runtime]
                              (fn [rt] (or rt
                                           (atom {}))))
        nargs      (->> arglist
                        (take ninputs)
                        (map #(get instance %)))]
    ((-> invoke-base
         wrap-instance
         wrap-cached
         wrap-timing
         wrap-registry
         wrap-mode
         wrap-exist
         wrap-id)
     instance nargs)))

(defrecord Task []
  Object
  (toString [obj]
    (str "#task" (-> (select-keys obj [:name :mode :params :cached :runtime :arglist])
                     (->> (into {})))))
  
  clojure.lang.IFn
  (invoke [obj]
    (invoke-task obj))
  (invoke [obj arg1]
    (invoke-task obj arg1))
  (invoke [obj arg1 arg2]
    (invoke-task obj arg1 arg2))
  (invoke [obj arg1 arg2 arg3]
    (invoke-task obj arg1 arg2 arg3))
  (invoke [obj arg1 arg2 arg3 arg4]
    (invoke-task obj arg1 arg2 arg3 arg4))
  (invoke [obj arg1 arg2 arg3 arg4 arg5]
    (invoke-task obj arg1 arg2 arg3 arg4 arg5))
  (invoke [obj arg1 arg2 arg3 arg4 arg5 arg6]
    (invoke-task obj arg1 arg2 arg3 arg4 arg5 arg6))
  (invoke [obj arg1 arg2 arg3 arg4 arg5 arg6 arg7]
    (invoke-task obj arg1 arg2 arg3 arg4 arg5 arg6 arg7))
  (invoke [obj arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8]
    (invoke-task obj arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8))
  (invoke [obj arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9]
    (invoke-task obj arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9))
  (invoke [obj arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10]
    (invoke-task obj arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10))
  (invoke [obj arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11]
    (invoke-task obj arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11))
  (invoke [obj arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12]
    (invoke-task obj arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12))
  (invoke [obj arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13]
    (invoke-task obj arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13))
  (invoke [obj arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14]
    (invoke-task obj arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14))
  (applyTo [obj args]
    (apply invoke-task obj args)))

(defmethod print-method Task
  [v ^java.io.Writer w]
  (.write w (str v)))

(defmethod print-method TaskInstance
  [v ^java.io.Writer w]
  (.write w (str v)))

(prefer-method print-method
               clojure.lang.IRecord
               clojure.lang.IDeref)

(defn task [tk]
  (cond (fn? tk)
        (task {:handler tk})

        (hash-map? tk)
        (map->Task (nested/merge-nil-nested tk *default-settings*))))


(defn list-all 
  ([]))



(comment
  (defn all-running
    ([] (all-running *default-registry*))
    ([registry]
     (nested/update-vals-in @registry [] (comp sort keys))))

  (defn instance
    ([name id] (instance *default-registry* name id))
    ([registry name id]
     (get-in @registry [name id])))

  (defn running?
    ([name id] (running? *default-registry* name id))
    ([registry name id]
     (if (instance registry name id)
       true
       false)))

  (defn kill
    ([name id] (kill *default-registry* name id))
    ([registry name id]
     (if-let [{:keys [thread]} (instance registry name id)]
       (do (cond (future? thread)
                 (future-cancel thread)
                 
                 (and (thread? thread)
                      (= thread (Thread/currentThread)))
                 (.stop ^Thread thread))
           (swap! registry map/dissoc-in [name id])
           true)
       false))))
