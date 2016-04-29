(ns hara.object.string-like-test
  (:use midje.sweet)
  (:require [hara.object.string-like :refer :all]))

^{:refer hara.object.string-like/extend-string-like :added "2.3"}
(fact "creates an entry for string-like classes"

  (extend-string-like
   java.io.File
   {:tag "path"
    :read .getPath
    :write (fn [^String path _] (java.io.File. path))})

  (with-out-str
    (prn (java.io.File. "/home")))
  => "#path \"/home\"\n")

