(ns documentation.hara-concurrent-ova.walkthrough
  (:require [hara.concurrent.ova :refer :all]
            [hara.common.watch :as watch]
            [midje.sweet :refer :all]))

[[:section {:title "Constructor"}]]
"The key to `ova` lies in the ease of manipulating the postions of elements within an array as well as updating the elements themselves. We begin by constructing and displaying an ova."

[[{:numbered false}]]
(fact
  (def ov
    (ova [{:val 1} {:val 2}
          {:val 3} {:val 4}]))

  (-> ov class str)
  => "class hara.concurrent.ova.Ova")

[[:section {:title "Dereferencing"}]]
"An `ova` is a `ref` of a `vector` of `refs`. They are dereferenced accordingly:"
[[{:numbered false}]]
(fact
  (mapv deref (deref ov))
  => [{:val 1} {:val 2}
      {:val 3} {:val 4}]

  (<< ov)                     ;; Shorthand
  => [{:val 1} {:val 2}
      {:val 3} {:val 4}])

[[:section {:title "Append / Insert / Concat"}]]
"Adding elements to the ova is very straight forward:"

[[{:numbered false}]]
(fact
  (<< (append! ov {:val 6}))         ;; Append
  => [{:val 1} {:val 2} {:val 3}
      {:val 4} {:val 6}]

  (<< (insert! ov {:val 5} 4))       ;; Insert
  => [{:val 1} {:val 2} {:val 3}
      {:val 4} {:val 5} {:val 6}]

  (<< (concat! ov [{:val 7}          ;; Concat
                   {:val 8}]))
  => [{:val 1} {:val 2} {:val 3}
      {:val 4} {:val 5} {:val 6}
      {:val 7} {:val 8}])

[[:section {:title "Select"}]]
"Where `ova` really shines is in the mechanism by which elements are selected. There are abundant ways of selecting elements - by index, by sets, by vectors, by predicates and by lists. The specific mechanism will be described more clearly in later sections."

