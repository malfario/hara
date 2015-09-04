(ns hara.group-test
  (:use midje.sweet)
  (:require [hara.group :refer :all]))

(defrecord Person [])

(defmethod print-method
  Person
  [v ^java.io.Writer w]
  (.write w (str "#person" (into {} v))))

^{:refer hara.group/defgroup :added "2.2"}
(fact "creates a group of items"
  (defgroup people
    {:tag :people
     :constructor map->Person
     :items [{:name :andy}
             {:name :bob}]})
  => (comp group? deref))

^{:refer hara.group/defitem :added "2.2"}
(fact "adds an item to the group"
  (-> (defitem people {:name :chris})
      deref
      list-items)
  => [:andy :bob :chris]
  ^:hidden
  (remove-item people :chris))

^{:refer hara.group/group :added "2.2"}
(fact "creates a group from a map"
  (group {:tag :hello})
  => #(-> % :tag (= :hello)))

^{:refer hara.group/group? :added "2.2"}
(fact "checks to see if an element is a group"
  (group? people)
  => true)

^{:refer hara.group/list-items :added "2.2"}
(fact "returns a list of keys to items in the group"

  (list-items people)
  => [:andy :bob])

^{:refer hara.group/add-item :added "2.2"}
(fact "adds an item to the group"
  (-> (add-item people {:name :chris})
      (list-items))
  => [:andy :bob :chris])

^{:refer hara.group/find-item :added "2.2"}
(fact "finds an item based on the given key"
  (find-item people :andy)
  => {:name :andy})

^{:refer hara.group/remove-item :added "2.2"}
(fact "removes items based on the key"
  (-> (remove-item people :chris)
      (list-items))
  => [:andy :bob])

^{:refer hara.group/append-items :added "2.2"}
(fact "appends a set of data to the group"
  (-> (append-items people [{:name :dave} {:name :erin}])
      (list-items))
  => [:andy :bob :dave :erin]
  ^:hidden
  (-> people
      (remove-item :dave)
      (remove-item :erin)))

^{:refer hara.group/install-items :added "2.2"}
(fact "reads a set of data from a resource and loads it into the group"
  (-> (install-items people (java.io.StringReader.
                             "[{:name :dave} {:name :erin}]"))
      (list-items))
  => [:andy :bob :dave :erin]
  ^:hidden
  (-> people
      (remove-item :dave)
      (remove-item :erin)))
