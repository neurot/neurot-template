(ns neurot-template.server
  (:use [org.httpkit.server :only [run-server]])
  (:require [neurot-template.core :refer [handle-request]]
            [bidi.ring :refer (make-handler)]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.util.response :refer [response resource-response]]))

(defn api-handler [request]
  (response (handle-request request)))

(def handler
  (make-handler ["" {"/" (resource-response "index.html" {:root "public"})
                     "/api" api-handler}]))

(def app-dev (-> #'handler
                 (wrap-reload)
                 (wrap-json-body {:keywords? true :bigdecimals? true})
                 (wrap-json-response)
                 (wrap-cors :access-control-allow-origin [#"http://localhost"]
                            :access-control-allow-methods [:get :put :post :delete])))

(def app (-> handler
             (wrap-json-body {:keywords? true :bigdecimals? true})
             (wrap-json-response)
             (wrap-cors :access-control-allow-origin [#"http://localhost"]
                        :access-control-allow-methods [:get :put :post :delete])))

(defn -main []
  (run-server app {:port 3000 :join? false}))
