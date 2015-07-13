(ns documentation.hara-concurrent-ova.scoreboard
  (:use midje.sweet)
  (:require [hara.concurrent.ova :refer :all]
            [hara.common.watch :as watch]))

[[:section {:title "Data Setup"}]]

"A scoreboard is used to track player attempts, scores and high-scores"

(def scoreboard
  (ova [{:name "Bill" :attempts 0 :score {:all ()}}
        {:name "John" :attempts 0 :score {:all ()}}
        {:name "Sally" :attempts 0 :score {:all ()}}
        {:name "Fred"  :attempts 0 :score {:all ()}}]))

[[:section {:title "Notifier Setup"}]]

"`hara.event` is used to listen for a `:log` signal and print out the `:msg` component of the event.
"

(require '[hara.event :as event])

(event/deflistener print-logger
  :log
  [msg]
  (println msg))

"
We set up two watch notifiers that signal and event.

- one to print when an attempt has been made to play a game
- one to print when there is a new highscore"


(watch/add scoreboard
           :notify-attempt
           (fn [k o r p n]  ;; key, ova, ref, previous, next
             (event/signal [:log {:msg (str (:name @r) " is on attempt " n)}]))
           {:select :attempts})

(watch/add scoreboard
           :notify-high-score
           (fn [k o r p n]
             (event/signal [:log {:msg (str (:name @r) " has a new highscore of " n)}]))
           {:select [:score :highest]})

"Of course, we could have added the `println` statement directly. However, in an actual application, events may be logged to file, emailed, beeped or read back to the user. Having a light-weight event signalling framework lets that decision be made much later"

[[:section {:title "High Scores"}]]

"Another watch is added to update the high score whenever it occurs."

(watch/add scoreboard
           :update-high-score
           (fn [k o r p n]
             (let [hs    [:score :highest]
                   high  (get-in @r hs)
                   current (first n)]
               (if (and current
                        (or (nil? high)
                            (< high current)))
                 (dosync (alter r assoc-in hs current)))))
           {:select [:score :all]})

[[:section {:title "Game Simulation"}]]

"
Functions for simulation are defined with the following parameters:

- `sim-game` and `sim-n-games` are used to update the scoreboard
- the time to finish the game is randomised
- the wait-time between subsequent games is randomised
- the score they get is also randomised
"

(defn sim-game [scoreboard name]
  ;; increment number of attempts
  (dosync (!> scoreboard [:name name]
              (update-in [:attempts] inc)))

  ;; simulate game playing time
  (Thread/sleep (rand-int 500))

  ;; conj the newest score at the start of the list
  (dosync (!> scoreboard [:name name]
              (update-in [:score :all] conj (rand-int 50)))))

(defn sim-n-games [scoreboard name n]
  (when (> n 0)
    (Thread/sleep (rand-int 500))
    (sim-game scoreboard name)
    (recur scoreboard name (dec n))))

[[:section {:title "Multithreading"}]]

"
To demonstrate the use of ova within a multithreaded environment, we run the following simulation

- for each player on the scoreboard, they each play a random number of games simultaneously
- the same scoreboard is used to keep track of state
"


(defn sim! [scoreboard]
  (let [names (map :name scoreboard)]
    (doseq [nm names]
      (future (sim-n-games scoreboard nm (+ 5 (rand-int 5)))))))

"A sample simulation is show below:"


(comment
  (sim! scoreboard)

  => [Sally is on attempt 1
      Bill is on attempt 1
      Bill has a new highscore of 35
      Sally has a new highscore of 40
      John is on attempt 1
      Fred is on attempt 1

      .....

      Sally is on attempt 8
      Bill has a new highscore of 44
      Bill is on attempt 9
      Bill has a new highscore of 45]

  (<< scoreboard)

  => [{:name "Bill", :attempts 9, :score {:highest 45, :all (45 44 36 9 24 25 39 18 3)}}
      {:name "John", :attempts 7, :score {:highest 49, :all (20 37 32 8 48 37 49)}}
      {:name "Sally", :attempts 8, :score {:highest 49, :all (1 48 7 12 43 0 39 49)}}
      {:name "Fred", :attempts 5, :score {:highest 47, :all (16 40 47 15 22)}}])
