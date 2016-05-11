(ns hara.object.map-like
  (:require [hara.protocol.object :as object]
            [hara.object.write :as write]
            [hara.object.read :as read]
            [hara.object.print :as print]))

(defn key-selection
  [m include exclude]
  (cond-> m
    include (select-keys include)
    exclude (#(apply dissoc % exclude))))

(defmacro extend-map-like
  "creates an entry for map-like classes
 
   (extend-map-like test.DogBuilder
                    {:tag \"build.dog\"
                     :write {:empty (fn [_] (test.DogBuilder.))}
                     :read :reflect})
 
   (extend-map-like test.Dog {:tag \"dog\"
                              :write  {:methods :reflect
                                       :from-map (fn [m] (-> m
                                                             (write/from-map test.DogBuilder)
                                                             (.build)))}
                              :exclude [:species]})
 
   (with-out-str
     (prn (write/from-data {:name \"hello\"} test.Dog)))
   => \"#dog{:name \"hello\"}\"
 
   (extend-map-like test.Cat {:tag \"cat\"
                              :write  {:from-map (fn [m] (test.Cat. (:name m)))}
                              :exclude [:species]})
 
   (extend-map-like test.Pet {:tag \"pet\"
                              :from-map (fn [m] (case (:species m)
                                                  \"dog\" (write/from-map m test.Dog)
                                                  \"cat\" (write/from-map m test.Cat)))})
 
   (with-out-str
    (prn (write/from-data {:name \"hello\" :species \"cat\"} test.Pet)))
   => \"#cat{:name \"hello\"}\""
  {:added "2.3"} 
  [^Class cls {:keys [read write exclude include] :as opts}]
  `[(defmethod object/-meta-read ~cls
      [~'_]
      ~(let [read (cond (map? read) read

                        (= read :reflect)
                        `{:methods (key-selection (read/read-reflect-fields ~cls) ~include ~exclude)}

                        (or (nil? read)
                            (= read :getters))
                        `{:methods (-> (merge (read/read-getters ~cls read/+read-get-template+)
                                              (read/read-getters ~cls read/+read-is-template+))
                                       (key-selection ~include ~exclude))})]
         (print/assoc-print-vars read opts)))

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

    (print/extend-print ~cls)])
