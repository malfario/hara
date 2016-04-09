(ns hara.io.classloader-test
  (:use midje.sweet)
  (:require [hara.io.classloader :refer :all]))

^{:refer hara.io.classloader/delegation :added "2.2"}
(fact "returns a list of classloaders in order of top to bottom"
  (-> (Thread/currentThread)
      (.getContextClassLoader)
      (delegation)
      count)
  => #(> % 3))

^{:refer hara.io.classloader/to-url :added "2.2"}
(fact "constructs a `java.net.URL` object from a string"
  (str (to-url "/dev/null"))
  => "file:/dev/null")

^{:refer hara.io.classloader/url-classloader :added "2.2"}
(fact "returns a `java.net.URLClassLoader` from a list of strings"
  (->> (url-classloader ["/dev/null"])
       (.getURLs)
       (map str))
  => ["file:/dev/null"])

^{:refer hara.io.classloader/eval-in :added "2.2"}
(fact "given an environment, evaluates a form")

^{:refer hara.io.classloader/new-env :added "2.2"}
(fact "creates an new environment for isolated class loading")
