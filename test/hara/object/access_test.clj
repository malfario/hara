(ns hara.object.access-test
  (:use midje.sweet)
  (:require [hara.object.access :refer :all]
            [hara.object.builder :as builder]
            [hara.protocol.map :as map]
            [hara.protocol.data :as data]
            [hara.common.string :refer [to-string]]
            [hara.string.case :as case]
            [hara.reflect :as reflect])
  (:import [org.eclipse.aether.util.repository
            AuthenticationBuilder
            ChainedAuthentication
            StringAuthentication]
           org.eclipse.aether.repository.Authentication))

(defmethod data/-meta-object Authentication
  [_]
  {:class Authentication
   :types #{java.util.Map}
   :from-data map/-from-map
   :to-data map/-to-map})

(defmethod builder/build-template Authentication
  [_]
  {:init  (fn [m] (AuthenticationBuilder.))
   :final (fn [obj] (.build obj))
   :exclude []
   :prefix "add"
   :setters {:private-key (fn [obj private-key]
                            (let [{:keys [path passphrase]} private-key]
                              (.addPrivateKey obj path passphrase)))

             :secrets (fn [obj secrets]
                        (reduce-kv (fn [obj k v]
                                     (doto obj (.addSecret k v)))
                                   obj
                                   secrets))
             :ntlms (fn [obj ntlms]
                      (reduce-kv (fn [obj k v]
                                   (doto obj (.addNtlm k v)))
                                 obj
                                 ntlms))}})

(defmethod map/-from-map Authentication
  [m cls]
  (-> (builder/build-template cls)
      (builder/build m)))

(extend-protocol map/IMap
  Authentication
  (-to-map [obj]
    (let [t (type obj)
          map-fn (fn [out {:keys [value key] secret :secretHash}]
                   (if (#{"username"} key)
                     (assoc out (keyword key) value)
                     out))]
      (cond (= ChainedAuthentication t)
            (->> (reflect/delegate obj)
                 (into {})
                 :authentications
                 seq
                 (map #(into {} (reflect/delegate %)))
                 (reduce map-fn {}))
            (= StringAuthentication t)
            (->> (reflect/delegate obj)
                 (into {})
                 (map-fn {})))
      )))

^{:refer hara.object.access/access-get :added "2.2"}
(fact "access data within a class through keyword getters")

^{:refer hara.object.access/coerce :added "2.2"}
(fact "coerces data into the right class"
  (-> (coerce  Authentication {:username "chris"})
      map/-to-map)
  => {:username "chris"})

^{:refer hara.object.access/access-set-coerce :added "2.2"}
(fact "function that allows access-set to use coercion")

^{:refer hara.object.access/access-set :added "2.2"}
(fact "access data within a class through keyword setters")

^{:refer hara.object.access/access-get-nested :added "2.2"}
(fact "access data within nested classes")

^{:refer hara.object.access/access-set-nested :added "2.2"}
(fact "set data within nested classes")

^{:refer hara.object.access/access-set-map :added "2.2"}
(fact "sets class data using a map")

^{:refer hara.object.access/access :added "2.2"}
(fact "generic interface for getters and setters")
