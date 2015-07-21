(ns hara.protocol.string)

(defprotocol IString
  (-to-string [x])
  (-to-string-meta [x]))

(defmulti -from-string (fn [string type] type))
