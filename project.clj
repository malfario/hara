(defproject im.chit/hara "2.1.12"
  :description "code patterns and utilities"
  :url "https://github.com/zcaudate/hara"
  :license {:name "The MIT License"
            :url "http://http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.6.0"]]
  :profiles {:dev {:dependencies [[midje "1.6.3"]]
                   :plugins [[lein-midje "3.1.3"]
                             [lein-midje-doc "0.0.23"]
                             [lein-repack "0.2.10"]]}}
  :documentation {:type   :portfolio
                  :name   "hara"
                  :output "docs/index.html"
                  :description "code patterns and utilities"
                  :tracking "UA-31320512-2"
                  :owners [{:name    "Chris Zheng"
                            :email   "z@caudate.me"
                            :website "http://z.caudate.me"}]
                  :paths ["test/documentation"]
                  :files {
                          ;"home"
                          ;{:input "test/midje_doc/guides/home.clj"
                          ; :template "full"
                          ; :title "home"}
                          ;"orientation"
                          ;{:input "test/midje_doc/hara/orientation.clj"
                          ; :title "hara in small pieces"
                          ; :link {:auto-number false}}
                          ;"api"
                          ;{:input "test/midje_doc/hara/outline.clj"
                          ; :title "api reference"}
                          ;"hara.component"
                          ;{:input "test/midje_doc/guides/hara_component.clj"
                          ; :title "hara.component - composable systems"}
                          "hara.ova"
                          {:input "test/documentation/hara/ova.clj"
                           :title "hara.ova - reactive array"}
                          ;"hara.io.watch"
                          ;{:input "test/midje_doc/guides/hara_io_watch.clj"
                          ; :title "hara.io.watch - file watcher"}
                          ;"hara.io.timer"
                          ;{:input "test/midje_doc/guides/hara_io_timer.clj"
                          ; :title "hara.io.scheduler - task scheduler"}
                          }
                :html {:logo "hara.png"
                       :theme "clean"
                       :home "home",
                       :navigation ["quickstart"
                                    ["guides" ["hara.component" 
                                               "hara.ova" 
                                               "hara.io.watch" 
                                               "hara.io.timer"]]
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
            :standalone #{"reflect"}}])
            