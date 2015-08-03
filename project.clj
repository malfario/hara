(defproject im.chit/hara "2.2.5"
  :description "patterns and utilities"
  :url "https://github.com/zcaudate/hara"
  :license {:name "The MIT License"
            :url "http://http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.6.0"]]
  :profiles {:dev {:dependencies [[midje "1.6.3"]
                                  [compojure "1.4.0"]
                                  [ring "1.4.0"]
                                  [clj-http "1.1.2"]]
                   :plugins [[lein-midje "3.1.3"]
                             [lein-repack "0.2.10"]]}}
  :documentation {:site   "hara"
                  :output "docs"
                  :description "patterns and utilities"
                  :tracking "UA-31320512-2"
                  :owners [{:name    "Chris Zheng"
                            :email   "z@caudate.me"
                            :website "http://z.caudate.me"}]
                  :template {:path "template"
                             :copy ["assets/css" "assets/js"]
                             :defaults {:template "article-basic.html"
                                        :navbar  [:file "partials/navbar.html"]
                                        :sidebar [:file "partials/sidebar.html"]
                                        :footer  [:file "partials/footer.html"]
                                        :dependencies [:file "partials/deps-web.html"]
                                        :contentbar  :navigation
                                        :article     :article}}
                  :paths ["test/documentation"]
                  :files {"index"
                          {:template "home.html"
                           :title "hara"
                           :subtitle "patterns and utilities"}
                          ;"api"
                          ;{:input "test/documentation/hara_api.clj"
                          ; :title "api examples"}
                          "hara-component"
                          {:input "test/documentation/hara_component.clj"
                           :title "component"
                           :subtitle "constructing composable systems"}
                          "hara-concurrent-ova"
                          {:input "test/documentation/hara_concurrent_ova.clj"
                           :title "concurrent.ova"
                           :subtitle "shared mutable state for multi-threaded applications"}
                          ;"hara-concurrent-procedure"
                          ;{:input "test/documentation/hara_concurrent_procedure.clj"
                          ; :title "concurrent.procedure"
                          ; :subtitle "generic model for controllable execution"}
                          "hara-event"
                          {:input "test/documentation/hara_event.clj"
                           :title "event"
                           :subtitle "event signalling and conditional restart"}
                          "hara-extend-abstract"
                          {:input "test/documentation/hara_extend_abstract.clj"
                           :title "extend.abstract"
                           :subtitle "the abstract container pattern generator"}
                          "hara-io-scheduler"
                          {:input "test/documentation/hara_io_scheduler.clj"
                           :title "io.scheduler"
                           :subtitle "easy and intuitive task scheduling"}
                          ;"hara-io-watch"
                          ;{:input "test/documentation/hara_io_watch.clj"
                          ; :title "io.watch"
                          ; :subtitle "watch for filesystem changes"}
                          "hara-object"
                          {:input "test/documentation/hara_object.clj"
                           :title "object"
                           :subtitle "turn everything into clojure data"}
                          "hara-reflect"
                          {:input "test/documentation/hara_reflect.clj"
                           :title "reflect"
                           :subtitle "java reflection made easy"}}
                :html {:logo "hara.png"
                       :home  "index",
                       :navigation ["home"
                                    {:link "api", :text "api"}
                                    ["guides" ["hara-component"
                                               "hara-concurrent-ova"
                                               ;;"hara-concurrent-procedure"
                                               "hara-event"
                                               ;;"hara-io-watch"
                                               "hara-io-scheduler"
                                               "hara-reflect"]]
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
            :standalone #{"reflect" "time" "event" "object"}}])
