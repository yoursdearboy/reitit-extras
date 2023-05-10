(ns reitit.extras.routes
  "Reverse routing (to URI string) for reitit with servlet's context support."
  (:require [clojure.string :as str]
            [reitit.core :as r]))

(defn path-for*
  ([request name] (path-for* request name {}))
  ([request name query-params] (path-for* request name query-params {}))
  ([request name query-params path-params]
   (str
    (:servlet-context-path request "") "/"
    (str/replace-first
     (cond (string? name) name
           :else (r/match->path
                  (r/match-by-name
                   (request :reitit.core/router)
                   name
                   query-params) path-params))
     #"^/" ""))))

(defmacro path-for [& args]
  `(path-for* ~'request ~@args))
