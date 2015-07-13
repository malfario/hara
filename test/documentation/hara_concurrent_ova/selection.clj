(ns documentation..hara-concurrent-ova.selection
  (:require [hara.concurrent.ova :refer :all]
            [midje.sweet :refer :all]))

"There are a number of ways elements in an `ova` can be selected. The library uses custom syntax to provide a shorthand for element selection. We use the function `indices` in order to give an examples of how searches can be expressed. Most of the functions like `select`, `remove!`, `filter!`, `smap!`, `smap-indexed!`, and convenience macros are all built on top of the `indices` function and so can be used accordingly once the convention is understood."

[[:section {:title "by index"}]]
"The most straight-forward being the index itself, represented using a number."

[[{:numbered false}]]
(fact
  (def ov (ova [{:v 0, :a {:c 4}}    ;; 0
                {:v 1, :a {:d 3}}    ;; 1
                {:v 2, :b {:c 2}}    ;; 2
                {:v 3, :b {:d 1}}])) ;; 3

  (indices ov)           ;; return all indices
  => [0 1 2 3]

  (indices ov 0)         ;; return indices of the 0th element
  => [0]

  (indices ov 10)        ;; return indices of the 10th element
  => [])

[[:section {:title "by value"}]]
"A less common way is to search for indices by value."

[[{:numbered false}]]
(fact
  (indices ov            ;; return indices of elements matching term
           {:v 0 :a {:c 4}})
  => [0])


[[:section {:title "by predicate"}]]
"Most of the time, predicates are used. They allow selection of any element returning a non-nil value when evaluated against the predicate. Predicates can take the form of functions, keywords or list representation."

[[{:numbered false}]]
(fact
  (indices ov #(get % :a))   ;; retur indicies where (:a elem) is non-nil

  => [0 1]

  (indices ov #(:a %))       ;; more succint function form

  => [0 1]

  (indices ov :a)            ;; keyword form, same as #(:a %)

  => [0 1]

  (indices ov '(get :a))     ;; list form, same as #(get % :a)

  => [0 1]

  (indices ov '(:a))         ;; list form, same as #(:a %)

  => [0 1])

[[:section {:title "by sets (or)"}]]
"sets can be used to compose more complex searches by acting as an `union` operator over its members"

[[{:numbered false}]]
(fact
  (indices ov #{0 1})        ;; return indices 0 and 1
  => [0 1]

  (indices ov #{:a 2})       ;; return indices of searching for both 2 and :a
  => (just [0 1 2] :in-any-order)

  (indices ov #{'(:a)        ;; a more complex example
                #(= (:v %) 2)})
  => (just [0 1 2] :in-any-order))

[[:section {:title "by vectors (and)"}]]
"vectors can be used to combine predicates for more selective filtering of elements"

[[{:numbered false}]]
(fact
  (indices ov [:v 0])        ;; return indicies where (:a ele) = {:c 4}
  => [0]

  (indices ov [:v '(= 0)])   ;; return indicies where (:a ele) = {:c 4}
  => [0]

  (indices ov [:a #(% :c)])  ;; return indicies where (:a ele) has a :c element
  => [0]

  (indices ov [:a '(:c)])    ;; with list predicate
  => [0]

  (indices ov [:a :c])       ;; with keyword predicate
  => [0]

  (indices ov [:v odd?       ;; combining predicates
               :v '(> 1)])
  => [3]

  (indices ov #{[:a :c] 2})  ;; used within a set

  => (just [0 2] :in-any-order))


[[:section {:title "accessing nested elements"}]]
"When dealing with nested maps, a vector can be used instead of a keyword to specify rules of selection using nested elements"
(fact
  (indices ov [[:b :c] 2])   ;; with value
  => [2]

  (indices ov [[:v] '(< 3)]) ;; with predicate
  => [0 1 2]

  (indices ov [:v 2          ;; combining in vector
               [:b :c] 2])
  => [2])
