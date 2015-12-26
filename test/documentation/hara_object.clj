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


"
[hara.object](https://github.com/zcaudate/hara/blob/master/src/hara/object.clj) has been extracted out from [gita](https://github.com/zcaudate/gita) where it was used to analyse the [jgit porcelain](https://wiki.eclipse.org/JGit/User_Guide#Porcelain_API) api and to reflectively generate an interface for git. A similar technique was used to recursively access cassandra datastructures in [cassius](https://github.com/mypost/cassius) though it was done through protocols and not in such a generic fashion.

The reason one may wish to use such a library would be to quickly visualize the structure of a class as well as to customize how the class looks in the repl. It can also be used to wrap java apis. The library is very similar in concept to the `def-map-type` and `def-derived-type` in [potemkin](https://github.com/ztellman/potemkin) but instead of defining a wrapper to the class, data with the class is accessed directly through reflection. Currently the implementation is not optimised though it has an extremely broad use cases.

Libraries that rely on `hara.object` for exposing functionality:
- [gita](https://github.com/zcaudate/gita)
- [gulfstream](https://github.com/helpshift/gulfstream)
"

[[:chapter {:title "Philosophy"}]]

"`hara.object` works at the level of meta-programming. As explained in the previous section, there is a correspondence between a class and the data with the class. Below shows a simple example of the concept of `Dog` as an `Object` and as data:
"

[[:image {:src "img/hara_object/dog.png" :width "600px" :title "Class as Data"}]]

"There are advantages of using pure data for the representation of the `Dog` concept.

- generic methods con be used to manipulate the data
- the entire structure is transparent is better for reasoning
- simpler representation (though at the cost of Type Correctness)

In this way, many objects can be turned into maps/data for consumption by clojure methods. This makes working with many big java libraries much easier before."


[[:chapter {:title "Stringlike"}]]

"
It is best to look at a real world example of how to use such a library.

[gita](https://www.github.com/zcaudate/gita) is a wrapper around the popular [jgit](https://eclipse.org/jgit/) project. Due to the enormous amount of functionality around [git](http://www.git.org), it is very difficult to manually write a wrapper around the entire suite. The novelty of [gita](https://www.github.com/zcaudate/gita) is that it uses [hara.object](https://www.github.com/zcaudate/hara) for customisation of the wrapper interface in such a way that the entire functionality of the main `org.eclipse.jgit.api.Git` class is accessible and usable in a clojure compatible convention.
"

"`hara.object` is based on a very simple idea of treating objects as being `stringlike` or a `maplike` . What is meant by `stringlike` is that the object can be represented into a string. The reverse; that the object can be constructed from a string is not always true but there are very few exceptions."

[[:section {:title "File"}]]

"An example is shown below when we use the `extend-stringlike` method to extend the functionality to `java.io.File`."

(object/extend-stringlike
 java.io.File
 {:tag "path"
  :to .getPath
  :from (fn [^String path _] (java.io.File. path))})

"The three keys that are important in the map are:

- `:tag` identifying the type of data
- `:to` which converts the object to string
- `:from`, a function taking two paramters that constructs the object out of a string

Constructing a File object, notice that the output is different from before:"

(java.io.File. "/home") ;;=> #path "/home"

"Now that stringlike has been extended to `java.io.File`, generic data conversion methods can be used. `to-data` converts the object to a string:"

(object/to-data (java.io.File. "/home")) ;;=> "/home"

"`from-data` converts a string back into an instance of a `File`."

(object/from-data "/home" java.io.File)  ;;=> #path "/home"

[[:section {:title "Repository"}]]

"`extend-stringlike` are implementated for `jgit` classes in [gita](https://github.com/zcaudate/gita/src/gita/interop/string_like.clj). The example below shows the `Repository` object:"

(object/extend-stringlike
 org.eclipse.jgit.lib.Repository
 {:tag   "repository"
  :to     (fn [^org.eclipse.jgit.lib.Repository repo]
            (-> repo (.getDirectory) object/to-data))
  :from   (fn [^String path _]
            (org.eclipse.jgit.internal.storage.file.FileRepository. path))})

"It's usage can be seen:"

(object/from-data "/home" org.eclipse.jgit.lib.Repository) ;;=> #repository "/home"

"What this allows is for back and forth coercion of similar types of objects using a very generic framework. We see the back and forth conversion of a string to a File and Repository object below:"

(-> "/home"
    (object/from-data java.io.File)
    (object/to-data)
    (object/from-data org.eclipse.jgit.lib.Repository)
    (object/to-data))
;;=> "/home"

[[:section {:title "Enums"}]]

"Enumerations are considered to be stringlike. Therefore `to-data` is used to convert a enum constants to a string represention"

(object/to-data java.lang.Thread$State/NEW)
;;=> "NEW"

"`from-data` is used to convert a string back into the enum represention"

(object/from-data "NEW" java.lang.Thread$State)
;;=> #<State NEW>

"All enumeration values can be obtained using `enum-values`"

(->> (object/enum-values java.lang.Thread$State)
     (map object/to-data)
     (sort))
;;=> ("BLOCKED" "NEW" "RUNNABLE" "TERMINATED" "TIMED_WAITING" "WAITING")


[[:chapter {:title "Maplike"}]]

"Classes are containers of fields and methods. Therefore, they can be considered maplike because instances of the class contain fields that hold data about its state. [hara.reflect](hara-reflect.html) allows access of fields through the [delegate](hara-reflect.html#delegate) function. However, it has been found that this is a little too overpowered and a more subtle way would be to use the `getter` and `setter` methods."

[[:section {:title "Primitives"}]]

"`hara.object` accesses class information through the class getters:"

(-> (object/object-getters org.eclipse.jgit.api.Status)
    (keys)
    (sort))
;;=> (:added :changed :class :clean? :conflicting :conflicting-stage-state :ignored-not-in-index :missing :modified :removed :uncommitted-changes :uncommitted-changes! :untracked :untracked-folders)

"And sets data through class setters. In the case for `Status`, because it is readonly, there are no setters to show"

(-> (object/object-setters org.eclipse.jgit.api.Status)
    (keys)
    (sort))
;;=> ()

"`object-getters` can be used on any object:"

(-> (object/object-getters "abc")
    (keys)
    (sort))
;;=> (:bytes :class :empty?)

"`object-data` is used to access data through getter calls."

(object/object-data "abc")
;;=> {:bytes #<byte[] [B@97d8f46>, :class java.lang.String, :empty? false}

"Calling `object-data` is the same as these two calls:"

(-> (object/object-getters "abc")
    (object/object-apply "abc" identity))
;;=> {:bytes #<byte[] [B@551e9c78>, :class java.lang.String, :empty? false}

[[:section {:title "Extend"}]]

"As for stringlike classes, maplike classes can also be seen to be extended in [gita](https://github.com/zcaudate/gita/src/gita/interop/map_like.clj). Below are examples of classes that have been extended:"

(object/extend-maplike
 org.eclipse.jgit.revwalk.RevCommit
 {:tag "commit"
  :include [:commit-time :name :author-ident :full-message]})

"Important keys are `:tag` and either `:include` which only includes tha getter keys listed or `:exclude`, which includes all the keys except those listed. For the extension of `RevCommit`, only four keys are chosen. The `:author-ident` key is a `Personident` object and also needs to be extended:"

(object/extend-maplike
 org.eclipse.jgit.lib.PersonIdent
 {:tag "person" :exclude [:time-zone]})

"Once classes have been extended, the typical usage would be to wrap a function returning the object wtih a `to-data` call. An example of this can be seen [here](https://github.com/zcaudate/gita#working-locally---add-commit-log-and-rm), where a call to `git :commit` returns a `RevCommit` object which then is converted into a hashmap via the `to-data` wrapper:"

(comment
  (git :commit :message "Added Hello.txt")
  ;;=> {:commit-time 1425683330,
  ;;    :name "9f1177ad928d7dea2afedd58b1fb7192b3523a6c",
  ;;    :author-ident {:email-address "z@caudate.me",
  ;;                   :name "Chris Zheng",
  ;;                   :time-zone-offset 330,
  ;;                   :when #inst "2015-03-06T23:08:50.000-00:00"},
  ;;    :full-message "Added Hello.txt"}
  )

"Notice that the datastructure is nested. Both `RevCommit` and `PersonIdent` have been turned into generic clojure maps in the format specified through `extend-maplike`. Most of [gita](https://github.com/zcaudate/gita) has been implementated in this way."

[[:section {:title "IData"}]]

"For more flexibility of output, classes can implementat the `hara.protocol.data/IData` protocol and extend `hara.protocol.data/-meta-object`. The use would the same method name without prefix `-` in the `hara.object` namespace. An example of this is found [here](https://github.com/zcaudate/gita/blob/master/src/gita/interop/status.clj) where the `org.eclipse.jgit.api.Status` is extended:"

(comment
  (ns gita.interop.status
    (:require [hara.reflect :as reflect]
              [hara.protocol.data :as data]
              [hara.object :as object])
    (:import org.eclipse.jgit.api.Status))


  (defmethod data/-meta-object Status
    [type]
    {:class     Status
     :types     #{java.util.Map}
     :to-data   data/-to-data})

  (extend-protocol data/IData
    Status
    (-to-data [status]
      (let [methods (reflect/query-class status [:method])]
        (->> methods
             (map (fn [ele] [(-> ele :name object/java->clojure keyword)
                             (ele status)]))
             (reduce (fn [m [k v]]
                       (if (and (or (instance? java.util.Collection v)
                                    (instance? java.util.Map v))
                                (empty? v))
                         m
                         (assoc m k v)))
                     {})))))

  (defmethod print-method Status
    [v ^java.io.Writer w]
    (.write w (str "#status::" (data/-to-data v)))))

"An example of how is is used can be seen in the `gita` readme:"

(comment
  (git :status)
  => {:clean? false, :uncommitted-changes! false, :untracked #{"hello.txt"}}
 )
