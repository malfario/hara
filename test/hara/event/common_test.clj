(ns hara.event.common-test
  (:use midje.sweet)
  (:require [hara.event.common :refer :all]))

^{:refer hara.event.common/expand-data :added "2.2"}
(fact "expands shorthand data into a map"
  
  (expand-data :hello)
  => {:hello true}

  (expand-data [:hello {:world "foo"}])
  => {:world "foo", :hello true})

^{:refer hara.event.common/check-data :added "2.2"}
(fact "checks to see if the data corresponds to a template"

  (check-data {:hello true} :hello)
  => true

  (check-data {:hello true} {:hello true?})
  => true

  (check-data {:hello true} '_)
  => true

  (check-data {:hello true} #{:hello})
  => true)

^{:refer hara.event.common/add-handler :added "2.2"}
(fact "adds a handler to the manager"
  (-> (add-handler (manager) :hello {:id :hello
                                     :handler identity})
      (match-handlers {:hello "world"})
      (count))
  => 1)

^{:refer hara.event.common/remove-handler :added "2.2"}
(fact "adds a handler to the manager"
  (-> (add-handler (manager) :hello {:id :hello
                                     :handler identity})
      (remove-handler :hello)
      (match-handlers {:hello "world"}))
  => ())
