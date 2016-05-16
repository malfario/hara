(ns hara.io.watch
  (:require [hara.protocol.watch :refer :all]
            [hara.data.map :refer [merge-nil]]
            [clojure.java.io :as io]
            [clojure.string :as string])
  (:import [java.nio.file WatchService Paths FileSystems
            StandardWatchEventKinds WatchEvent$Modifier]
           [com.sun.nio.file ExtendedWatchEventModifier]
           [java.util.concurrent TimeUnit]))

(def ^:dynamic *defaults* {:recursive true
                           :types :all
                           :mode :sync
                           :exclude [".*"]})

(defonce ^:dynamic *filewatchers* (atom {}))

(def event-kinds  [StandardWatchEventKinds/ENTRY_CREATE
                   StandardWatchEventKinds/ENTRY_DELETE
                   StandardWatchEventKinds/ENTRY_MODIFY])
(def event-types  [:create :delete :modify])
(def event-lookup (zipmap event-types event-kinds))
(def kind-lookup  (zipmap event-kinds event-types))

(defn pattern [s]
  (-> s
      (string/replace #"\." "\\\\\\Q.\\\\\\E")
      (string/replace #"\*" ".+")
      (re-pattern)))

(defn register-sub-directory
  [watcher dir-path]
  (let [{:keys [root seen options service excludes includes]} watcher]
    (when (and (or (= dir-path root)
                   (empty? includes)
                   (some #(re-find % (subs dir-path (-> root count inc))) includes))
               (not (or (get @seen dir-path)
                        (some #(re-find % (last (string/split dir-path #"/"))) excludes))))
      (let [dir (Paths/get dir-path (make-array String 0))]
        (.register dir service (into-array event-kinds)
                   (into-array [ExtendedWatchEventModifier/FILE_TREE]))
        (swap! seen conj dir-path)
      (if (:recursive options)
        (doseq [^java.io.File f (.listFiles (io/file dir-path))]
          (when (. f isDirectory)
            (register-sub-directory watcher (.getCanonicalPath f))))))
    watcher)))

(defn process-event [watcher kind ^java.io.File file]
  (let [{:keys [options callback excludes filters kinds]} watcher
        filepath (.getPath file)
        filename (.getName file)]
    (if (and (get kinds kind)
             (or  (empty? filters)
                  (some #(re-find % filename) filters)))
      (case (:mode options)
        :async (future (callback (kind-lookup kind) file))
        :sync  (callback (kind-lookup kind) file)))))

(defn process-deleted [watcher kind ^java.io.File file]
  (process-event watcher kind file))

(defn run-watcher [watcher]
  (let [^java.nio.file.WatchKey wkey
        (.take ^java.nio.file.WatchService (:service watcher))]
    (doseq [^java.nio.file.WatchEvent event (.pollEvents wkey)
            :when (not= (.kind event)
                        StandardWatchEventKinds/OVERFLOW)]
      (let [kind (.kind event)
            ^java.nio.file.Path path (.watchable wkey)
            ^java.nio.file.Path context (.context event)
            ^java.nio.file.Path res-path (.resolve path context)
            ^java.io.File file (.toFile res-path)]
        (when (= kind StandardWatchEventKinds/ENTRY_DELETE)
          ;; Deleted entries are neither a file nor a dir
          (process-deleted watcher kind file))
        (when (and (= kind StandardWatchEventKinds/ENTRY_CREATE)
                 (.isDirectory file)
                 (-> watcher :options :recursive))
          (register-sub-directory watcher (.getPath file)))
        (when (.isFile file)
          (process-event watcher kind file))))
    (.reset wkey)
    (recur watcher)))

(defn start-watcher [watcher]
  (let [{:keys [types filter exclude include]} (:options watcher)
        ^java.nio.file.WatchService service (.newWatchService (FileSystems/getDefault))
        seen    (atom #{})
        kinds   (if (= types :all)
                  (set event-kinds)
                  (->> types (map event-lookup) set))
        filters  (->> filter  (map pattern))
        excludes (->> exclude (map pattern))
        includes (->> include (map pattern))
        watcher  (->> (assoc watcher
                        :root (first (:paths watcher))
                        :service service
                        :seen seen
                        :filters filters
                        :excludes excludes
                        :includes includes
                        :kinds kinds))
        watcher  (reduce register-sub-directory watcher (:paths watcher))]
    (assoc watcher :running (future (run-watcher watcher)))))

(defn stop-watcher [watcher]
  (.close ^java.nio.file.WatchService (:service watcher))
  (future-cancel (:running watcher))
  (dissoc watcher :running :service :seen))

(defrecord Watcher [paths callback options]
  Object
  (toString [this]
    (str "#watcher" (assoc options :paths paths :running (-> this :running not not)))))

(defmethod print-method Watcher
  [v ^java.io.Writer w]
  (.write w (str v)))

(defn watcher
  "the watch interface provided for java.io.File
 
   (def ^:dynamic *happy* (promise))
 
   (watch/add (io/file \".\") :save
              (fn [f k _ [cmd file]]
                (watch/remove f k)
                (.delete file)
                (deliver *happy* [cmd (.getName file)]))
              {:types #{:create :modify}
               :recursive false
               :filter  [\".hara\"]
               :exclude [\".git\" \"target\"]})
 
   (watch/list (io/file \".\"))
   => (contains {:save fn?})
 
   (spit \"happy.hara\" \"hello\")
 
   @*happy*
   => [:create \"happy.hara\"]
 
   (watch/list (io/file \".\"))
   => {}"
  {:added "2.1"}
  [paths callback options]
  (let [paths   (if (coll? paths) paths [paths])]
    (Watcher. paths callback
              (merge-nil options *defaults*))))

(defn watch-callback [f root k]
  (fn [type file]
    (f root k nil [type file])))


(defn add-io-watch [obj k f opts]
  (let [path (.getCanonicalPath ^java.io.File obj)
          _    (if-let [wt (get-in @*filewatchers* [path k :watcher])]
                 (-remove-watch obj k nil))
          cb   (watch-callback f obj k)
          wt   (start-watcher (watcher path cb opts))]
      (swap! *filewatchers* assoc-in [path k] {:watcher wt :function f})
      obj))

(defn list-io-watch [obj _]
  (let [path (.getCanonicalPath ^java.io.File  obj)]
      (->> path (get @*filewatchers*)
           (reduce-kv (fn [i k v]
                        (assoc i k (:function v)))
                      {}))))

(defn remove-io-watch [obj k _]
  (let [path (.getCanonicalPath ^java.io.File obj)
        wt   (get-in @*filewatchers* [path k :watcher])]
    (if-not (nil? wt)
      (if (stop-watcher wt)
        (swap! *filewatchers* dissoc path)
        true)
      false)))

(extend-protocol IWatch
  java.io.File
  (-add-watch [obj k f opts]
    (add-io-watch obj k f opts))

  (-list-watch [obj _]
    (list-io-watch obj nil))

  (-remove-watch [obj k _]
    (remove-io-watch obj k nil)))
