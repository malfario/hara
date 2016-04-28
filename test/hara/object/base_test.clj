(ns hara.object.base-test
  (:use midje.sweet)
  (:require [hara.object.base :refer :all]
            [hara.protocol.object :as object]
            [hara.reflect :as reflect])
  (:import [test PersonBuilder Person Dog DogBuilder Cat Pet]))

(defmethod object/-meta-write DogBuilder
  [_]
  {:empty (fn [_] (DogBuilder.))
   :methods (write-setters DogBuilder)})

(defmethod object/-meta-read DogBuilder
  [_]
  {:to-map (read-reflect-fields DogBuilder)})

(defmethod object/-meta-write Pet
  [_]
  {:from-map (fn [m] (case (:species m)
                       "dog" (from-map m Dog)
                       "cat" (from-map m Cat)))})

(defmethod object/-meta-read Pet
  [_]
  {:methods (read-getters Pet)})

(defmethod object/-meta-write Dog
  [_]
  {:from-map (fn [m] (-> m
                         (from-map DogBuilder)
                         (.build)))})

(defmethod object/-meta-write Cat
  [_]
  {:from-map (fn [m] (Cat. (:name m)))})

(defmethod object/-meta-write PersonBuilder
  [_]
  {:empty (fn [_] (PersonBuilder.))
   :methods (write-setters PersonBuilder)})

(defmethod object/-meta-read PersonBuilder
  [_]
  {:methods (read-reflect-fields PersonBuilder)})

(defmethod object/-meta-write Person
  [_]
  {:from-map (fn [m] (-> m
                         (from-map PersonBuilder)
                         (.build)))})

(defmethod object/-meta-read Person
  [_]
  {:methods (read-getters Person)})


^{:refer hara.object.base/meta-read :added "0.1"}
(fact "accesses the read-attributes of an object"

  (meta-read Pet)
  => (contains {:class test.Pet
                :methods (contains {:name fn? :species fn?})}))

^{:refer hara.object.base/meta-write :added "0.1"}
(fact "accesses the write-attributes of an object"

  (meta-write DogBuilder)
  => (contains {:class test.DogBuilder
                :empty fn?,
                :methods (contains
                          {:name
                           (contains {:type java.lang.String, :fn fn?})})}))

^{:refer hara.object.base/read-reflect-fields :added "0.1"}
(fact "fields of an object from reflection"
  (-> (read-reflect-fields Dog)
      keys)
  => [:name :species])

^{:refer hara.object.base/read-getters :added "0.1"}
(fact "returns fields of an object through getter methods"
  (-> (read-getters Dog)
      keys)
  => [:name :species])

^{:refer hara.object.base/write-reflect-fields :added "0.1"}
(fact "write fields of an object from reflection"
  (-> (write-reflect-fields Dog)
      keys)
  => [:name :species])

^{:refer hara.object.base/write-setters :added "0.1"}
(fact "write fields of an object through setter methods"
  (write-setters Dog)

  (keys (write-setters DogBuilder))
  => [:name])

^{:refer hara.object.base/from-empty :added "0.1"}
(fact "creates the object from an empty object constructor"
  (from-empty {:name "chris" :pet "dog"}
              (fn [_] (java.util.Hashtable.))
              {:name {:type String
                      :fn (fn [obj v]
                            (.put obj "hello" (keyword v))
                            obj)}
               :pet  {:type String
                      :fn (fn [obj v]
                            (.put obj "pet" (keyword v))
                            obj)}})
  => {"pet" :dog, "hello" :chris})

^{:refer hara.object.base/from-map :added "0.1"}
(fact "creates the object from a map"
  (-> {:name "chris" :age 30 :pets [{:name "slurp" :species "dog"}
                                    {:name "happy" :species "cat"}]}
      (from-map Person)
      (to-data))
  => {:name "chris", :age 30, :pets [{:name "slurp", :species "dog"}
                                     {:name "happy", :species "cat"}]})

^{:refer hara.object.base/to-data :added "0.1"}
(fact "creates the object from a string or map"
  (to-data "hello")
  => "hello"

  (to-data (from-map {:name "hello" :species "dog"} Pet))
  => {:name "hello", :species "dog"})

^{:refer hara.object.base/from-data :added "0.1"}
(fact "creates the object from data"
  (-> (from-data ["hello"] (Class/forName "[Ljava.lang.String;"))
      seq)
  => ["hello"])
