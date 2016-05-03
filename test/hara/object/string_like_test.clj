(ns hara.object.string-like-test
  (:use midje.sweet)
  (:require [hara.object.string-like :refer :all]
            [hara.object :as object]))

^{:refer hara.object.string-like/extend-string-like :added "2.3"}
(fact "creates an entry for string-like classes"

  (extend-string-like
   java.io.File
   {:tag "path"
    :read (fn [f] (.getPath f))
    :write (fn [^String path] (java.io.File. path))})

  (object/from-data "/home" java.io.File)

  (with-out-str
    (prn (java.io.File. "/home")))
  => "#path \"/home\"\n")

