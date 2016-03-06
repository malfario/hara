(ns hara.time.data
  (:require [hara.io.environment :as env]
            [hara.time.data
             map
             long]
            [hara.time.data.zone
             java-util-timezone]
            [hara.time.data.instant
             java-util-date
             java-util-calendar
             java-sql-timestamp]
            [hara.time.format
             java-text-simpledateformat]))

(env/require {:java    {:major 1 :minor 8}}
             '[hara.time.data.zone
               java-time-zoneid]
             '[hara.time.data.instant
               java-time-instant
               java-time-clock
               java-time-zoneddatetime]
             '[hara.time.format
               java-time-format-datetimeformatter])
