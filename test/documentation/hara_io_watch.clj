(ns documentation.hara-io-watch
  (:use midje.sweet)
  (:require [hara.io.watch]
            [hara.common.watch :as watch]
            [clojure.java.io :as io]))

[[:chapter {:title "Introduction"}]]

[[:section {:title "Installation"}]]

"Add to `project.clj` dependencies:

    [im.chit/hara.io.watch \"{{PROJECT.version}}\"]"

"All functions are in the `hara.common.watch` namespace, with `hara.io.watch` providing filewatch extensions:"

(comment (require '[hara.io.watch]
                  '[hara.common.watch :as watch]))

[[:section {:title "Motivation"}]]

"
`hara.io.watch` wraps the `java.nio.file.WatchService` api for an add-watch compatible interface for. There are many file watches implementations around:

- [clojure-watch](https://github.com/derekchiang/Clojure-Watch)
- [dirwatch](https://github.com/juxt/dirwatch)
- [hawk](https://github.com/wkf/hawk)
- [watchtower](https://github.com/ibdknox/watchtower) 
- [java-watcher](https://github.com/klauern/java-watcher.clj)
- [panoptic](https://github.com/xsc/panoptic)
- [ojo](https://github.com/rplevy/ojo)
- [filevents](https://github.com/Raynes/filevents)

As well as seperate filewatch implementations as part of larger application libraries. 

- [lein-midje](https://github.com/marick/lein-midje)
- [ns-tracker](https://github.com/weavejester/ns-tracker) 
- [lazytest](https://github.com/stuartsierra/lazytest)

The novelty of `hara.io.watch` lies in the concept that it extends `hara.common.watch` (which is an abstraction around clojure`s `add-watch` semantics).
"

[[:chapter {:title "Walkthrough"}]]

[[:section {:title "Watching Atoms"}]]

"
There's a pattern for watching things that already exists in clojure:
"

(comment
  (add-watch object :key (fn [object key previous next])))

"
However, `add-watch` is a generic concept that exists beyond atoms. It can be applied to all sorts of objects. Furthermore, watching something usually comes with a condition. We usually don't react on every change that comes to us in our lives. We only react when a certain condition comes about. For example, we can see the condition that is placed on this statement: 

>  Watch the noodles on the stove and IF it starts
>  boiling over, add some cold water to the pot

The `hara.common.watch` package provides for additional options to be specified when watching the object in question. Is the following example, `:select :b` is used to focus on `:b` and `:diff true` is a setting that configures the watcher so that it will only take action when `:b` has been changed:
"

(fact
  (let [subject  (atom {:a 1 :b 2})
        observer (atom nil)]
    (watch/add subject :clone
               (fn [_ _ p n] (reset! observer n))
               
               ;; Options
               {:select :b   ;; we will only look at :b
                :diff true   ;; we will only trigger if :b changes
                })

    (swap! subject assoc :a 0) ;; change in :a does not
    @observer => nil           ;; affect watch

    (swap! subject assoc :b 1) ;; change in :b does
    @observer => 1))

[[:section {:title "Watching Files"}]]

"The same concept of `watch` is used for filesystems. So instead of an atom, a directory is specified using very similar semantics:"

(fact
  
  (def ^:dynamic *happy* (promise))
  
  ;; We add a watch  
  (watch/add (io/file ".") :save
             (fn [f k _ [cmd ^java.io.File file]]
               
               ;; One-shot strategy where we remove the 
               ;; watch after a single event
               (watch/remove f k)
               (.delete file)
               (deliver *happy* [cmd (.getName file)]))
             
             ;; Options
             {:types #{:create :modify} 
              :recursive false
              :filter  [".hara"]
              :exclude [".git" "target"]
              :mode :async})
  
  ;; We can look at the watches on the current directory
  (watch/list (io/file "."))
  => (contains {:save fn?})
  
  ;; Create a file to see if the watch triggers
  (spit "happy.hara" "hello")
  
  ;; It does!
  @*happy*
  => [:create "happy.hara"]
  
  ;; We see that the one-shot watch has worked
  (watch/list (io/file "."))
  => {})

[[:section {:title "Watch Options"}]]

"There are a couple of cenfigurable options for the filewatch:

- `:types` determine which actions are responded to. The possible values are 
   - `:create`, when a file is created
   - `:modify`, when a file is mobifies
   - `:delete`, when a file is deleted
   - or a combination of them
- `:recursive` determines if subfolders are also going to be responded to
- `:filter` will pick out only files that match this pattern.
- `:exclude` wil leave out files that match this patter
- `:mode`, can be either :sync or :async"

[[:section {:title "Components"}]]

"It was actually very easy to build `hara.io.watch` using the idea of something that is startable and stoppable. `watcher`, `start-watcher` and `stop-watcher` all follow the conventions and so it becomes easy to wrap the component model around the three methods:"

(comment
  (require '[hara.component :as component]
           '[hara.io.watch :refer :all])

  (extend-protocol component/IComponent
    Watcher
    (component/-start [watcher]
      (println "Starting Watcher")
      (start-watcher watcher))

    (component/-stop [watcher]
      (println "Stopping Watcher")
      (stop-watcher watcher)))

  (def w (component/start
          (watcher ["."] println
                   {:types #{:create :modify}
                    :recursive false
                    :filter  [".clj"]
                    :exclude [".git"]
                    :async false})))

  (component/stop w))
