(ns hara.object.enum-test
  (:use midje.sweet)
  (:require [hara.object.enum :refer :all]
            [hara.object.base :as base])
  (:import java.lang.annotation.ElementType))

^{:refer hara.object.enum/enum? :added "2.2"}
(fact "Check to see if class is an enum type"

  (enum? java.lang.annotation.ElementType) => true

  (enum? String) => false)

^{:refer hara.object.enum/enum-values :added "2.2"}
(fact "Returns all values of an enum type"
  
  (->> (enum-values ElementType)
       (map str))
  => (contains "TYPE" "FIELD" "METHOD" "PARAMETER" "CONSTRUCTOR"
               :in-any-order :gaps-ok))

^{:refer hara.object.base/from-data-enum :added "2.2"}
(fact "from-data works with enum"
  (base/from-data "CONSTRUCTOR" ElementType)
  => ElementType/CONSTRUCTOR)
