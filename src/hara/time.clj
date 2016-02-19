(ns hara.time
  (:require [hara.time.common :as common]
            [hara.protocol.time :as time]
            [hara.class.checks :as class]
            [hara.time.impl java-util
             ;;java-sql
             ]
            [hara.io.environment :as env]))

(env/require {:java    {:major 1 :minor 8}}
             '[hara.time.impl java-time])

(defn meta-info [obj]
  (common/meta-info obj))

(defn instant? [obj]
  (class/implements? time/IInstant obj))

(defn interval? [obj]
  (class/implements? time/IInterval obj))

(defn period? [obj]
  (class/implements? time/IPeriod obj))

(defn partial? [obj]
  (class/implements? time/IPartial obj))

(defn timezone? [obj]
  (class/implements? time/IZone obj))

(defn to-value [t]
  (time/-to-value t))

(defn start-value [p]
  (time/-start-value p))

(defn end-value [p]
  (time/-start-value p))

(defn to-timezone [obj]
  (time/-timezone obj))
