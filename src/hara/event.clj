(ns hara.event
  (:require [hara.common.error :refer [error]]
            [hara.data.map :as map]
            [hara.common.primitives :refer [uuid]]))

(defonce ^:dynamic *global* (atom {}))

(defn attach-listener [key handler]
  (swap! *global* assoc key handler))

(defn detach-listener [key]
  (swap! *global* dissoc key))

(defn list-listeners []
  (keys *global*))

(def event-types #{:catch :choose :common})

(defrecord Event [type target value])

(defn sig
  ([value]
   (sig :common nil value))
  ([type target value]
   {::type type ::target target ::value value}))

(defn signal [value])

