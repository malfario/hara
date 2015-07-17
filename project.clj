(defproject im.chit/hara "2.2.0"
  :description "code patterns and utilities"
  :url "https://github.com/zcaudate/hara"
  :license {:name "The MIT License"
            :url "http://http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.6.0"]]
  :profiles {:dev {:dependencies [[midje "1.6.3"]
                                  [compojure "1.4.0"]
                                  [ring "1.4.0"]
                                  [clj-http "1.1.2"]]
                   :plugins [[lein-midje "3.1.3"]
                             [lein-midje-doc "0.0.23"]
                             [lein-repack "0.2.10"]]}}
  :documentation {:site   "hara"
                  :output "docs"
                  :description "code patterns and utilities"
                  :tracking "UA-31320512-2"
                  :owners [{:name    "Chris Zheng"
                            :email   "z@caudate.me"
                            :website "http://z.caudate.me"}]
                  :template {:path "template"
                             :copy ["assets"]
                             :defaults {:sidebar [:file "partials/sidebar.html"]
                                        :footer  [:file "partials/footer.html"]
                                        :navbar  :navigation
                                        :article :article}}
                  :paths ["test/documentation"]
                  :files {"index"
                          {:type :html
                           :template "home.html"
                           :title "home"}
                          ;"api"
                          ;{:input "test/documentation/hara_api.clj"
                          ; :title "api examples"}
                          "hara-component"
                          {:input "test/documentation/hara_component.clj"
                           :title "hara.component"
                           :subtitle "constructing composable systems"}
                          "hara-concurrent-ova"
                          {:input "test/documentation/hara_concurrent_ova.clj"
                           :title "hara.concurrent.ova"
                           :subtitle "shared mutable state for multi-threaded applications"}
                          ;"hara-concurrent-procedure"
                          ;{:input "test/documentation/hara_concurrent_procedure.clj"
                          ; :title "hara.concurrent.procedure"
                          ; :subtitle ""}
                          "hara-event"
                          {:input "test/documentation/hara_event.clj"
                           :title "hara.event"
                           :subtitle "event signalling and conditional restart"}
                          ;"hara-io-watch"
                          ;{:input "test/documentation/hara_io_watch.clj"
                          ; :title "hara.io.watch"
                          ; :subtitle "watch for filesystem changes"}
                          "hara-io-scheduler"
                          {:input "test/documentation/hara_io_scheduler.clj"
                           :title "hara.io.scheduler"
                           :subtitle "easy and intuitive task scheduling"}
                          "hara-reflect"
                          {:input "test/documentation/hara_reflect.clj"
                           :title "hara.reflect"
                           :subtitle "java reflection made easy"}}
                :html {:logo "hara.png"
                       :home  "index",
                       :navigation ["quickstart"
                                    ["guides" ["hara-component"
                                               "hara-concurrent-ova"
                                               "hara-concurrent-procedure"
                                               "hara-event"
                                               "hara-io-watch"
                                               "hara-io-scheduler"
                                               "hara-reflect"]]
                                    {:link "api", :text "api"}
                                    {:link "https://gitter.im/zcaudate/hara",
                                     :text "support"}
                                    {:link "https://www.github.com/zcaudate/hara",
                                     :text "source"}]}
                :link {:auto-tag    true
                       :auto-number true}}

  :global-vars {*warn-on-reflection* true}
  :repack [{:type :clojure
            :levels 2
            :path "src"
            :standalone #{"reflect" "time" "event"}}])
