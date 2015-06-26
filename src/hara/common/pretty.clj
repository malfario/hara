(ns hara.common.pretty
  (:require [clojure.instant :as instant]))

(defn prettify-string [x]
  (cond (nil? x) "nil"
        
        (string? x)
        (str "\"" x "\"")

        (instance? java.util.Date x)
        (let [^java.text.SimpleDateFormat utc-format (.get ^ThreadLocal @#'instant/thread-local-utc-date-format)]
          (str "#inst \"" (.format utc-format x) "\""))
        
        :else x))

(defmethod print-method clojure.lang.Atom
  [v ^java.io.Writer w]
  (.write w (str "#atom " (prettify-string @v))))

(defmethod print-method clojure.lang.Ref
  [v ^java.io.Writer w]
  (.write w (str "#ref " (prettify-string @v))))
  
(defmethod print-method clojure.lang.Agent
  [v ^java.io.Writer w]
  (.write w (str "#agent " (prettify-string @v))))
