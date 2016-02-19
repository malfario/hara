(ns hara.io.environment
  (:require [hara.string.path :as path])
  (:refer-clojure :exclude [require]))

(defrecord Properties [])

(defmethod print-method Properties
  [v ^java.io.Writer w]
  (.write w (str "#props" (vec (keys v)))))

(defn clojure-version []
  *clojure-version*)

(defn java-version []
  (let [[major minor incremental qualifier]
        (->> (path/split (System/getProperty "java.version") #"[\._]")
             (map #(Long/parseLong %)))]
    {:major major
     :minor minor
     :incremental incremental
     :qualifier qualifier}))

(defn version [tag]
  (case tag
    :clojure (clojure-version)
    :java    (java-version)))

(defn require [constraints & libs]
  (let [satisfy (fn [current constraint]
                  (every? (fn [[label val]]
                            (>= (get current label) val))
                          (seq constraint)))]
    (when (every? (fn [[tag constraint]]
                    (let [current (version tag)]
                      (satisfy current constraint)))
                  (seq constraints))
        (apply clojure.core/require libs))))

(defn properties []
  (->> (System/getProperties)
     (reduce (fn [out [k v]]
               (conj out [(path/split (keyword k) #"\.") v]))
             [])
     (sort)
     (reverse)
     (reduce (fn [out [k v]]
               (if (get-in out k)
                 (assoc-in out (conj k :name) v)
                 (assoc-in out k v)))
             (Properties.))))

(comment

  
  (require {:java    {:major 1 :minor 8}
            :clojure {:major 1 :minor 7}}
           '[hara.time.instant.java-instant-time])
  
  (:clojure (properties))
  {:debug "false", :compile {:path "/Users/chris/Development/chit/hara/target/classes"}})









