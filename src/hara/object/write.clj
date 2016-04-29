(ns hara.object.write
  (:require [clojure.walk :as walk]
            [hara.protocol.object :as object]
            [hara.protocol.map :as map]
            [hara.data.map :as data]
            [hara.string.case :as case]
            [hara.reflect :as reflect]
            [hara.reflect.util :as reflect-util]))

(defn meta-write
  "accesses the write-attributes of an object
 
   (write/meta-write DogBuilder)
   => (contains {:class test.DogBuilder
                 :empty fn?,
                 :methods (contains
                           {:name
                            (contains {:type java.lang.String, :fn fn?})})})"
  {:added "2.3"}
  [^Class cls]
  (assoc (object/-meta-write cls) :class cls))

(declare from-data)

(defn write-reflect-fields
  "write fields of an object from reflection
   (-> (write/write-reflect-fields Dog)
       keys)
   => [:name :species]"
  {:added "2.3"}
  [cls]
  (->> (reflect/query-class cls [:field])
       (reduce (fn [out ele]
                 (let [k (-> ele :name case/spear-case keyword)
                       cls (.getType (get-in ele [:all :delegate]))]
                   (assoc out k {:type cls :fn ele})))
               {})))

(defn write-setters
  "write fields of an object through setter methods
   (write/write-setters Dog)
   => {}
 
   (keys (write/write-setters DogBuilder))
   => [:name]"
  {:added "2.3"}
  ([cls] (write-setters cls {:prefix "set"
                             :template '(fn <method> [obj val]
                                          (. obj (<method> val))
                                          obj)}))
  ([cls {:keys [prefix template]}]
   (->> [:method :instance (re-pattern (str "^" prefix ".+")) 2]
        (reflect/query-class cls)
        (reduce (fn [out ele]
                  (assoc out
                         (-> (:name ele) (subs (count prefix)) case/spear-case keyword)
                         {:type (-> ele :params second)
                          :fn (eval (walk/postwalk-replace {'<method> (symbol (:name ele))}
                                                                template))}))
                {}))))

(defn from-empty
  "creates the object from an empty object constructor
   (write/from-empty {:name \"chris\" :pet \"dog\"}
                     (fn [_] (java.util.Hashtable.))
                     {:name {:type String
                             :fn (fn [obj v]
                                   (.put obj \"hello\" (keyword v))
                                   obj)}
                      :pet  {:type String
                            :fn (fn [obj v]
                                   (.put obj \"pet\" (keyword v))
                                   obj)}})
   => {\"pet\" :dog, \"hello\" :chris}"
  {:added "2.3"}
  [m empty methods]
  (let [obj (empty m)]
    (reduce-kv (fn [obj k v]
                 (if-let [{:keys [type] func :fn} (get methods k)]
                   (func obj (from-data v type))
                   obj))
               obj
               m)))

(defn from-map
  "creates the object from a map
   (-> {:name \"chris\" :age 30 :pets [{:name \"slurp\" :species \"dog\"}
                                     {:name \"happy\" :species \"cat\"}]}
       (write/from-map Person)
       (read/to-data))
   => {:name \"chris\", :age 30, :pets [{:name \"slurp\", :species \"dog\"}
                                      {:name \"happy\", :species \"cat\"}]}"
  {:added "2.3"}
  [m ^Class cls]
  (let [m (if-let [rels (get object/*transform* type)]
            (data/transform-in m rels)
            m)
        {:keys [empty methods from-map] :as mobj} (meta-write cls)]
    (cond from-map
          (from-map m)

          (and empty methods)
          (from-empty m empty methods)

          :else
          (map/-from-map m cls))))

(defn from-data
  "creates the object from data
   (-> (write/from-data [\"hello\"] (Class/forName \"[Ljava.lang.String;\"))
       seq)
   => [\"hello\"]"
  {:added "2.3"}
  [arg ^Class cls]
  (let [^Class targ (type arg)]
    (cond
      ;; If there is a direct match
      (reflect-util/param-arg-match cls targ)
      arg

      ;; If there is a vector
      (and (vector? arg)
           (.isArray cls))
      (let [cls (.getComponentType cls)]
        (->> arg
             (map #(from-data % cls))
             (into-array cls)))

      :else
      (let [{:keys [from-string] :as mobj} (meta-write cls)]
        (cond
          ;; If input is a string and there is a from-string method
          (and (string? arg) from-string)
          (from-string arg cls)

          ;; If the input is a map
          (map? arg)
          (from-map arg cls)

          :else
          (throw (Exception. (format "Problem converting %s to %s" arg targ))))))))
