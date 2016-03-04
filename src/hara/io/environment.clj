(ns hara.io.environment
  (:require [hara.string.path :as path])
  (:refer-clojure :exclude [require clojure-version]))

(defrecord Properties [])

(defmethod print-method Properties
  [v ^java.io.Writer w]
  (.write w (str "#props" (vec (keys v)))))

(defn clojure-version
  "returns the current clojure version
   (env/clojure-version)
   => (contains
       {:major anything,
        :minor anything,
        :incremental anything
        :qualifier anything})"
  {:added "2.2"}
  []
  *clojure-version*)

(defn java-version
  "returns the current java version
   (env/java-version)
   => (contains
       {:major anything,
        :minor anything,
        :incremental anything
        :qualifier anything})"
  {:added "2.2"}
  []
  (let [[major minor incremental qualifier]
        (->> (path/split (System/getProperty "java.version") #"[\._]")
             (map #(Long/parseLong %)))]
    {:major major
     :minor minor
     :incremental incremental
     :qualifier qualifier}))

(defn version
  "alternate way of getting clojure and java version
   (env/version :clojure)
   => (env/clojure-version)

   (env/version :java)
   => (env/java-version)"
  {:added "2.2"}
  [tag]
  (case tag
    :clojure (clojure-version)
    :java    (java-version)))

(defn require
  "only attempts to load the files when the minimum versions have been met
   (env/require {:java    {:major 1 :minor 8}
                 :clojure {:major 1 :minor 6}}
               '[hara.time.data.zone
                  java-time-zoneid]
                '[hara.time.data.instant
                  java-time-instant]
                '[hara.time.format
                  java-time-format-datetimeformatter])"
  {:added "2.2"}
  [constraints & libs]
  (let [satisfy (fn [current constraint]
                  (every? (fn [[label val]]
                            (>= (get current label) val))
                          (seq constraint)))]
    (when (every? (fn [[tag constraint]]
                    (let [current (version tag)]
                      (satisfy current constraint)))
                  (seq constraints))
        (apply clojure.core/require libs))))

(defn properties
  "returns jvm properties in a nested map for easy access
   (->> (env/properties)
        :os)
   => (contains {:arch anything
                 :name anything
                 :version anything})"
  {:added "2.2"}
  []
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
