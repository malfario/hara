(ns hara.event.condition.data
  (:require [hara.event.common :as common]
            [hara.common.primitives :refer [uuid]]))

(defn issue
  [data msg options default]
  (let [data    (common/expand-data data)
        id      (common/new-id)
        options (or options {})
        optmap (zipmap (keys options) (repeat id))]
    {:id id
     :data data
     :msg msg
     :options options
     :optmap optmap
     :default default}))

(defn catch-condition
  [target value]
  (ex-info "catch" {::condition :catch :target target :value value}))

(defn choose-condition
  [target label args]
  (ex-info "choose" {::condition :choose :target target :label label :args args}))

(defn exception
  ([issue]
   (let [contents (:data issue)
         msg    (str (:msg issue) " - " contents)
         error  ^Throwable (ex-info msg contents)]
     (doto error
       (.setStackTrace (->> (seq (.getStackTrace error))
                            (filter (fn [^StackTraceElement name] (not (.startsWith (.getClassName name) "hara.event"))))
                            (into-array StackTraceElement))))))
  ([issue data]
   (exception (update-in issue [:data] merge data))))
