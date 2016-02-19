(ns hara.time.data.instant.sql-date
  (:require [hara.protocol.time :as time])
  (:require [hara.time.common :as common])
  (:import java.sql.Date))
  
(defmethod common/from-long Date
  [type long]
  (Date. long))
  
