(ns hara.io.scheduler.tab
  (:require [clojure.string :refer [split]]
            [hara.common.error :refer [error suppress]]
            [hara.common.primitives :refer [F]]
            [hara.time :as t]))

(def +schedule-elements+ [:second :minute :hour :day-of-week :day :month :year])

 ;; There are 2 different representations of schedular tab data:
 ;;   string: (for humans)        "   *       2,4      2-9         /8      ...  "
 ;;    array: (for efficiency)    [ (-*)   [2 4 6]   (-- 2 9)   (-- 8)     ...  ]
 ;;
 ;;            :tab-str                   :tab-arr
 ;;           +---------+                 +---------+
 ;;           |         |                 |         |
 ;;           |         |                 |         |
 ;;           | string  |    -----------> |  array  |
 ;;           |         |    parse-tab    |         |
 ;;           |         |                 |         |
 ;;           +---------+                 +---------+
 ;;            for human                    used in
 ;;            use to add                  cronj-loop
 ;;            tasks

;; Methods for type conversion
(defn- to-long [x] (Long/parseLong x))

;; Array Representation
(defn *-
  "takes a string and returns something
   (*-) => :*
 
   (map (*- 2) (range 60))
   => (map even? (range 60))
 
   "
  {:added "2.2"}
  ([]      :*)
  ([s]     (fn [v] (zero? (mod v s))))
  ([a b]   (fn [v] (and (>= v a) (<= v b))))
  ([a b s] (fn [v] (and (>= v a)
                        (<= v b)
                        (zero? (mod (- v a) s))))))

;; String to Array Methods
(defn parse-tab-element [^String es]
  (cond (= es "*") :*
        (re-find #"^\d+$" es) (to-long es)
        (re-find #"^/\d+$" es) (*- (to-long (.substring es 1)))
        (re-find #"^\d+-\d+$" es)
        (apply *-
               (sort (map to-long (split es #"-"))))
        (re-find #"^\d+-\d+/\d$" es)
        (apply *-
               (map to-long (split es #"[-/]")))
        :else (error es " is not in the right format.")))

(defn parse-tab-group [s]
  (let [e-toks (re-seq #"[^,]+" s)]
    (map parse-tab-element e-toks)))

(defn parse-tab
  "takes a string and creates matches
 
   (parse-tab \"* * * * * * *\")
   => '[(:*) (:*) (:*) (:*) (:*) (:*) (:*)]
 
   (parse-tab \"* * * * * *\")
   => '[(0) (:*) (:*) (:*) (:*) (:*) (:*)]
 
   (parse-tab \"* * * * *\")
   => (throws Exception)
 
   "
  {:added "2.2"}
  [s]
  (let [c-toks (re-seq #"[^\s]+" s)
        len-c (count c-toks)
        sch-c  (count +schedule-elements+)]
    (cond (= sch-c len-c) (map parse-tab-group c-toks)
          (= (dec sch-c) len-c) (map parse-tab-group (cons "0" c-toks))
          :else
          (error "The schedule " s
                 " does not have the correct number of elements."))))

(defn valid-tab? [s]
  (suppress (if (parse-tab s) true)))

;; dt-arr methods
(defn to-time-array
  "takes a time element and returns an array representation
   
   (to-time-array #inst \"1970-01-01T00:00:00.000-00:00\" \"UTC\")
   => [0 0 0 5 1 1 1970]
 
   (to-time-array #inst \"1970-01-01T00:00:00.000-00:00\" \"GMT-10\")
   => [0 0 14 4 31 12 1969]"
  {:added "2.2"}
  ([t] (to-time-array t (t/local-timezone)))
  ([t tz]
   (t/to-vector t {:timezone tz} +schedule-elements+)))

(defn match-element?
  "takes an element of the array and compares with a single matcher
 
   (match-element? 1 :*)
   => true
   
   (match-element? 1 [2 3 4])
   => false
 
   "
  {:added "2.2"}
  [dt-e tab-e]
  (or (cond (= tab-e :*) true
            (= tab-e dt-e) true
            (fn? tab-e) (tab-e dt-e)
            (sequential? tab-e) (some #(match-element? dt-e %) tab-e))
      false))

(defn match-array?
  "takes an array representation for match comparison
 
   (match-array? [30 14 0 4 26 7 2012]
                 [(*- 0 60 5) (*-) (*-) (*-) (*-) (*-) (*-)])
   => true
 
   (match-array? [31 14 0 4 26 7 2012]
                 [(*- 0 60 5) (*-) (*-) (*-) (*-) (*-) (*-)])
   => false"
  {:added "2.2"}
  [dt-array tab-array]
  (every? true?
          (map match-element? dt-array tab-array)))

(def nil-array [F F F F F F F])
