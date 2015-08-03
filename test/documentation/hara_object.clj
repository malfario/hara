(ns documentation.hara-object
  (:use midje.sweet)
  (:require [hara.object :refer :all]))

[[:chapter {:title "Introduction"}]]

"
[hara.object](https://github.com/zcaudate/hara/blob/master/src/hara/object.clj) is a library for converting java classes into clojure data types. It is somewhat like the `bean` command but enables more control and customisation of the output data."

[[:section {:title "Installation"}]]

"Add to `project.clj` dependencies:

   [im.chit/hara.object \"{{PROJECT.version}}\"]"

"All functionality is found contained in the `hara.object` namespace"

(comment (require '[hara.object :as obj]))

[[:section {:title "Motivation"}]]

"
[hara.object](https://github.com/zcaudate/hara/blob/master/src/hara/object.clj) has been extracted out from [gita](https://github.com/zcaudate/gita) where it was used to analyse the [jgit porcelain](https://wiki.eclipse.org/JGit/User_Guide#Porcelain_API) api and to reflectively generate an interface for git. A similar technique was used to access cassandra datastructures in [cassius](https://github.com/mypost/cassius) though it was done through protocols and not in such a generic fashion. The reason one may wish to use such a library would be to quickly visualize the structure of a class as well as to customize how the class looks in the repl. Of course it can also be used to wrap java apis.
"

[[:chapter {:title "Understanding JavaFX"}]]

(comment

  (import javafx.scene.layout.StackPane
          javafx.scene.Scene
          javafx.stage.Stage
          javafx.scene.control.Button)

  (defonce force-toolkit-init (javafx.embed.swing.JFXPanel.))

  (extend-maplike java.awt.Container
                  {:tag "container"
                   :include [:components :x :y :size]}
                  java.awt.Dimension
                  {:tag "dimension"
                   :include [:width :height]})

  (StackPane)

  (.* force-toolkit-init #"arent" :name)


  (count (object-getters force-toolkit-init))


  (def root (StackPane.))
  (def scene (Scene. root 300 250))
  (def stage (Stage.)))
