(ns hara.object.base-test
  (:use midje.sweet)
  (:require [hara.object.read :as read]
            [hara.object.write :as write]
            [hara.protocol.object :as object]
            [hara.reflect :as reflect])
  (:import [test PersonBuilder Person Dog DogBuilder Cat Pet]))

(defmethod object/-meta-write DogBuilder
  [_]
  {:empty (fn [_] (DogBuilder.))
   :methods (write/write-setters DogBuilder)})

(defmethod object/-meta-read DogBuilder
  [_]
  {:to-map (read/read-reflect-fields DogBuilder)})

(defmethod object/-meta-write Pet
  [_]
  {:from-map (fn [m] (case (:species m)
                       "dog" (write/from-map m Dog)
                       "cat" (write/from-map m Cat)))})

(defmethod object/-meta-read Pet
  [_]
  {:methods (read/read-getters Pet)})

(defmethod object/-meta-write Dog
  [_]
  {:from-map (fn [m] (-> m
                         (write/from-map DogBuilder)
                         (.build)))})

(defmethod object/-meta-write Cat
  [_]
  {:from-map (fn [m] (Cat. (:name m)))})

(defmethod object/-meta-write PersonBuilder
  [_]
  {:empty (fn [_] (PersonBuilder.))
   :methods (write/write-setters PersonBuilder)})

(defmethod object/-meta-read PersonBuilder
  [_]
  {:methods (read/read-reflect-fields PersonBuilder)})

(defmethod object/-meta-write Person
  [_]
  {:from-map (fn [m] (-> m
                         (write/from-map PersonBuilder)
                         (.build)))})

(defmethod object/-meta-read Person
  [_]
  {:methods (read/read-getters Person)})

^{:refer hara.object.read/meta-read :added "0.1"}
(fact "accesses the read-attributes of an object"

  (read/meta-read Pet)
  => (contains {:class test.Pet
                :methods (contains {:name fn? :species fn?})}))

^{:refer hara.object.write/meta-write :added "0.1"}
(fact "accesses the write-attributes of an object"

  (write/meta-write DogBuilder)
  => (contains {:class test.DogBuilder
                :empty fn?,
                :methods (contains
                          {:name
                           (contains {:type java.lang.String, :fn fn?})})}))

^{:refer hara.object.read/read-reflect-fields :added "0.1"}
(fact "fields of an object from reflection"
  (-> (read/read-reflect-fields Dog)
      keys)
  => [:name :species])

^{:refer hara.object.read/read-getters :added "0.1"}
(fact "returns fields of an object through getter methods"
  (-> (read/read-getters Dog)
      keys)
  => [:name :species])

^{:refer hara.object.write/write-reflect-fields :added "0.1"}
(fact "write fields of an object from reflection"
  (-> (write/write-reflect-fields Dog)
      keys)
  => [:name :species])

^{:refer hara.object.write/write-setters :added "0.1"}
(fact "write fields of an object through setter methods"
  (write/write-setters Dog)
  => {}

  (keys (write/write-setters DogBuilder))
  => [:name])

^{:refer hara.object.write/from-empty :added "0.1"}
(fact "creates the object from an empty object constructor"
  (write/from-empty {:name "chris" :pet "dog"}
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

^{:refer hara.object.write/from-map :added "0.1"}
(fact "creates the object from a map"
  (-> {:name "chris" :age 30 :pets [{:name "slurp" :species "dog"}
                                    {:name "happy" :species "cat"}]}
      (write/from-map Person)
      (read/to-data))
  => {:name "chris", :age 30, :pets [{:name "slurp", :species "dog"}
                                     {:name "happy", :species "cat"}]})

^{:refer hara.objectr.read/to-data :added "0.1"}
(fact "creates the object from a string or map"
  (read/to-data "hello")
  => "hello"

  (read/to-data (write/from-map {:name "hello" :species "dog"} Pet))
  => {:name "hello", :species "dog"})

^{:refer hara.object.write/from-data :added "0.1"}
(fact "creates the object from data"
  (-> (write/from-data ["hello"] (Class/forName "[Ljava.lang.String;"))
      seq)
  => ["hello"])
