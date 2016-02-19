(ns hara.time.common
  (:require [hara.protocol.time :as time]))

(defmulti timezone? (fn [type] type))

(defmulti from-long (fn [type long tz] type))

(defmulti from-map (fn [type map] type))

(defmulti truncate (fn [data field tz] (type data)))
