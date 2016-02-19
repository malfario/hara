(ns hara.protocol.time)

(defprotocol IZone
  (-timezone     [z]))

(defprotocol IInstant
  (-to-value     [t]))
  
(defprotocol IInterval
  (-start-value  [v])
  (-end-value    [v]))

(defprotocol IPeriod
  (-duration     [p])
  (-millis       [p])
  (-seconds      [p])
  (-minutes      [p])
  (-hours        [p])
  (-days         [p])
  (-months       [p])
  (-years        [p]))

(defprotocol IPartial
  (-missing      [r]))