(ns hara.reflect.core.class
  (:require [hara.reflect.common :as common]
            [hara.reflect.element.common :as element]
            [hara.class.inheritance :as inheritance]))

(defn class-info
  "Lists class information
 
   (class-info String)
   => (contains {:name \"java.lang.String\"
                 :hash anything
                 :modifiers #{:instance :class :public :final}})"
  {:added "2.1"}
  [obj]
  (select-keys (element/seed :class (common/context-class obj))
               [:name :hash :modifiers]))

(defn class-hierarchy
  "Lists the class and interface hierarchy for the class
 
   (class-hierarchy String)
   => [java.lang.String
       [java.lang.Object
        #{java.io.Serializable
          java.lang.Comparable
          java.lang.CharSequence}]]"
  {:added "2.1"}
  [obj]
  (let [t (common/context-class obj)]
    (vec (cons t (inheritance/ancestor-tree t)))))
