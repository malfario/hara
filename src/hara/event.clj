(ns hara.event
  (:require [hara.event.common :as common]
            [hara.event.condition.data :as data]
            [hara.event.condition.raise :as raise]
            [hara.event.condition.manage :as manage]
            [hara.event.condition.util :as util]))

(defonce ^:dynamic *signal-manager* (atom (common/manager)))

(defonce ^:dynamic *issue-managers* [])

(defonce ^:dynamic *issue-optmap* {})

(defn clear-listeners []
  (reset! *signal-manager* (common/manager)))

(defn list-listeners
  ([]
   (common/list-handlers @*signal-manager*))
  ([checker]
   (common/list-handlers @*signal-manager* checker)))

(defn install-listener [id checker handler]
  (swap! *signal-manager*
         common/add-handler checker {:id id
                                     :fn handler}))

(defn uninstall-listener [id]
  (swap! *signal-manager* common/remove-handler id))

(defmacro deflistener [name checker bindings & more]
  (let [sym    (str  (.getName *ns*) "/" name)
        hform  (common/handler-form bindings more)]
    `(install-listener (symbol ~sym) ~checker ~hform)))

(defmacro signal [data]
  `(let [ndata#   (common/expand-data ~data)]
     (doall (for [handler# (common/match-handlers @*signal-manager* ndata#)]
              ((:fn handler#) ndata#)))))

(defmacro continue [& body]
  `{:type :continue :value (do ~@body)})

(defmacro default [& args]
  `{:type :default :args (list ~@args)})

(defmacro choose [label & args]
  `{:type :choose :label ~label :args (list ~@args)})

(defmacro fail
  ([] {:type :fail})
  ([data]
     `{:type :fail :data ~data}))

(defmacro escalate [data & forms]
  (let [[data forms]
        (if (util/is-special-form :raise data)
          [nil (cons data forms)]
          [data forms])]
    `{:type :escalate
      :data ~data
      :options  ~(util/parse-option-forms forms)
      :default  ~(util/parse-default-form forms)}))

(defmacro raise
  "Raise an issue with the content to be either a keyword, hashmap or vector, optional message
  and raise-forms - 'option' and 'default'"
  [content & [msg & forms]]
  (let [[msg forms] (if (util/is-special-form :raise msg)
                      ["" (cons msg forms)]
                      [msg forms])
        options (util/parse-option-forms forms)
        default (util/parse-default-form forms)]
    `(let [issue# (data/issue ~content ~msg ~options ~default)]
       (signal (assoc (:data issue#) :issue (:msg issue#)))
       (raise/raise-loop issue# *issue-managers*
                         (merge (:optmap issue#) *issue-optmap*)))))


(defmacro manage
  "This creats the 'manage' dynamic scope form. The body will be executed
  in a dynamic context that allows handling of issues with 'on' and 'option' forms."
  [& forms]
  (let [sp-fn           (fn [form] (util/is-special-form :manage form #{'finally 'catch}))
        body-forms      (vec (filter (complement sp-fn) forms))
        sp-forms        (filter sp-fn forms)
        id              (common/new-id)
        options         (util/parse-option-forms sp-forms)
        on-handlers     (util/parse-on-handler-forms sp-forms)
        on-any-handlers (util/parse-on-any-handler-forms sp-forms)
        try-forms       (util/parse-try-forms sp-forms)
        optmap          (zipmap (keys options) (repeat id))]
    `(let [manager# (common/manager ~id
                                    ~(vec (concat on-handlers on-any-handlers))
                                    ~options)]
       (binding [*issue-managers* (cons manager# *issue-managers*)
                 *issue-optmap*   (merge ~optmap *issue-optmap*)]
         (try
           (try
             ~@body-forms
             (catch clojure.lang.ExceptionInfo ~'ex
               (manage/manage-condition manager# ~'ex)))
           ~@try-forms)))))


(comment
  (deflistener hello-print :hello
    ev
    (println ev))

  (manage
   [(raise {:hello "there"} "hoeuoeu")]
   (on :hello e
       (println e)
       (continue 3)
       (escalate {:a 1})))

  
  


  
  )
