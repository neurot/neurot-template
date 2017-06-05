(ns neurot-template.server
  (:use [org.httpkit.server :only [run-server]])
  (:require [bidi.ring :refer (make-handler)]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [ring.util.response :refer [response resource-response]]
            [ring.middleware.cors :refer [wrap-cors]]))

(defn api-handler [request]
  (response {:name "api" :version "0.0.1"}))

(def handler
  (make-handler ["" {"/" (resource-response "index.html" {:root "public"})
                     "/api" api-handler}]))

(def app (-> handler
             (wrap-json-body {:keywords? true :bigdecimals? true})
             (wrap-json-response)
             (wrap-cors :access-control-allow-origin [#"http://localhost"]
                        :access-control-allow-methods [:get :put :post :delete])))

(defn -main []
  (run-server app {:port 3000 :join? false}))
