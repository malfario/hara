(ns hara.io.environment-test
  (:use midje.sweet)
  (:require [hara.io.environment :as env]))

^{:refer hara.io.environment/clojure-version :added "2.2"}
(fact "returns the current clojure version"
  (env/clojure-version)
  => (contains
      {:major anything,
       :minor anything,
       :incremental anything
       :qualifier anything}))

^{:refer hara.io.environment/java-version :added "2.2"}
(fact "returns the current java version"
  (env/java-version)
  => (contains
      {:major anything,
       :minor anything,
       :incremental anything
       :qualifier anything}))

^{:refer hara.io.environment/version :added "2.2"}
(fact "alternate way of getting clojure and java version"
  (env/version :clojure)
  => (env/clojure-version)

  (env/version :java)
  => (env/java-version))

^{:refer hara.io.environment/satisfied :added "2.2"}
(fact "only attempts to load the files when the minimum versions have been met"
  (env/satisfied {:java    {:major 1 :minor 7}
                  :clojure {:major 1 :minor 6}})
  => true)


^{:refer hara.io.environment/init :added "2.2"}
(fact "only attempts to load the files when the minimum versions have been met"
  (env/init {:java    {:major 1 :minor 8}
             :clojure {:major 1 :minor 6}}
            (:require [hara.time.data.zone
                       java-time-zoneid]
                      [hara.time.data.instant
                       java-time-instant]
                      [hara.time.data.format
                       java-time-format-datetimeformatter])
            (:import java.time.Instant)))

^{:refer hara.io.environment/run :added "2.2"}
(fact "only runs the following code is the minimum versions have been met"
  (env/run {:java    {:major 1 :minor 8}
            :clojure {:major 1 :minor 6}}
    (Instant/ofEpochMilli 0)))

^{:refer hara.io.environment/properties :added "2.2"}
(fact "returns jvm properties in a nested map for easy access"
  (->> (env/properties)
       :os)
  => (contains {:arch anything
                :name anything
                :version anything}))
