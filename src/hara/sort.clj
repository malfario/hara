(ns hara.sort
  (:require [hara.namespace.import :as ns]
            [hara.sort.hierarchical]
            [hara.sort.topological]))

(ns/import
 hara.sort.hierarchical   :all
 hara.sort.topological   :all)
