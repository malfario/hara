(ns hara.concurrent.procedure
  (:require [hara.time :as time]
            [hara.event :as event]
            [hara.common.checks :refer [hash-map? thread?]]
            [hara.common.state :as state]
            [hara.data.map :as map]
            [hara.data.nested :as nested]
            [hara.function.args :as args]
            [hara.concurrent.procedure.data :as data]))

(defonce ^:dynamic *default-registry* (data/registry))

(defn instance
    ([name id] (instance *default-registry* name id))
    ([registry name id]
     (get-in @registry [name id])))

(defn kill
    ([name id] (kill *default-registry* name id))
    ([registry name id]
     (if-let [{:keys [thread]} (instance registry name id)]
       (do (cond (future? thread)
                 (future-cancel thread)

                 (and (thread? thread)
                      (not= thread (Thread/currentThread)))
                 (do (.stop ^Thread thread)
                     (Thread/sleep 1)))
           (event/signal [:log {:msg "Killed Execution" :instance instance}])
           (state/update registry map/dissoc-in [name id])
           true)
       false)))

(defonce ^:dynamic *default-cache* (data/cache))

(def ^:dynamic *default-settings*
  {:mode :async
   :interrupt false
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
  {:added "2.2"}
  [func num]
  (if (args/vargs? func)
    num
    (let [cargs (args/arg-count func)
          carr (filter #(<= % num) cargs)]
      (if (empty? carr)
        (throw (Exception. (str "Function needs at least " (apply min cargs) " inputs")))
        (apply max carr)))))

(defrecord ProcedureInstance []
  Object
  (toString [obj]
    (str "#proc[" (:id obj) "]"
         (-> (select-keys obj [:name :result :timestamp :interrupt
                               :overwrite :cached :runtime
                               :mode :params :input :args])
             (->> (into {}))
             (update-in [:runtime] (fn [x] (if x (deref x) {})))
             (update-in [:result]  (fn [x] (if (realized? x) (deref x) :waiting))))))

  clojure.lang.IDeref
  (deref [obj]
    (let [{:keys [data type]} (deref (:result obj))]
      (case type
        :success data
        :error   (throw data)))))

(defmethod print-method ProcedureInstance
  [v ^java.io.Writer w]
  (.write w (str v)))

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
      (let [prev (get-in @cache [name id args])]
        (if (and prev (not overwrite))
          (deliver (:result instance)
                   (assoc @(:result prev) :cached (-> prev :runtime deref :ended)))
          (let [current (f instance args)]
            (state/update cache assoc-in [name id args] instance)))))))

(defn wrap-interrupt [f]
  (fn [{:keys [registry name id interrupt] :as instance} args]
    (let [existing (get-in @registry [name id])]
      (cond (and interrupt existing)
            (do (kill registry name id)
                (f instance args))

            existing
            existing

            :else
            (f instance args)))))

(defn wrap-timing [f]
  (fn [instance args]
    (swap! (:runtime instance) assoc :started (time/now))
    (f instance args)
    (swap! (:runtime instance) assoc :ended (time/now))))

(defn wrap-registry [f]
  (fn [{:keys [registry name id cached] :as instance} args]
    (state/update registry assoc-in [name id] instance)
    (f instance args)
    (state/update registry map/dissoc-in [name id])))

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

(defn invoke-procedure [{:keys [id-fn handler arglist time] :as procedure} & args]
  (let [_          (if (< (count arglist) (count args))
                     (throw (Exception.
                             (str "There should be less inputs than the arglist: " arglist))))
        ninputs    (max-inputs handler (count args))
        opts       (zipmap arglist args)
        opts       (if-let [t (:instance opts)]
                     (nested/merge-nested t (dissoc opts :instance))
                     opts)
        instance   (nested/merge-nested procedure opts)
        instance   (update-in instance [:timestamp]
                              (fn [t] (or t
                                          (time/now (:type time)
                                                    (:zone time)))))
        instance   (update-in instance [:runtime]
                              (fn [rt] (or rt
                                           (atom {}))))
        nargs      (->> arglist
                        (take ninputs)
                        (map #(get instance %)))
        instance   (-> instance
                       (assoc :input args)
                       (assoc :args  nargs)
                       (assoc :procedure procedure)
                       (map->ProcedureInstance))]
    ((-> invoke-base
         wrap-instance
         wrap-cached
         wrap-timing
         wrap-registry
         wrap-mode
         wrap-interrupt
         wrap-id)
     instance nargs)))

(defrecord Procedure []
  Object
  (toString [obj]
    (str "#proc" (-> (select-keys obj [:name :mode :params :cached :runtime :arglist])
                     (->> (into {})))))

  clojure.lang.IFn
  (invoke [obj]
    (invoke-procedure obj))
  (invoke [obj arg1]
    (invoke-procedure obj arg1))
  (invoke [obj arg1 arg2]
    (invoke-procedure obj arg1 arg2))
  (invoke [obj arg1 arg2 arg3]
    (invoke-procedure obj arg1 arg2 arg3))
  (invoke [obj arg1 arg2 arg3 arg4]
    (invoke-procedure obj arg1 arg2 arg3 arg4))
  (invoke [obj arg1 arg2 arg3 arg4 arg5]
    (invoke-procedure obj arg1 arg2 arg3 arg4 arg5))
  (invoke [obj arg1 arg2 arg3 arg4 arg5 arg6]
    (invoke-procedure obj arg1 arg2 arg3 arg4 arg5 arg6))
  (invoke [obj arg1 arg2 arg3 arg4 arg5 arg6 arg7]
    (invoke-procedure obj arg1 arg2 arg3 arg4 arg5 arg6 arg7))
  (invoke [obj arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8]
    (invoke-procedure obj arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8))
  (invoke [obj arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9]
    (invoke-procedure obj arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9))
  (invoke [obj arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10]
    (invoke-procedure obj arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10))
  (invoke [obj arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11]
    (invoke-procedure obj arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11))
  (invoke [obj arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12]
    (invoke-procedure obj arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12))
  (invoke [obj arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13]
    (invoke-procedure obj arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13))
  (invoke [obj arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14]
    (invoke-procedure obj arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14))
  (invoke [obj arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15]
    (invoke-procedure obj arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15))
  (invoke [obj arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16]
    (invoke-procedure obj arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16))
  (invoke [obj arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17]
    (invoke-procedure obj arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17))
  (applyTo [obj args]
    (apply invoke-procedure obj args)))

(defmethod print-method Procedure
  [v ^java.io.Writer w]
  (.write w (str v)))

(defn procedure
  "creates a procedure based"

  {:added "2.2"}
  ([tk arglist]
   (cond (fn? tk)
         (procedure {:handler tk} arglist)

         (hash-map? tk)
         (-> (assoc tk :arglist arglist)
             (nested/merge-nil-nested *default-settings*)
             (map->Procedure)))))

(defmacro defprocedure [name defaults & body]
  (let [defaults (cond (vector? defaults)
                       {:arglist defaults}
                       (map? defaults)
                       defaults)
        arglist  (:arglist defaults)]
    `(def ~name (procedure (merge {:handler (fn ~@body)} ~defaults) ~arglist))))
