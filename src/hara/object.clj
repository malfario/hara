(ns hara.object
  (:require [hara.namespace.import :as ns]
            [hara.object.base :as base]
            [hara.object.enum :as enum]
            [hara.object.util :as util]
            [hara.object.map-like :as map-like]
            [hara.object.string-like :as string-like]))

(ns/import hara.object.base :all
           hara.object.enum :all
           hara.object.util :all)

(defmacro extend-stringlike [& {:as classes}]
  `(vector ~@(map (fn [[cls opts]]
                    `(string-like/extend-stringlike-class ~cls ~opts))
                  classes)))

(defmacro extend-maplike [& {:as classes}]
  `(vector ~@(map (fn [[cls opts]]
                    `(map-like/extend-maplike-class ~cls ~opts))
                  classes)))
