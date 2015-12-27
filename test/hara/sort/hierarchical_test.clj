(ns hara.sort.hierarchical-test
  (:use midje.sweet)
  (:require [hara.sort.hierarchical :refer :all]))

^{:refer hara.sort.hierarchical/top-node :added "2.2"}
(fact "find the top node for the hierarchy of descendants"
  (top-node {1 #{2 3 4 5 6}
             2 #{3 5 6}
             3 #{5 6}
             4 #{}
             5 #{6} 
             6 #{}})
  => 1)

^{:refer hara.sort.hierarchical/hierarchical-sort :added "2.2"}
(fact "prunes a hierarchy of descendants into a directed graph"
  (hierarchical-sort {1 #{2 3 4 5 6}
                      2 #{3 5 6}
                      3 #{5 6}
                      4 #{}
                      5 #{6} 
                      6 #{}})
  => {1 #{4 2}
      2 #{3}
      3 #{5}
      4 #{}
      5 #{6}
      6 #{} })
