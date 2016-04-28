(ns hara.object.map-like
  (:require [hara.protocol.object :as object]
            [hara.object.write :as write]
            [hara.object.read :as read]))

(defmacro extend-map-like [^Class cls {:keys [tag read write exclude] :as opts}]
  `[(defmethod object/-meta-read ~cls
      [~'_]
      ~(cond (map? read) read

             (= read :reflect)
             `{:methods (read/read-reflect-fields ~cls)}

             (or (nil? read)
                 (= read :getters))
             `{:methods (read/read-getters ~cls)}))

    ~(when (and write (map? write))
       (assert (or (:from-map write)
                   (:empty write))
               "The :write entry requires a sub-entry for either :from-map or :empty ")
       (let [methods (:methods write)]
         `(defmethod object/-meta-write ~cls
            [~'_]
            ~(cond-> write
               (= methods :reflect)
               (assoc :methods `(write/write-reflect-fields ~cls))

               (or (= methods :setters)
                   (nil? methods))
               (assoc :methods `(write/write-setters ~cls))))))


    (defmethod print-method ~cls
      [v# ^java.io.Writer w#]
      (.write w# (str "#" (or ~tag (.getName ~cls)) ""
                      (dissoc (read/to-data v#) ~@exclude))))])


(comment

  (write/meta-write test.Dog)
  (read/meta-read test.Dog)

  (extend-map-like test.Dog {:tag "dog"
                             :write  {:methods :reflect
                                      :from-map (fn [m] (-> m
                                                            (write/from-map test.DogBuilder)
                                                            (.build)))}
                             :exclude [:species]})

  (extend-map-like test.Cat {:tag "cat"
                             :write  {:from-map (fn [m] (test.Cat. (:name m)))}
                             :exclude [:species]})

  (extend-map-like test.Pet {:tag "pet"
                             :from-map (fn [m] (case (:species m)
                                                 "dog" (from-map m Dog)
                                                 "cat" (from-map m Cat)))})
  
  (write/from-data {:name "hello"} test.Dog)
  (write/from-data {:name "hello"} test.Cat)
  (write/from-data {:name "hello" :species "cat"} test.Pet)
  

  )
