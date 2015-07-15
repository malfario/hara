(ns documentation.hara-reflect
  (:use midje.sweet)
  (:require [hara.reflect :refer :all]))

 [[:chapter {:title "Introduction"}]]

 "
 `hara.reflect` is a library for jvm reflection. It is designed to be used for testing, repl based development, and blantant hacks bypassing the jvm security mechanism. The library was originally developed as a seperate library - [iroh](https://github.com/zcaudate/iroh) but has been included as part of the larger [hara](https://github.com/zcaudate/hara) codebase."

 [[:section {:title "Installation"}]]

  "Add to `project.clj` dependencies (use double quotes):

      [im.chit/hara.reflect '{{PROJECT.version}}']"

 "All functionality is found contained in the `hara.reflect` namespace"

 (comment (use 'hara.reflect))

[[:section {:title "Motivation"}]]

"
When working and understanding encapsulated code, the best way is to expose everything first, then to test each piece of functionality in a controlled way. Finally only when all the pieces are known, then work out a strategy for code refactoring/rewriting.

Although private and protected keywords have their uses in java, they may also be considered functionality obsfucators. They are complete hinderences when a developer attempts do something to the code base that the previous author had not intended - one of them being to understand what is going on underneath. 

If the previous author had taken shortcuts in design, those private keywords turn one of those over-protective parents that get in the way of the growth of their children. Taking inspiration from clj-wallhack, here are some primary use cases for the library:

- To explore the members of classes as well as all instances within the repl
- To be able to test methods and functions that are usually not testable, or very hard to test:
- Make hidden class members visible by providing access to private methods and fields
- Make immutable class members flexible by providing ability to change final members (So that initial states can be set up easily)
- Extract out class members into documented and executable functions (including multi-argument functions)
- Better understand jvm security and how to dodge it if needed
- Better understand the java type system as well as clojure's own interface definitions
- To make working with java fun again"

[[:chapter {:title "Querying"}]]

[[:section {:title "Basics" :tag "query-basics"}]]

"There are two different calls for querying - `query-instance` and `query-class`. They have the same listing and filtering mechanisms but they do things a little differently. Both has the same structure, given as follows:"

(comment  
  (query-<command> <obj> [<option> ...]))

""

[[:subsection {:title "query-class"}]]

"Holds the java view of the Class declaration, staying true to the class and its members. It will show all methods and fields defined within a Class."

(fact 
  (query-class 1 [:name])
  => '("MAX_VALUE" "MIN_VALUE" "SIZE" "TYPE" "bitCount" "byteValue" "compare" "compareTo" "decode" "doubleValue" "equals" "floatValue" "getChars" "getLong" "hashCode" "highestOneBit" "intValue" "longValue" "lowestOneBit" "new" "numberOfLeadingZeros" "numberOfTrailingZeros" "parseLong" "reverse" "reverseBytes" "rotateLeft" "rotateRight" "serialVersionUID" "shortValue" "signum" "stringSize" "toBinaryString" "toHexString" "toOctalString" "toString" "toUnsignedString" "value" "valueOf"))

[[:subsection {:title "query-instance"}]]

"Holds the runtime view of Objects and what methods could be applied to that instance. The method will also look up the inheritance tree of the instance to fill in additional functionality and therefore non-static methods of java.lang.Object will always be shown:"

(fact
  (query-instance 1 [:name])
  => '("byteValue" "clone" "compareTo" "doubleValue" "equals" "finalize" "floatValue" "getClass" "hashCode" "intValue" "longValue" "notify" "notifyAll" "shortValue" "toString" "value" "wait"))

"The reason that `static` fields and methods are not shown for the instance is that we want all the operations that can operate on an instantiated object. Static accessors and methods are strictly operations belonging to the class and so cannot operate on an instance of the class."

[[:section {:title "Usage"}]]

"Lets see how reflection is used. We will do a search for `MIN_VALUE` with a long"

(comment
  (query-class 1 ["MIN_VALUE"])
  ;;=> (#[MIN_VALUE :: <java.lang.Long> | long])
  )

"We can open up the returned result and take a look at the internals of the element itself"

(fact
  (-> (query-class 1 ["MIN_VALUE"])
      first
      :all)
  => (contains {:origins [java.lang.Long]
                :hash number?
                :delegate #(instance? java.lang.reflect.Field %)
                :name "MIN_VALUE",
                :static true,
                :params [Class],
                :type Long/TYPE
                :modifiers #{:public :static :field :final},
                :container java.lang.Long,
                :tag :field}))

"There is a bunch of meta information regarding the reflected field. This is used for searching and filtering. We don't actually need to know too much about the internals to use it. Instead of using `first`, we can use `:#` to grab the first element in the search results:"

(def min-value (query-class 1 ["MIN_VALUE" :#]))

"Now we can use as if it is any other function:"

(fact  
  (min-value Long)
  => -9223372036854775808)

[[:subsection {:title "Multiarity"}]]

"Another cool feature of `:#` is that it will automatically merge functions into one taking multiple arities and types. For instance, if `valueOf` is queried for in String, it will have 8 different types of inputs:"

(comment
  (query-class String ["valueOf" :#])
  ;;=> #[valueOf :: ([char]),
  ;;                ([java.lang.Object]),
  ;;                ([boolean]),
  ;;                ([char[] int int]),
  ;;                ([char[]]),
  ;;                ([long]),
  ;;                ([float]),
  ;;                ([int]),
  ;;                ([double])]
  )

"We can capture the method and use it with a variety of inputs:"

(def value-of (query-class String ["valueOf" :#]))

(fact
  (value-of \c)  => "c"   ; char
  (value-of 100) => "100" ; long
  (value-of 3.9) => "3.9" ; double

  
  (let [arr (make-array Character/TYPE 5)
        _   (doall (map-indexed (fn [i v]
                                  (aset arr i v))
                                [\h \e \l \l \o]))]
    (value-of arr) => "hello"    ; char []
    (value-of arr 0 2) => "he"   ; char[], int, int
))

[[:section {:title "Options"}]]

"
The option array takes selectors and filters can be used to customise the results returned by the two query calls.

- attribute selection
- name filtering
- parameter filtering
- modifier filtering
- return type filtering
"

[[:section {:title "Selectors"}]]

"They can be in the form of selector keywords:"

(fact
  (query-class Long [:name "MIN_VALUE" :#])
  => "MIN_VALUE"
  
  (query-class Long [:params "MIN_VALUE" :#])
  => [java.lang.Class]
  
  (query-class Long [:params :name "MIN_VALUE" :#])
  => {:name "MIN_VALUE", :params [java.lang.Class]})

"Additional selector keywords include `:container`, `:hash`, `:delegate`, `:origins`, `:name`, `:modifiers`, `:tag` and `:type` and used as follows:"

(fact
  (query-class Long [:params :name :modifiers "MIN_VALUE" :#])
  => {:modifiers #{:public :static :field :final},
      :name "MIN_VALUE",
      :params [java.lang.Class]})

[[:section {:title "Name Filtering"}]]

"We can filter on the name of the class member using two methods - exact matches using strings and regex matchesusing regexs:"

(fact
  (query-class Long [:name "value"])
  => '("value")
  
  (query-class Long [:name #"value"])
  => '("value" "valueOf")
  
  (query-class Long [:name #"VALUE"])
  => '("MAX_VALUE" "MIN_VALUE"))

[[:section {:title "Parameter Filtering"}]]

[[:subsection {:title "Number of Inputs"}]]

"Input parameters can be filtered through specifying the number of inputs:"

(fact
  (query-class Long [:name :params 2])
  => [{:name "compare", :params [Long/TYPE Long/TYPE]}
      {:name "compareTo", :params [Long Long]}
      {:name "compareTo", :params [Long Object]}
      {:name "equals", :params [Long Object]}
      {:name "getLong", :params [String Long]}
      {:name "getLong", :params [String Long/TYPE]}
      {:name "parseLong", :params [String Integer/TYPE]}
      {:name "rotateLeft", :params [Long/TYPE Integer/TYPE]}
      {:name "rotateRight", :params [Long/TYPE Integer/TYPE]}
      {:name "toString", :params [Long/TYPE Integer/TYPE]}
      {:name "toUnsignedString", :params [Long/TYPE Integer/TYPE]}
      {:name "valueOf", :params [String Integer/TYPE]}])

[[:subsection {:title "Exact Inputs"}]]

"Exact inputs can be specified by using a vector with input types:"

(fact
  (query-class Long [:name :params [Long/TYPE]])
  => [{:name "bitCount", :params [Long/TYPE]}
      {:name "highestOneBit", :params [Long/TYPE]}
      {:name "lowestOneBit", :params [Long/TYPE]}
      {:name "new", :params [Long/TYPE]}
      {:name "numberOfLeadingZeros", :params [Long/TYPE]}
      {:name "numberOfTrailingZeros", :params [Long/TYPE]}
      {:name "reverse", :params [Long/TYPE]}
      {:name "reverseBytes", :params [Long/TYPE]}
      {:name "signum", :params [Long/TYPE]}
      {:name "stringSize", :params [Long/TYPE]}
      {:name "toBinaryString", :params [Long/TYPE]}
      {:name "toHexString", :params [Long/TYPE]}
      {:name "toOctalString", :params [Long/TYPE]}
      {:name "toString", :params [Long/TYPE]}
      {:name "valueOf", :params [Long/TYPE]}])

[[:subsection {:title "Partial Inputs"}]]

"Using a vector with `:any` as the first input will output all functions with any of the types as input arguments"

(fact
  (query-class Long [:name [:any String Long]])
  => ["bitCount" "compare" "decode" "getChars" "getLong" "highestOneBit" "lowestOneBit" "new" "numberOfLeadingZeros" "numberOfTrailingZeros" "parseLong" "reverse" "reverseBytes" "rotateLeft" "rotateRight" "signum" "stringSize" "toBinaryString" "toHexString" "toOctalString" "toString" "toUnsignedString" "valueOf"])

"Using a vector with `:all` as the first input will output all functions having all of the types as input arguments"

(fact
  (query-class Long [:name :params [:all String Long]])
  => [{:name "getLong", :params [String Long/TYPE]}])

[[:section {:title "Modifier Filtering"}]]

"The following are all the modifier keywords that can be used for filtering, most are directly related to flags, four have been defined for completeness of filtering:"

(comment
  :public         1      ;; java.lang.reflect.Modifier/PUBLIC
  :private        2      ;; java.lang.reflect.Modifier/PRIVATE
  :protected      4      ;; java.lang.reflect.Modifier/PROTECTED
  :static         8      ;; java.lang.reflect.Modifier/STATIC
  :final          16     ;; java.lang.reflect.Modifier/FINAL
  :synchronized   32     ;; java.lang.reflect.Modifier/SYNCHRONIZE
  :native         256    ;; java.lang.reflect.Modifier/NATIVE
  :interface      512    ;; java.lang.reflect.Modifier/INTERFACE
  :abstract       1024   ;; java.lang.reflect.Modifier/ABSTRACT
  :strict         2048   ;; java.lang.reflect.Modifier/STRICT
  :synthetic      4096   ;; java.lang.Class/SYNTHETIC
  :annotation     8192   ;; java.lang.Class/ANNOTATION
  :enum           16384  ;; java.lang.Class/ENUM
  :volatile       64     ;; java.lang.reflect.Modifier/VOLATILE
  :transient      128    ;; java.lang.reflect.Modifier/TRANSIENT
  :bridge         64     ;; java.lang.reflect.Modifier/BRIDGE
  :varargs        128    ;; java.lang.reflect.Modifier/VARARGS

  :plain          0      ;; not :public, :private or :protected
  :instance       0      ;; not :static
  :field          0      ;; is field
  :method         0      ;; is method
)

[[:subsection {:title "Modifier Examples"}]]

"Find all the fields in `java.lang.Long`:"
(fact
  (query-class Long [:name :field])
  => ["MAX_VALUE" "MIN_VALUE" "SIZE" "TYPE" "serialVersionUID" "value"])

"Find all the static fields in `java.lang.Long`:"
(fact
  (query-class Long [:name :static :field])
  => ["MAX_VALUE" "MIN_VALUE" "SIZE" "TYPE" "serialVersionUID"])

"Find all the non-static fields in `java.lang.Long`:"
(fact
  (query-class Long [:name :instance :field])
  => ["value"])

"Find all public fields in `java.lang.Long`:"
(fact
  (query-class Long [:name :public :field])
  => ["MAX_VALUE" "MIN_VALUE" "SIZE" "TYPE"])

"Find all private members in `java.lang.Long`:"
(fact
  (query-class Long [:name :private])
  => ["serialVersionUID" "toUnsignedString" "value"])

"Find all private fields in `java.lang.Long`:"
(fact
  (query-class Long [:name :private :field])
  => ["serialVersionUID" "value"])

"Find all private methods in `java.lang.Long`:"
(fact
  (query-class Long [:name :private :method])
  => ["toUnsignedString"])

"Find all protected members in `java.lang.Long`:"
(fact
  (query-class Long [:name :protected])
  => [])

"Find all members in `java.lang.Long` with no security attribute:"
(fact
  (query-class Long [:name :plain])
  => ["getChars" "stringSize"])

[[:section {:title "Return Type Filtering"}]]

"Return types signatures can be filtered by giving a class in the options, again all filters can be mixed and matched as needed. In the following example, we query for the name of all methods having a return type of `Long/TYPE`:"

(fact
  (query-class Long [:name :type :method Long/TYPE])
  => [{:name "highestOneBit", :type Long/TYPE}
      {:name "longValue", :type Long/TYPE}
      {:name "lowestOneBit", :type Long/TYPE}
      {:name "parseLong", :type Long/TYPE}
      {:name "parseLong", :type Long/TYPE}
      {:name "reverse", :type Long/TYPE}
      {:name "reverseBytes", :type Long/TYPE}
      {:name "rotateLeft", :type Long/TYPE}
      {:name "rotateRight", :type Long/TYPE}])

[[:chapter {:title "Member Operations"}]]

[[:section {:title "apply-element"}]]

"`apply-element` is for function invocation of an object. It uses `query-instance` underneath the hood to figure what to call but is faster when used more than once because the reflected member is cached."

(fact
  (apply-element "HELLO" "charAt" [0])
  => \H

  (seq (apply-element "HELLO" "value" []))
  => [\H \E \L \L \O])

[[:section {:title "delegate"}]]

"`delegate` does what `bean` does but instead of manipulating fields through accessor functions, it directly gives field access to the underlying object. This way, one can set and get values from any object, regardless of permission model. `delegate` is used most often to look at the entire state of an object as well as to set the fields of an object to a particular initial state for testing. In the following example, we will change the value of a normally immutable string"

(fact
  (def a "hello")
  a => "hello"

  (def a* (delegate a))
  (deref a*)   ; dereferencing the delegate will give all the fields
  => (contains {:hash 99162322,
                :hash32 0,
                :value #(instance? (Class/forName "[C") %)})
  
  (:hash a*)   ; values can be accessed just like bean
  => 99162322

  (seq (a* :value))     ; getter call
  => [\h \e \l \l \o]

  (a* :value (char-array "world"))   ; setter call
  a => "world" ; oh look! we have secretly changed an immutable object
)


[[:chapter {:title "Class Information"}]]

[[:section {:title "class-info"}]]

"Information about a class can be accessed using `class-info`. Either a class or an instance can be passed it. The contextual class will be determined automatically:"

(fact
  (class-info Long)
  => (contains {:name "java.lang.Long"
                :hash number?
                :modifiers #{:instance :public :final :class}}))

"It is interesting to be able explore the underlying implementation of clojure itself. For example, if there are nine elements passed to a zipmap, the type is an `PersistentArrayMap` whereas is ten elements are passed to the zipmap, the object becomes a `PersistentHashMap`:"

(fact
  (class-info (zipmap (range 9) (range 9)))
  => (contains {:name "clojure.lang.PersistentArrayMap"
                :hash number?
                :modifiers #{:instance :public :class}})
  
  (class-info (zipmap (range 10) (range 10)))
  => (contains {:name "clojure.lang.PersistentHashMap",
                :hash number?
                :modifiers #{:instance :public :class}}))

[[:section {:title "class-hierarchy"}]]

"The class hierarchy can be readily viewed. It is given in the form of a vector. The first element is always the class itself, the subsequent elements are split into the base/super class as well as all extended interfaces:"

(fact
  (class-hierarchy String)
  => [java.lang.String [java.lang.Object ; base class
                        #{java.io.Serializable ;interfaces
                          java.lang.CharSequence
                          java.lang.Comparable}]])

"Here is the type hierarchy of a `PersistentHashMap`:"

(comment
  (class-hierarchy clojure.lang.PersistentHashMap)
  => [clojure.lang.PersistentHashMap

      [clojure.lang.APersistentMap
       #{clojure.lang.IObj
         clojure.lang.IEditableCollection
         clojure.lang.IMapIterable}]

      [clojure.lang.AFn
       #{clojure.lang.IHashEq
         java.io.Serializable
         clojure.lang.IPersistentMap
         java.util.Map
         java.lang.Iterable
         clojure.lang.MapEquivalence}]

      [java.lang.Object
       #{clojure.lang.IFn}]])

[[:chapter {:title "Extraction"}]]

[[:section {:title "extract-to-var"}]]

"Often, we need to extract out one or all elements of a class for use. `extract-to-var` and `extract-to-ns` allows for this. We see how extract-to-var works:"

(comment
  (extract-to-var 'char-at String "charAt")

  (mapv #(char-at "Hello" %) (range 5))
  => [\H \e \l \l \o])

"The function can also take a fully qualified symbol as well as create meta-information regarding the var itself. "

(comment
  (extract-to-var 'hello/char-at String "charAt")

  (hello/char-at "Hello" 4)
  => \o
  
  (meta #'hello/char-at)
  => (contains {:ns #(instance? clojure.lang.Namespace %)
                :name 'char-at
                :arglists '[[java.lang.String int]]
                :doc "\nmember: java.lang.String/charAt\ntype: char\nmodifiers: instance, method, public"}))

[[:section {:title "extract-to-ns"}]]

"`extract-to-ns` takes a class and extracts out all methods to a particular namespace, filters can be applied as seen in the [selectors](#selectors) section:"

(comment
  (extract-to-ns 'test.string String [:private])
  => [#'test.string/HASHING_SEED
      #'test.string/checkBounds
      #'test.string/hash
      #'test.string/hash32
      #'test.string/indexOfSupplementary
      #'test.string/lastIndexOfSupplementary
      #'test.string/serialPersistentFields
      #'test.string/serialVersionUID
      #'test.string/value]

  (seq (test.string/value "hello"))
  => [\h \e \l \l \o])

"If just the class is passed in, it will dump all methods of the class into the current namespace. This is very useful for quickly getting information about the internal structure of a class when exploring a new package. It is not the best idea to do this in production, but having the ability to do so whilst developing is great. We can see the `valueOf` function being called after all members in `java.lang.Long` have been extracted:"

(comment
  (extract-to-ns Long)
  
  (clojure.repl/doc valueOf)
  ;;   -------------------------
  ;;   documentation.hara-reflect/valueOf
  ;;   [[java.lang.String int] [java.lang.String] [long]]
  
  (valueOf "72" 10)
  => 72

  (valueOf "72" 16)
  => 114)
