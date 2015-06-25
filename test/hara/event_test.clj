(ns hara.event-test)

(comment
  (deflistener log-listener :log
    [e]
    (println "LOG:" (:message e)))

  (common/match-handlers @*signal-manager* {:log true :message "oeuoeu"})

  (signal [:log {:message "This is a log message"}]))