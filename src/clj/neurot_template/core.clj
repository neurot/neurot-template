(ns neurot-template.core
  (:require [ring.adapter.jetty :as jetty]
            [bidi.ring :refer (make-handler)]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [ring.util.response :refer [response resource-response]]
            [ring.middleware.cors :refer [wrap-cors]]))


(defn index-handler [request]
  (resource-response "index.html" {:root "public"}))

(defn api-handler [request]
  (response {:name "api" :version "0.0.1"}))

(def handler
  (make-handler ["" {"/" index-handler
                      "/api" api-handler}]))

(def app (-> handler
             (wrap-json-body {:keywords? true :bigdecimals? true})
             (wrap-json-response)
             (wrap-cors :access-control-allow-origin [#"http://localhost"]
                        :access-control-allow-methods [:get :put :post :delete])))

(defn -main []
  (jetty/run-jetty app {:port 3000 :join? false}))
