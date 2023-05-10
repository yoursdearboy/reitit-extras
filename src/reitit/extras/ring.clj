(ns reitit.extras.ring
  "Middlewares and handlers with servlet's context support."
  (:require [clojure.string :as str]
            [reitit.core :as r]))

;; Credits @ouvanous
;; https://github.com/metosin/reitit/issues/287#issuecomment-535453675
(defn servlet-middleware [handler]
  (fn [request]
    (handler (update request :uri #(str/replace-first % (:servlet-context-path request "") "")))))

;; `reitit.ring/redirect-trailing-slash-handler` with servlet context support
(defn redirect-trailing-slash-handler
  "A ring handler that redirects a missing path if there is an
  existing path that only differs in the ending slash.

  | key     | description |
  |---------|-------------|
  | :method | :add - redirects slash-less to slashed |
  |         | :strip - redirects slashed to slash-less |
  |         | :both - works both ways (default) |
  "
  ([] (redirect-trailing-slash-handler {:method :both}))
  ([{:keys [method]}]
   (letfn [(maybe-redirect [request path]
             (if (and (seq path) (r/match-by-path (:reitit.core/router request) path))
               (let [servlet-context-path (:servlet-context-path request "")
                     rel-path (str/replace-first path #"^/" "")
                     location (str servlet-context-path "/" rel-path)]
                 {:status (if (= (:request-method request) :get) 301 308)
                  :headers {"Location" location}
                  :body ""})))
           (redirect-handler [request]
             (let [uri (:uri request)]
               (if (str/ends-with? uri "/")
                 (if (not= method :add)
                   (maybe-redirect request (str/replace-first uri #"/+$" "")))
                 (if (not= method :strip)
                   (maybe-redirect request (str uri "/"))))))]
     (fn
       ([request]
        (redirect-handler request))
       ([request respond _]
        (respond (redirect-handler request)))))))
