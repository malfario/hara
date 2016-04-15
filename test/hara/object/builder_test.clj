(ns hara.object.builder-test
  (:use midje.sweet)
  (:require [hara.object.builder :refer :all]))

^{:refer hara.object.builder/build :added "2.2"}
(fact "creates an object from a build template")
