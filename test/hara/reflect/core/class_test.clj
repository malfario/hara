(ns hara.reflect.core.class-test
  (:use midje.sweet)
  (:require [hara.reflect.core.class :refer :all]))

^{:refer hara.reflect.core.class/class-info :added "2.1"}
(fact "Lists class information"

  (class-info String)
  => (contains {:name "java.lang.String"
                :hash anything
                :modifiers #{:instance :class :public :final}}))

^{:refer hara.reflect.core.class/class-hierarchy :added "2.1"}
(fact "Lists the class and interface hierarchy for the class"

  (class-hierarchy String)
  => [java.lang.String
      [java.lang.Object
       #{java.io.Serializable
         java.lang.Comparable
         java.lang.CharSequence}]])
