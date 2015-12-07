(ns hara.data.record-test
  (:use midje.sweet)
  (:require [hara.data.seq :refer :all]))

^{:refer hara.data.seq/positions :added "2.2"}
(fact "find positions of elements matching the predicate"
  (positions even? [5 5 4 4 3 3 2 2])
  => [2 3 6 7])

^{:refer hara.data.seq/remove-index :added "2.2"}
(fact "removes element at the specified index"
  (remove-index [:a :b :c :d] 2)
  => [:a :b :d])
