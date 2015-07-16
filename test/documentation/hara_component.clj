(ns documentation.hara-component
  (:use midje.sweet)
  (:require [hara.component :as component]
            [hara.concurrent.ova :as ova]
            [compojure.core :as routes]
            [ring.adapter.jetty :as jetty]
            [clj-http.client :as client]))

[[:chapter {:title "Introduction"}]]

"
[hara.component](https://github.com/zcaudate/hara/blob/master/src/hara/component.clj) is a dependency injection framework inspired by the original Stuart Sierra component [library](https://github.com/stuartsierra/component) and [talk](http://www.youtube.com/watch?v=13cmHf_kt-Q). The virtues of this type of design for composing large systems has been much lauded and is quite a common practise within enterprise applications. Doing a [search](https://www.google.com?q=stuart+sierra+component) will yield many uses of such a pattern."

[[:section {:title "Installation"}]]

"Add to `project.clj` dependencies:

    [im.chit/hara.component \"{{PROJECT.version}}\"]"

"All functions are in the `hara.component` namespace."

(comment (require '[hara.component :as component]))

[[:section {:title "Motivation"}]]

"
The main reason for a reinterpretation of the original [stuartsierra/component](https://github.com/stuartsierra/component) was for a couple reasons:

- the `component/Lifecycle` protocol did not expose `started?` and `stopped?` methods
- the new library has been designed to work well with configuration files
- dependencies are now not required to be explicitly defined
- added support for dealing with arrays of component
- more control was needed when working with nested systems
- more emphasis has been placed on prettiness and readibility

The differentiation of `hara.component` is to tease apart configuration and application topology. Configuration gives the ability to set the starting state of the entire program and should be easy as possible. Many a system become bloated due to not being able to properly manage configuration, therefore composing systems with configuration at the forefront will make for much simpler code and design. This library was build with this paradigm in mind."

[[:chapter {:title "Config Driven Design"}]]

"We will aim to create a system based upon a configuration file. As components are a very high level concept, using the pattern in code is more of a state of mind than following a set of APIs. Therefore in this documentation, it is hoped that a tutorial based approach will demonstrate the core functionality within the library. We seperate the guide into the following sections

- [Probability Model](#probability-model) - How to calculate a bug distribution model.
- [Sampling Model](#sampling-model) - How to sample the distribution model.
- [Implementing Components](#implementing-components) - How to create and pretty up components so they are easy to integrate.
- [The Big Picture](#the-big-picture) - How to think about building and extending systems

The first two chapters are not really about components, but it is important in showing how to create functions based around a particular configuration. The rest of the sections take a comphrensive approach of how to configure and reason about entire systems based on the component approach."

[[:section {:title "The Bug Trapper"}]]

"We are creating a simulation based on trapping bugs in different parts of the house, then tallying up the results and displaying it through a web interface. We can see how the sub-systems connect together in the diagram below:"

[[:image {:src "img/hara_component/dependency.png" :height "400px" :title "sub-system dependencies"}]]

"At the very bottom is a statistical model for generating events ie, how often an insect would be likely to appear given a certain set of conditions such as brightness, dampness, if it is indoors or outdoors, etc. This is used by a number of `traps`, which are an array of devices that simulates events of an insect going into the trap. An insect may or may not be captured by the trap itself and this is also tallied. The `app` itself tracks events over time by putting results into a *'database'* and the server takes live results and outputs a string via http."

[[:section {:title "Configuration"}]]

"A datastructure can be created that customises various aspects of the simulation:"

(def config
  {:server     {:port 8090}
   :app        {}
   :traps     ^{:fuzziness 0.1 :efficiency 0.6}
               [{:location "kitchen"  :brightness 0.3
                 :indoor true :rate 0.5}
                {:location "bedroom"  :brightness 0.1
                 :dampness 0.2 :indoor true :rate 0.3  :efficiency 0.2}
                {:location "patio"    :brightness 0.5
                 :outdoor true :rate 1.5  :efficiency 0.1}
                {:location "bathroom" :dampness 0.3
                 :indoor true :rate 0.2  :efficiency 0.3}]
   :db         {}
   :model      {:default {:fly 0.5 :ladybug 0.05
                          :mosquito 0.35 :bee 0.1}
                :linear  {:brightness {:bee 0.5}
                          :dampness   {:mosquito 0.4}}
                :toggle  {:indoor     {:fly 0.3
                                       :mosquito 0.2}
                          :outdoor    {:bee 0.1
                                       :ladybug 0.1}}}})

[[:chapter {:title "Probability Model"}]]

"We have a model of what percentage of bugs and depending on location, brightness and dampness, we adjust our model accordingly. So for example, we should be able to write a function `adjusted-distribution` that takes in a model and some parameter settings and spits out a probability distribution in the form of a map:"

(comment
  (adjusted-distribution {} (-> config :model))
  => {:fly 0.5, :ladybug 0.05, :mosquito 0.35, :bee 0.1}

  (adjusted-distribution {:indoor true}
                         (-> config :model))
  => {:fly 0.8, :ladybug 0.05, :mosquito 0.55, :bee 0.1})


[[:section {:title "linear-adjustment"}]]

"We write a functions to adjustment for the linear increase:"

(defn linear-adjustment [params linear]
  (reduce-kv (fn [m k stats]
               (if-let [mul (get params k)]
                 (reduce-kv (fn [m k v]
                              (update-in m [k] (fnil #(+ % (* mul v))
                                                      0)))
                            m
                            stats)
                 m))
             {}
             linear))

"It can be applied to the model:"

(facts
  (linear-adjustment {:brightness 0.1}
                     (-> config :model :linear))
  => {:bee 0.05}

  (linear-adjustment {:brightness 0.2}
                     (-> config :model :linear))
  => {:bee 0.1}

  (linear-adjustment {:brightness 0.3}
                     (-> config :model :linear))
  => {:bee 0.15}

  (linear-adjustment {:dampness 0.5}
                     (-> config :model :linear))
  => {:mosquito 0.2})

[[:section {:title "toggle-adjustment"}]]

"The second function is for toggle adjustment, meaning that depending on a particular flag, we add a certain amount to the overall distribution:"

(defn toggle-adjustment [params toggle]
  (reduce-kv (fn [m k stats]
               (if-let [mul (get params k)]
                 (reduce-kv (fn [m k v]
                              (update-in m [k] (fnil #(+ % v)
                                                     0)))
                            m
                            stats)
                 m))
             {}
             toggle))

"It can be applied to the model:"

(facts
  (toggle-adjustment {:indoor true}
                     (-> config :model :toggle))
  => {:fly 0.3, :mosquito 0.2}

  (toggle-adjustment {:outdoor true}
                     (-> config :model :toggle))
  => {:bee 0.1, :ladybug 0.1})

[[:section {:title "add-distributions"}]]

"A helper function is defined to add distributions together"

(defn add-distributions
  ([] {})
  ([m] m)
  ([m1 m2]
   (reduce-kv (fn [m k v]
                (update-in m [k] (fnil #(+ % v) 0)))
              m1
              m2))
  ([m1 m2 & more]
   (apply add-distributions (add-distributions m1 m2) more)))

"The function is relatively generic and can be used to add arbitrary maps together:"

(fact
  (add-distributions {:a 0.1} {:a 0.1 :b 0.3} {:a 0.3 :c 0.3})
  => {:a 0.5, :b 0.3, :c 0.3})

[[:section {:title "adjusted-distribution"}]]

"Combining the three functions, we can get an adjusted distribution based on the model taken from the config:"

(defn adjusted-distribution [params {:keys [default linear toggle] :as model}]
  (let [ladjust (linear-adjustment params linear)
        tadjust (toggle-adjustment params toggle)]
    (add-distributions default ladjust tadjust)))

"The adjusted distributions for each trap can then be calculated:"

(fact
  (mapv #(adjusted-distribution % (-> config :model))
        (-> config :traps))
  => [;; kitchen
      {:fly 0.8, :ladybug 0.05,
       :mosquito 0.55, :bee 0.25}
      ;; bedroom
      {:fly 0.8, :ladybug 0.05,
       :mosquito 0.63, :bee 0.15000000000000002}
      ;; patio
      {:fly 0.5, :ladybug 0.15000000000000002,
       :mosquito 0.35, :bee 0.44999999999999996}
      ;; bathroom
      {:fly 0.8, :ladybug 0.05,
       :mosquito 0.6699999999999999, :bee 0.1}])

[[:chapter {:title "Sampling Model"}]]

"The sampling model is easier to construct. We wish to create a function that takes in a distribution and returns a key that is proportional to the values of the map:"

(comment
  (random-sample {:a 0.5 :b 0.5})
  => ;; either returns :a or :b
  #(get #{:a :b} %))

[[:section {:title "cumultive"}]]

"culmultive takes a distribution and turns it into a range, sorted by value:"

(defn cumultive [distribution]
  (dissoc (reduce (fn [out [k v]]
                    (let [total (::total out)
                          ntotal (+ total v)]
                      (assoc out
                             k [total ntotal]
                             ::total ntotal)))
                  {::total 0}
                  (sort-by val distribution))
          ::total))

"examples of its usage can be seen:"

(fact
  (cumultive {:a 0.3 :b 0.5 :c 0.2})
  => {:c [0 0.2], :a [0.2 0.5], :b [0.5 1.0]})

[[:section {:title "category"}]]

"category takes a cumultive distribution and a point, return which section it belongs to:"

(defn category [cumulative stat]
  (->> cumulative
       (keep (fn [[k [lower upper]]]
               (if (<= lower stat upper) k)))
       first))

"examples of its usage can be seen:"

(fact
  (def dist (cumultive {:a 0.3 :b 0.5 :c 0.2}))
  ;; {:c [0 0.2], :a [0.2 0.5], :b [0.5 1.0]} 0.1


  (category dist 0.1) => :c

  (category dist 0.3) => :a

  (category dist 0.8) => :b)

[[:section {:title "random-sample"}]]

"Now the `random-sample` function can be written:"

(defn random-sample [distribution]
  (let [total (apply + (vals distribution))
        stat  (rand total)]
    (category (cumultive distribution) stat)))

"We can now use this with a probability map:"

(fact
  (random-sample {:a 0.3 :b 0.5 :c 0.2})
  => ;; Returns either :a :b or :c
  #(get #{:a :b :c} %))

"As well as with `adjusted-model` defined in the [previous chapter](#probability-models)"

(fact
  (random-sample
   (adjusted-distribution {:brightness 0.3 :indoor true :rate 0.5}
                          (-> config :model)))
  => ;; Return either :fly :ladybug :mosquito :bee
  #(get #{:fly :ladybug :mosquito :bee} %))

[[:chapter {:title "Implementing Components"}]]

[[:section {:title "Model"}]]

"We create a record for `Model`. The data is just a nested map but a record is used purely for printing purposes. There is quite alot of stuff in the map and we should be able to only show the necessary amount of information - in this case, we only want to know the keys of the datastructure:"

(defrecord Model []
  Object
  (toString [obj]
    (str "#model" (vec (keys (into {} obj))))))

(defmethod print-method Model
  [v w]
  (.write w (str v)))

"We can now use the `map->Model` function to create a nicer new on the model:"

(comment
  (map->Model (:model config))
  ;;=> #model[:default :linear :toggle]
)

[[:section {:title "Trap"}]]

"`Trap` is a component that needs to be started and stopped. It simulates a trap that knows what insect went inside the trap, what time it entered and if it had been captured. We create a basic function for one round of the trapping an insect:"

(defn trap-bug [{:keys [rate efficiency fuzziness model output] :as trap}]
  (let [pause   (long (* (+ rate
                            (* (- (rand 1) 0.5) fuzziness))
                         1000))]
    (Thread/sleep pause)
    (reset! output
           {:time (java.util.Date.)
            :bug (random-sample
                  (adjusted-distribution trap model))
            :captured (< (rand 1) efficiency)})
    trap))

"The usage for such a function can be seen below:"

(fact
  (-> (trap-bug {:rate 0.8
                 :efficiency 0.5
                 :fuzziness 0.1
                 :model (:model config)
                 :output (atom nil)})
      :output
      deref)
  => (contains {:time #(instance? java.util.Date %)
                :bug #(#{:fly :bee :ladybug :mosquito} %)
                :captured #(instance? Boolean %)}))

"We create a record that implements the `IComponent` interface, making sure that we hide keys that are not useful"

(defrecord Trap []
  Object
  (toString [obj]
    (let [selected [:location :output]]
      (str "#trap" (-> (into {} obj)
                       (select-keys selected)
                       (update-in [:output] deref)))))

  component/IComponent
  (-start [trap]
    (assoc trap
           :thread (future
                     (println (str "Starting trap in "
                                   (:location trap) "\n"))
                     (last (iterate trap-bug trap)))))

  (-stop [{:keys [thread output] :as trap}]
    (do
      (println (str "Stopping trap in " (:location trap)))
      (future-cancel thread)
      (reset! output nil)
      (dissoc trap :thread))))

(defmethod print-method Trap
  [v w]
  (.write w (str v)))

"Finally, we create a `trap` constructor taking a config map and outputting a `Trap` record:"

(defn trap [m]
  (assoc (map->Trap m)
         :output (atom nil)))

[[:section {:title "Partial System Testing"}]]

"Having implemented the records for `:traps` and `:model`, we can test to see if our array of traps are working. The call to system takes two parameters - a topology map and a configuration map. The topology map specifies functions and dependencies whilst the configuration map specifies the initial input data. Note that to specify contruction of an array of components put the constructor in an additional vector:"

(comment
  (def topology {:traps [[trap] :model]
                 :model [map->Model]})

  (def sys (-> (component/system toplogy config)
               (component/start)))
  ;; Starting trap in patio
  ;; Starting trap in bathroom
  ;; Starting trap in kitchen
  ;; Starting trap in bedroom


  (add-watch (-> sys :traps first :output)
             :print-change
             (fn [_ _ _ n]
               (if (:captured n)
                 (println n))))
  ;; {:time #inst "2015-07-15T08:21:33.690-00:00", :bug :fly, :captured true}
  ;; {:time #inst "2015-07-15T08:21:34.216-00:00", :bug :mosquito, :captured true}
  ;; ....
  ;; ....
  ;; {:time #inst "2015-07-15T08:21:36.753-00:00", :bug :fly, :captured false}

  (remove-watch (-> sys :traps first :output) :print-change)
  ;; <CONSOLE OUTPUT STOPS>

  (component/stop sys)
  ;;=> {:traps #arr[#trap{:location "kitchen", :output nil}
  ;;             #trap{:location "bedroom", :output nil}
  ;;             #trap{:location "patio", :output nil}
  ;;             #trap{:location "bathroom", :output nil],
  ;;    :model #model[:default :linear :toggle]}
)

[[:section {:title "App"}]]

"The role of the app is to hook up the sensors to a datastore, in this case an [ova](hara-concurrent-ova.html), a mutable array of elements. We define `initialise-app` to setup watches to provide some summary and coordination:"

(require '[hara.concurrent.ova :as ova])

(defn initialise-app [{:keys [db traps display total] :as app}]
  (let [data (mapv (fn [trap]
                     (select-keys trap [:location]))
                   traps)]
    (dosync (ova/init! db data))
    (doseq [{:keys [location output] :as trap} traps]
      (add-watch
       output :summary
       (fn [_ _ _ {:keys [success bug]}]
         (dosync (ova/!> db [:location location]
                         (update-in [:triggered]
                                    (fnil inc 0))
                         (update-in [:captured]
                                    (fnil #(update-in % [bug] (fnil inc 0))
                                          {}))))
         (swap! total update-in [bug] (fnil inc 0))))))
  app)

"The opposite method `deinitialise-app` is also defined:"

(defn deinitialise-app [{:keys [db traps total] :as app}]
  (dosync (ova/empty! db))
  (reset! total {})
  (doseq [{:keys [output]} traps]
    (remove-watch output :summary))
  app)

"The two functions are then hooked up via `-start` and `-stop` protocol methods for the component architecture:"

(defrecord App []
  Object
  (toString [app]
    (str "#app" (-> app keys vec)))

  component/IComponent
  (-start [app]
    (initialise-app app)
    app)

  (-stop [app]
    (deinitialise-app app)
    app))

(defmethod print-method App
  [v w]
  (.write w (str v)))

(defn app [m]
  (assoc (map->App m) :total (atom {})))

[[:section {:title "App Testing"}]]

"We can now do a more testing by including a couple more constructors. Note that the keys `:db`, `app` and `summary` have been added. Also see the syntax for the `:summary` topology to expose the `:total` submap from `:app`.

The syntax for the `:summary` key should be further explained. What `component/start` sees a initialisation of {:expose [:total]}, it take the first dependency (in this case, `:app`), gets the `:total` submap and exposes it as `:summary` in the system map. The value of `:expose` can be either a vector (for nested map supprt) or a function for more generic operations. This promotes reuse and composition of multiple systems."

(comment
  (def topology {:traps   [[trap] :model]
                 :model   [map->Model]
                 :db      [ova/ova]
                 :app     [app :traps :db]
                 :summary [{:expose [:total]} :app]})

  (def sys (-> (component/system topology config)
               (component/start)))
  ;; Starting trap in patio
  ;; Starting trap in bathroom
  ;; Starting trap in kitchen
  ;; Starting trap in bedroom


  @(:summary sys) ;; first call to :summary gives a set of bugs trapped
  ;;=> {:mosquito 101, :fly 120, :ladybug 6, :bee 28}

  @(:summary sys) ;; second call to :summary gives an updated of bugs trapped
  ;;=> {:mosquito 148, :fly 184, :ladybug 12, :bee 37}

  (component/stop sys)
  ;; Stopping trap in kitchen
  ;; Stopping trap in bedroom
  ;; Stopping trap in patio
  ;; Stopping trap in bathroom

  ;;=> {:app #app[:display :total],
  ;;    :db #ova [],
  ;;    :traps #arr[#trap{:location "kitchen",  :output nil}
  ;;                #trap{:location "bedroom",  :output nil}
  ;;                #trap{:location "patio",    :output nil}
  ;;                #trap{:location "bathroom", :output nil}]
  ;;    :model #model[:default :linear :toggle]}
)

[[:section {:title "Server"}]]

"The server requires a couple of external dependencies:"

(comment
  (require '[compojure.core :as routes]
           '[ring.adapter.jetty :as jetty]
           '[clj-http.client :as client]))

"We define a very simple server with one route that just returns the summary as a string:"

(defn make-routes
  [{:keys [summary] :as serv}]
  (routes/GET "*" [] (str @summary)))

(defrecord Server []
  Object
  (toString [serv]
    (str "#server" (-> serv keys vec)))

  component/IComponent
  (-start [{:keys [port summary] :as serv}]
    (println (str "STARTING SERVER on port " port))
    (assoc serv
           :instance (jetty/run-jetty (make-routes serv)
                                      {:join? false
                                       :port port})))

  (-stop [{:keys [summary instance] :as serv}]
    (println (str "STOPPING SERVER on port " (:port serv)))
    (.stop instance)
    (dissoc serv :instance)))

"Again, `print-method` is defined for prettiness:"

(defmethod print-method Server
  [v w]
  (.write w (str v)))

[[:section {:title "Server Testing"}]]

"Again, we add an additional constructor to the system and start:"

(comment
  (def sys (-> {:traps   [[trap] :model]
                :model   [map->Model]
                :db      [ova/ova]
                :app     [app :traps :db]
                :summary [{:expose [:total]} :app]
                :server  [map->Server :summary]}
               (component/system config)
               (component/start)))
  ;; Starting trap in patio
  ;; Starting trap in bathroom
  ;; Starting trap in kitchen
  ;; Starting trap in bedroom
  ;; STARTING SERVER on PORT 8090
  )

"We can now use a client to access the summary via a http protocol:"

(comment
  ;; First Time
  (-> (client/get "http://localhost:8090/")
      :body)
  ;;=> "{:fly 249, :bee 55, :mosquito 187, :ladybug 19}"


  ;; Second Time
  (-> (client/get "http://localhost:8090/")
      :body)
  ;; => "{:fly 305, :bee 70, :mosquito 227, :ladybug 26}"
  )

"Stopping is no different to before"

(comment
  (component/stop sys)
  ;; STOPPING SERVER on PORT 8090
  ;; Stopping trap in kitchen
  ;; Stopping trap in bedroom
  ;; Stopping trap in patio
  ;; Stopping trap in bathroom

  ;;=> {:app #app[:display :total],
  ;;    :db #ova [],
  ;;    :traps #arr[#trap{:location "kitchen",  :output nil}
  ;;                #trap{:location "bedroom",  :output nil}
  ;;                #trap{:location "patio",    :output nil}
  ;;                #trap{:location "bathroom", :output nil}]
  ;;    :model #model[:default :linear :toggle]
  ;;    :server #server[:port]}
)

[[:chapter {:title "The Big Picture"}]]

[[:section {:title "Summary"}]]

"We have created the bug trapping system based on our [dependency diagram](#sub-system-dependencies). We can visualize the essential components that make up our system in the diagram below:"

[[:image {:src "img/hara_component/system.png" :width "600px" :title "the system"}]]

"The constructors and the dependencies form our system topology whilst the data that initialised our system form our config. There are significant advantages of doing this:

- The final code is almost the same as the diagram of the system.
- There is an isometric correspondence between process and data.
- It clearly seperates data (which is normally loaded from a file) from process.
- It keeps all the initialisations in a single place.
- Systems can be built incrementally in the way that we have just done."

[[:section {:title "Further Extension"}]]

"Say we needed to add more functionality to our system, in which we define the `make-routes` method to add an endpoint giving us information about the status of the datastore:"

(defn make-routes
  [{:keys [db summary] :as serv}]
  (routes/routes
    (routes/GET "/total" [] (str @summary))
    (routes/GET "/db" []    (str (persist db)))))

"It is very easy to redefine topology to include the extra dependency:"

(comment
  (def toplogy {:traps   [[trap] :model]
                :model   [map->Model]
                :db      [ova/ova]
                :app     [app :traps :db]
                :summary [{:expose [:total]} :app]
                :server  [map->Server :summary :db]}) ;; note the extra `:db` key

  (def sys (-> (component/system topology config)
               (component/start))))

"We can again visualize the extended system:"

[[:image {:src "img/hara_component/system2.png" :width "600px" :title "the extended system"}]]

[[:chapter {:title "Links and Resources"}]]

"
Here are some more links and resources on the web:

- [stuartsierra/component](https://github.com/stuartsierra/component) - original library
- [just a bit more structure](http://z.caudate.me/hara-component-just-a-bit-more-structure/) - the announcement on my blog
"
