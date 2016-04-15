(ns hara.object.base-test
  (:use midje.sweet)
  (:require [hara.object.base :refer :all]))

^{:refer hara.object.base/map-transform-to :added "2.2"}
(fact "transforms a map back to the structure that it came from"
  (binding [*transform* {Class {[:authority :username] [:user]
                                [:authority :password] [:pass]}}]
    (map-transform-to {:authority {:username "chris"
                                   :password "pass"}} Class))
  => {:pass "pass", :user "chris"})

^{:refer hara.object.base/map-transform-from :added "2.2"}
(fact "transforms map to a new structure:"
  (binding [*transform* {Class {[:authority :username] [:user]
                                [:authority :password] [:pass]}}]
    (map-transform-from {:pass "pass", :user "chris"} Class))
  =>  {:authority {:username "chris"
                   :password "pass"}})

^{:refer hara.object.base/meta-object :added "2.2"}
(fact "gets meta-object for a particular class"
  (meta-object Class)
  => {:class java.lang.Class, :types #{}})


^{:refer hara.object.base/to-data :added "2.2"}
(fact "returns data from a java type"
  (to-data (into-array Object [{:a 1} 2 3]))
  => [{:a 1} 2 3])

^{:refer hara.object.base/from-data :added "2.2"}
(fact "creates a java type from data")
