(ns hara.object.util
  (:require [hara.string.case :as case]
            [hara.data.map :as map]
            [hara.reflect :as reflect]
            [hara.event :as event]))

(defn java->clojure [^String name]
  (let [nname (cond (re-find #"(^get)|(^set)[A-Z].+" name)
                    (subs name 3)

                    (re-find #"^is[A-Z].+" name)
                    (str (subs name 2) "?")

                    :else name)]
    (case/spear-case nname)))

(defn clojure->java
  ([name] (clojure->java name :get))
  ([^String name suffix]
   (let [nname (cond (.endsWith name "?")
                     (str "is-" (.substring name 0 (.length name)))

                     :else
                     (str (clojure.core/name suffix) "-" name))]
     (case/camel-case nname))))

(defn object-getters
  ([obj]
   (if obj
     (->> (reflect/query-instance obj [#"(^get)|(^is)[A-Z].+" 1 :instance])
          (reduce (fn [m ele]
                    (assoc m (-> ele :name java->clojure keyword) ele))
                  {}))
     {})))

(defn object-setters
  ([obj]
   (if obj
     (->> (reflect/query-instance obj [#"(^set)[A-Z].+" 2 :instance])
          (reduce (fn [m ele]
                    (assoc m (-> ele :name java->clojure keyword) ele))
                  {}))
     {})))

(defn object-apply [methods obj f]
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
  ([obj] (object-data obj identity))
  ([obj f]
    (-> (object-getters obj)
        (object-apply obj f))))
