(ns neurot-template.core
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [ring.util.response :refer [response]]
            [ring.middleware.cors :refer [wrap-cors]]
            ))

(defn handler [request]
  (response {:foo (str request)}))

(def app (-> handler
             (wrap-json-body {:keywords? true :bigdecimals? true})
             (wrap-json-response)
             (wrap-cors :access-control-allow-origin [#"http://localhost"]
                        :access-control-allow-methods [:get :put :post :delete])))

(defn -main []
  (jetty/run-jetty app {:port 3000 :join? false}))
