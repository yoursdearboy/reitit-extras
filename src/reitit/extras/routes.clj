(ns reitit.extras.routes
  "Reverse routing (to URI string)."
  (:require [clojure.string :as str]
            [reitit.core :as r]
            [reitit.impl :as impl]))

(defn path-for*
  ([request name] (path-for* request name {}))
  ([request name query-params] (path-for* request name query-params {}))
  ([request name query-params path-params] (path-for* request name query-params path-params :encode true))
  ([request name query-params path-params & {:keys [encode]}]
   (let [path (cond (string? name) name
                    :else (r/match->path
                           (r/match-by-name
                            (request :reitit.core/router)
                            name
                            query-params)
                           path-params))]
     (if encode path (impl/url-decode path)))))

(defmacro path-for [& args]
  `(path-for* ~'request ~@args))