[[{:numbered false}]]
(fact
  (select ov 0)                      ;; By Index
  => #{{:val 1}}

  (select ov #{0 1})                 ;; By Set of Index
  => #{{:val 1} {:val 2}}

  (select ov {:val 3})               ;; By Item
  => #{{:val 3}}

  (select ov #{{:val 3} {:val 4}})   ;; By Set of Items
  => #{{:val 3} {:val 4}}

  (select ov #(-> % :val even?))     ;; By Predicate
  => #{{:val 2} {:val 4}
       {:val 6} {:val 8}}

  (select ov '(:val even?))          ;; By List
  => #{{:val 2} {:val 4}
       {:val 6} {:val 8}}

  (select ov [:val 3])               ;; By Vector/Value
  => #{{:val 3}}

  (select ov [:val #{1 2 3}])       ;; By Vector/Set
  => #{{:val 1} {:val 2} {:val 3}}

  (select ov [:val '(< 4)])         ;; By Vector/List
  => #{{:val 1} {:val 2} {:val 3}}

  (select ov [:val even?            ;; By Vector/Predicate/List
              :val '(> 4)])
  => #{{:val 6} {:val 8}})

[[:section {:title "Remove / Filter"}]]
"`remove!` and `filter!` also use the same mechanism as `select`:"

[[{:numbered false}]]
(fact
  (<< (remove! ov 7))               ;; Index Notation
  => [{:val 1} {:val 2} {:val 3}
      {:val 4} {:val 5} {:val 6}
      {:val 7}]

  (<< (filter! ov #{1 2 3 4 5 6}))  ;; Set Notation
  => [{:val 2} {:val 3} {:val 4}
      {:val 5} {:val 6} {:val 7}]

  (<< (filter! ov [:val odd?]))     ;; Vector/Fn Notation
  => [{:val 3} {:val 5} {:val 7}]

  (<< (remove! ov [:val '(> 3)]))   ;; List Notation
  => [{:val 3}])

[[:section {:title "Sorting"}]]
"The `sort!` functions allows elements in the ova to be rearranged. The function becomes clearer to read with access and comparison defined seperately (last example)."

[[{:numbered false}]]
(fact
  (def ov (ova (map (fn [n] {:val n})
                    (range 8))))

  (<< ov)
  => [{:val 0} {:val 1} {:val 2}
      {:val 3} {:val 4} {:val 5}
      {:val 6} {:val 7}]

  (<< (sort! ov (fn [a b]          ;; Fn
                  (> (:val a)
                     (:val b)))))
  => [{:val 7} {:val 6} {:val 5}
      {:val 4} {:val 3} {:val 2}
      {:val 1} {:val 0}]

  (<< (sort! ov [:val] <))         ;; Accessor/Comparater
  => [{:val 0} {:val 1} {:val 2}
      {:val 3} {:val 4} {:val 5}
      {:val 6} {:val 7}])

[[:section {:title "Manipulation"}]]
"Using the same mechanism as `select`, bulk update of elements within the `ova` can be performed in a succint manner:"

[[{:numbered false}]]
(fact
  (def ov (ova (map (fn [n] {:val n})
                    (range 4))))

  (<< ov)
  => [{:val 0} {:val 1} {:val 2} {:val 3}]

  (<< (map! ov update-in [:val] inc))        ;; map! updates all elements
  => [{:val 1} {:val 2} {:val 3} {:val 4}]

  (<< (smap! ov [:val odd?]                  ;; update only odd elements
             update-in [:val] #(+ 10 %)))
  => [{:val 11} {:val 2} {:val 13} {:val 4}]

  (<< (smap! ov 0 update-in                     ;; update element at index 0
          [:val] #(- % 10)))
  => [{:val 1} {:val 2} {:val 13} {:val 4}]

  (<< (smap! ov [:val 13]                       ;; update element with :val of 13
          update-in [:val] #(- % 10)))
  => [{:val 1} {:val 2} {:val 3} {:val 4}]

  (<< (smap! ov [:val even?]                    ;; assoc new data to even :vals
          assoc-in [:x :y :z] 10))
  => [{:val 1} {:val 2 :x {:y {:z 10}}}
      {:val 3} {:val 4 :x {:y {:z 10}}}]

  (<< (smap! ov [:x :y :z] dissoc :x))          ;; dissoc :x for elements with nested [:x :y :z] keys
  => [{:val 1} {:val 2} {:val 3} {:val 4}]
  )


[[:section {:title "Ova Watch"}]]
"Because a ova is simply a ref, it can be watched for changes"

[[{:numbered false}]]
(fact
  (def ov (ova [0 1 2 3 4 5]))

  (def output (atom []))
  (add-watch ov
             :old-new
             (fn [ov k p n]
               (swap! output conj [(mapv deref p)
                                   (mapv deref n)])))

  (do (dosync (sort! ov >))
      (deref output))
  => [[[0 1 2 3 4 5]
       [5 4 3 2 1 0]]])

[[:section {:title "Element Watch"}]]
"Entire elements of the ova can be watched. A more substantial example can be seen in the [scoreboard example](#scoreboard-example):"

[[{:numbered false}]]
(fact
  (def ov (ova [0 1 2 3 4 5]))

  (def output (atom []))

  (watch/add      ;; key, ova, ref, previous, next
      ov :elem-old-new
      (fn [k o r p n]
        (swap! output conj [p n])))

  (<< (!! ov 0 :zero))
  => [:zero 1 2 3 4 5]

  (deref output)
  => [[0 :zero]]

  (<< (!! ov 3 :three))
  => [:zero 1 2 :three 4 5]

  (deref output)
  => [[0 :zero] [3 :three]])

[[:subsection {:title "Element Change Watch"}]]
"The `add-elem-change-watch` function can be used to only notify when an element has changed."

[[{:numbered false}]]
(fact
  (def ov (ova [0 1 2 3 4 5]))

  (def output (atom []))

  (watch/add   ;; key, ova, ref, previous, next
     ov :elem-old-new
     (fn [k o r p n]
       (swap! output conj [p n]))
     {:select identity
      :diff true})

  (do (<< (!! ov 0 :zero))  ;; a pair is added to output
      (deref output))
  => [[0 :zero]]

  (do (<< (!! ov 0 0))      ;; another pair is added to output
      (deref output))
  => [[0 :zero] [:zero 0]]

  (do (<< (!! ov 0 0))      ;; no change to output
      (deref output))
  => [[0 :zero] [:zero 0]])

[[:section {:title "Clojure Protocols"}]]
"`ova` implements the sequence protocol so it is compatible with all the bread and butter methods."

[[{:numbered false}]]
(fact
  (def ov (ova (map (fn [n] {:val n})
                    (range 8))))

  (seq ov)
  => '({:val 0} {:val 1} {:val 2}
       {:val 3} {:val 4} {:val 5}
       {:val 6} {:val 7})

  (map #(update-in % [:val] inc) ov)
  => '({:val 1} {:val 2} {:val 3}
       {:val 4} {:val 5} {:val 6}
       {:val 7} {:val 8})

  (last ov)
  => {:val 7}

  (count ov)
  => 8

  (get ov 0)
  => {:val 0}

  (nth ov 3)
  => {:val 3}

  (ov 0)
  => {:val 0}

  (ov [:val] #{1 2 3}) ;; Gets the first that matches
  => {:val 1})
