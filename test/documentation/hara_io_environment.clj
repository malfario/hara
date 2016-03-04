(ns documentation.hara-io-environment
  (:require [hara.io.environment :as env]))

[[:chapter {:title "Introduction"}]]

"[hara.io.environment](https://github.com/zcaudate/hara/blob/master/src/hara/io/environment.clj) provides an easier interface for working with jvm properties and versioning."

[[:section {:title "Installation"}]]

"Add to `project.clj` dependencies:

    [im.chit/hara.io.environment \"{{PROJECT.version}}\"]"

"All functions are in the `hara.io.environment` namespace."

(comment (require '[hara.io.environment :as env]))

[[:chapter {:title "API"}]]

[[:api {:namespace "hara.io.environment"}]]
