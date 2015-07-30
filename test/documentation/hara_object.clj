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
[hara.object]() has been extracted out from [gita](https://github.com/zcaudate/gita) where it was used to reflectively analyse the [jgit porcelain](https://wiki.eclipse.org/JGit/User_Guide#Porcelain_API) api to generate an interface for git. The A similar technique It takes the 
"

[[:section {:title "Other Libraries"}]]

"
"
