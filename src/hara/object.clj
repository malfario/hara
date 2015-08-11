(ns hara.object
  (:require [hara.namespace.import :as ns]
            [hara.object.access :as access]
            [hara.object.base :as base]
            [hara.object.enum :as enum]
            [hara.object.util :as util]
            [hara.object.map-like :as map-like]
            [hara.object.string-like :as string-like]))

(ns/import hara.object.access [access]
           hara.object.base   [to-data from-data meta-object]
           hara.object.enum   [enum? enum-values]
           hara.object.util   [java->clojure
                               clojure->java
                               object-getters
                               object-setters
                               object-apply
                               object-data])

(defmacro extend-stringlike [& {:as classes}]
  `(vector ~@(map (fn [[cls opts]]
                    `(string-like/extend-stringlike-class ~cls ~opts))
                  classes)))

(defmacro extend-maplike [& {:as classes}]
  `(vector ~@(map (fn [[cls opts]]
                    `(map-like/extend-maplike-class ~cls ~opts))
                  classes)))
