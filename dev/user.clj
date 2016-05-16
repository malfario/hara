(ns user
  (:require [clojure.java.io :as io]
            [hara.io.watch :as w]
            [clojure.tools.namespace.repl :refer [refresh]]))

(def system nil)

(defn init [])

(defn start []
  (alter-var-root
   #'system
   (constantly {:watcher (w/add-io-watch
                          (io/file "\\\\VMX-XP32\\elastindex_test")
                          :test println {:mode :sync :recursive false})})))

(defn stop []
  (when system
    (w/remove-io-watch (io/file "\\\\VMX-XP32\\elastindex_test") :test nil)))

(defn reset []
  (stop)
  (refresh :after 'user/start))

(defn go []
  (init)
  (start))
