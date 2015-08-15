(ns hara.object.util
  (:require [hara.string.case :as case]
            [hara.data.map :as map]
            [hara.reflect :as reflect]
            [hara.event :as event]))

(defn java->clojure
  "turns a java name into a clojure one.
 
   (java->clojure \"getKebabCase\") => kebab-case
 
   (java->clojure \"setKebabCase\") => kebab-case
 
   (java->clojure \"isKebabCase\")  => kebab-case?
 
   (java->clojure \"hasKebabCase\") => kebab-case!"
  {:added "2.2"}
  [^String name]
  (let [nname (cond (re-find #"(^get)|(^set)[A-Z].+" name)
                    (subs name 3)

                    (re-find #"^is[A-Z].+" name)
                    (str (subs name 2) "?")

                    (re-find #"^has[A-Z].+" name)
                    (str (subs name 3) "!")

                    :else name)]
    (case/spear-case nname)))

(defn clojure->java
  "turns a clojure name into a java one.
 
   (clojure->java \"camel-case\") => getCamelCase
 
   (clojure->java \"camel-case?\") => isCamelCase
 
   (clojure->java \"camel-case!\") => hasCamelCase"
  {:added "2.2"}
  ([name] (clojure->java name :get))
  ([^String name suffix]
   (let [nname (cond (.endsWith name "?")
                     (str "is-" (.substring name 0 (dec (.length name))))

                     (.endsWith name "!")
                     (str "has-" (.substring name 0 (dec (.length name))))

                     :else
                     (str (clojure.core/name suffix) "-" name))]
     (case/camel-case nname))))

(defn object-getters
  "finds all the reflected functions that act as getters.
 
   (object-getters [])
   => (just {:empty? element/element?
             :class  element/element?})"
  {:added "2.2"}
  ([obj]
   (if obj
     (->> (reflect/query-hierarchy obj [#"(^get)|(^is)|(^has)[A-Z].+" 1 :instance])
          (reduce (fn [m ele]
                    (assoc m (-> ele :name java->clojure keyword) ele))
                  {}))
     {})))

(defn object-setters
  "finds all the reflected functions that act as setters.
 
   (object-setters (java.util.Date.))
   => (contains {:year element/element?
                 :time element/element?
                 :seconds element/element?
                 :month element/element?
                 :minutes element/element?
                :hours element/element?
                 :date element/element?})"
  {:added "2.2"}
  ([obj]
   (if obj
     (->> (reflect/query-hierarchy obj [#"(^set)[A-Z].+" 2 :instance])
          (reduce (fn [m ele]
                    (assoc m (-> ele :name java->clojure keyword) ele))
                  {}))
     {})))

(defn object-apply
  "applies a map of functions to an object yielding a result of the same shape.
   (object-apply {:year #(.getYear ^Date %)
                  :month #(.getMonth ^Date %)}
                 (Date. 0) identity)
   => {:month 0 :year 70}"
  {:added "2.2"}
  [methods obj f]
  (reduce-kv (fn [m k ele]
               (map/assoc-if m k
                             (try
                               (f (ele obj))
                               (catch Throwable t
                                 (event/raise {:msg "Cannot process object-apply"
                                               :object obj
                                               :element ele
                                               :function f}
                                              (option :nothing [] nil)
                                              (default :nothing))))))
             {} methods))

(defn object-data
  "retrieves the data within the class as a map (like bean)
 
   (keys (object-data (Date. 0)))
   => (contains [:day :date :time :month :seconds :year :hours :minutes]
                :in-any-order :gaps-ok)"
  {:added "2.2"}
  ([obj] (object-data obj identity))
  ([obj f]
    (-> (object-getters obj)
        (object-apply obj f))))
