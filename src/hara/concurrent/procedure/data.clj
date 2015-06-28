(ns hara.concurrent.procedure.data
  (:require [hara.protocol.state :as state]
            [hara.data.nested :as nested]))

(defrecord ProcedureRegistry [store]
  Object
  (toString [reg]
    (str "#reg " (nested/update-vals-in @store [] keys)))

  clojure.lang.IDeref
  (deref [reg]
    (deref store))

  state/IStateSet
  (-empty-state [reg opts]
    (reset! store {}))
  (-set-state [reg opts v]
    (reset! store v))
  (-update-state [reg opts f args]
    (apply swap! store f args)))

(defmethod print-method ProcedureRegistry
  [v ^java.io.Writer w]
  (.write w (str v)))

(defn registry [] (ProcedureRegistry. (atom {})))

(defrecord ProcedureCache [store]
  Object
  (toString [cache]
    (str "#cache " (nested/update-vals-in @store []
                                        nested/update-vals-in [] keys)))

  clojure.lang.IDeref
  (deref [cache]
    (deref store))

  state/IStateSet
  (-empty-state [cache opts]
    (reset! store {}))
  (-set-state [cache opts v]
    (reset! store v))
  (-update-state [cache opts f args]
    (apply swap! store f args)))

(defmethod print-method ProcedureCache
  [v ^java.io.Writer w]
  (.write w (str v)))

(defn cache []  (ProcedureCache. (atom {})))

(prefer-method print-method
               clojure.lang.IRecord
               clojure.lang.IDeref)
