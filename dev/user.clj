(ns user
  (:require [clojure.java.io :as io]
            [hara.io.watch :as w]
            [clojure.tools.namespace.repl :refer [refresh]]))

(def system nil)

(defn filemap []
  {:test {:file "\\\\VMX-XP32\\elastindex_test"
          :callback println
          :opts {:mode :sync :recursive false}}} )

(defn init [])

(defn start []
  (let [filemap (filemap)]
    (doseq [[name {:keys [file callback opts]}] filemap]
      (w/add-io-watch (io/file file) (keyword name) callback opts))
    (alter-var-root #'system (constantly filemap))))

(defn stop []
  (when system
    (doseq [[name {:keys [file]}] system]
      (w/remove-io-watch (io/file file) (keyword name) nil))))

(defn reset []
  (stop)
  (refresh :after 'user/start))

(defn go []
  (init)
  (start))
