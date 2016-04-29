(ns hara.object
  (:require [hara.namespace.import :as ns]
            [hara.object
             [read :as read]
             [write :as write]
             [map-like :as map-like]
             [string-like :as string-like]]))

(ns/import hara.object.read   [to-data meta-read read-getters read-reflect-fields]
           hara.object.write  [from-data meta-write write-setters write-reflect-fields]
           hara.object.enum   [enum? enum-values])

(defmacro string-like [& {:as classes}]
  `(vector ~@(map (fn [[cls opts]]
                    `(string-like/extend-string-like ~cls ~opts))
                  classes)))

(defmacro map-like [& {:as classes}]
  `(vector ~@(map (fn [[cls opts]]
                    `(map-like/extend-map-like ~cls ~opts))
                  classes)))
