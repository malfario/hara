(ns hara.object.util-test
  (:use midje.sweet)
  (:require [hara.object.util :refer :all]
            [hara.reflect.types.element :as element])
  (:import java.util.Date))

^{:refer hara.object.util/java->clojure :added "2.2"}
(fact "turns a java name into a clojure one."

  (java->clojure "getKebabCase") => "kebab-case"

  (java->clojure "setKebabCase") => "kebab-case"

  (java->clojure "isKebabCase")  => "kebab-case?"

  (java->clojure "hasKebabCase") => "kebab-case!")


^{:refer hara.object.util/clojure->java :added "2.2"}
(fact "turns a clojure name into a java one."

  (clojure->java "camel-case") => "getCamelCase"

  (clojure->java "camel-case?") => "isCamelCase"

  (clojure->java "camel-case!") => "hasCamelCase")


^{:refer hara.object.util/object-getters :added "2.2"}
(fact "finds all the reflected functions that act as getters."

  (object-getters [])
  => (just {:empty? element/element?
            :class  element/element?}))


^{:refer hara.object.util/object-setters :added "2.2"}
(fact "finds all the reflected functions that act as setters."

  (object-setters (java.util.Date.))
  => (contains {:year element/element?
                :time element/element?
                :seconds element/element?
                :month element/element?
                :minutes element/element?
                :hours element/element?
                :date element/element?}))

^{:refer hara.object.util/object-apply :added "2.2"}
(fact "applies a map of functions to an object yielding a result of the same shape."
  (object-apply {:year #(.getYear ^Date %)
                 :month #(.getMonth ^Date %)}
                (Date. 0) identity)
  => {:month 0 :year 70})


^{:refer hara.object.util/object-data :added "2.2"}
(fact "retrieves the data within the class as a map (like bean)"

  (object-data (Date. 0))
  => (contains {:day 4
                :date 1
                :time 0
                :month 0
                :seconds 0
                :year 70
                :class Date
                :hours 5
                :minutes 30})
  )
