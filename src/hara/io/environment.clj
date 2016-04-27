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
        (->> (path/split (System/getProperty "java.version") #"[\._-]")
             (take 4)
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

(defn satisfied
  "only attempts to load the files when the minimum versions have been met
   (env/satisfied {:java    {:major 1 :minor 7}
                   :clojure {:major 1 :minor 6}})
   => true"
  {:added "2.2"}
  [constraints]
  (let [satisfy (fn [current constraint]
                  (every? (fn [[label val]]
                            (>= (get current label) val))
                          (seq constraint)))]
    (when (every? (fn [[tag constraint]]
                    (let [current (version tag)]
                      (satisfy current constraint)))
                  (seq constraints))
      true)))

(defmacro init
  "only attempts to load the files when the minimum versions have been met
   (env/init {:java    {:major 1 :minor 8}
              :clojure {:major 1 :minor 6}}
            (:require [hara.time.data.zone
                        java-time-zoneid]
                       [hara.time.data.instant
                        java-time-instant]
                       [hara.time.data.format
                        java-time-format-datetimeformatter])
             (:import java.time.Instant))"
  {:added "2.2"}
  [constraints & statements]
  (if (satisfied constraints)
    (let [trans-fn (fn [[k & rest]]
                     (let [sym (symbol (str "clojure.core/" (name k)))]
                       (cons sym (map (fn [w]
                                              (if (keyword? w)
                                                w
                                                (list 'quote w)))
                                      rest))))]
      (cons 'do (map trans-fn statements)))))

(defmacro run
  "only runs the following code is the minimum versions have been met
   (env/run {:java    {:major 1 :minor 8}
             :clojure {:major 1 :minor 6}}
    (Instant/ofEpochMilli 0))"
  {:added "2.2"}
  [constraints & body]
  (if (satisfied constraints)
    (cons 'do body)))

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
