(ns hara.data.diff-test
  (:use midje.sweet)
  (:require [hara.data.diff :refer :all]))

^{:refer hara.data.diff/diff-changes :added "2.1"}
(fact "Finds changes in nested maps, does not consider new elements"
  
  (diff-changes {:a 2} {:a 1})
  => {[:a] 2}

  (diff-changes {:a {:b 1 :c 2}} {:a {:b 1 :c 3}})
  => {[:a :c] 2}

  ^:hidden
  (diff-changes {:a 1 :b 2 :c 3} {:a 1 :b 2})
  => {}

  (diff-changes {:a 1 :b 2} {:a 1 :b 2 :c 3})
  => {})

^{:refer hara.data.diff/diff-new :added "2.1"}
(fact "Finds new elements in nested maps, does not consider changes"
  
  (diff-new {:a 2} {:a 1})
  => {}

  (diff-new {:a {:b 1}} {:a {:c 2}})
  => {[:a :b] 1}

  ^:hidden
  (diff-new {:a {:b 1 :c 2}} {:a {:b 1 :c 3}})
  => {}
  
  (diff-new {:a 1 :b 2 :c 3} {:a 1 :b 2})
  => {[:c] 3}

  (diff-new {:a 1 :b 2} {:a 1 :b 2 :c 3})
  => {})

^{:refer hara.data.diff/diff :added "2.1"}
(fact "Finds the difference between two maps"
  
  (diff {:a 2} {:a 1})
  => {:+ {} :- {} :> {[:a] 2} :< {[:a] 1}}

  (diff {:a {:b 1 :d 3}} {:a {:c 2 :d 4}})
  => {:+ {[:a :b] 1}
      :- {[:a :c] 2}
      :> {[:a :d] 3}
      :< {[:a :d] 4}})

^{:refer hara.data.diff/patch :added "2.1"}
(fact "Use the diff to convert one map to another in the forward 
  direction based upon changes between the two."
  
  (let [m1  {:a {:b 1 :d 3}}
        m2  {:a {:c 2 :d 4}}
        df  (diff m2 m1)]
    (patch m1 df)
    => m2))

^{:refer hara.data.diff/unpatch :added "2.1"}
(fact "Use the diff to convert one map to another in the reverse 
  direction based upon changes between the two."
  
  (let [m1  {:a {:b 1 :d 3}}
        m2  {:a {:c 2 :d 4}}
        df  (diff m2 m1)]
    (unpatch m2 df)
    => m1))
