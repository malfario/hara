(ns documentation.hara-object
  (:require [hara.object :as object]))

[[:chapter {:title "Introduction"}]]

"
[hara.object](https://github.com/zcaudate/hara/blob/master/src/hara/object.clj) is a library for converting java classes into clojure data types. It is somewhat like the `bean` command but enables more control and customisation of the output."

[[:section {:title "Installation"}]]

"
Add to `project.clj` dependencies:

    [im.chit/hara.object \"{{PROJECT.version}}\"]"

"All functionality is found contained in the `hara.object` namespace"

(comment (require '[hara.object :as object]))

[[:section {:title "Motivation"}]]

"
Quoting Wikipedia:

> Object-oriented programming (OOP) is a programming paradigm based on the concept of objects, which are structures that contain data, in the form of fields, often known as attributes; and code, in the form of procedures, often known as methods.

Clojure is tightly bounded to Java and so cannot escape from the world of objects. Objects provide data encapsulation, meaning that it is very difficult to access data within an object except through custom interfaces. Encapsulation run counter to the openess of Clojure's philosophy where all data can be represented as a map.

Reflection provides a way around this problem. The general technique is to query the object for the names and values of it's fields and return them as a map. [hara.object](https://www.github.com/zcaudate/hara) does just that. We cannot represent all fields in all objects as a datastructure (especially when there are circular references). However, in most cases, we just need to grab the most critical information from an object in order to make sense of what is going on and to then decide on a course of action; [hara.object](https://www.github.com/zcaudate/hara) has been built for this approach, allowing customization of what fields to return and exclude.
"