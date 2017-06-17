(ns neurot-template.server
  (:use org.httpkit.server
        [neurot-template.core :refer [handle-request]]
        [neurot-template.assets :refer [get-asset]]
        [compojure.core :only [GET POST defroutes routes]]
        [compojure.route :refer [resources not-found]]
        [ring.util.response :only [file-response response]]
        [ring.middleware.defaults :refer [wrap-defaults]]
        [ring.middleware.reload :refer [wrap-reload]]
        [pneumatic-tubes.core :only [receiver transmitter dispatch]]
        [pneumatic-tubes.httpkit :only [websocket-handler]]
        [environ.core :refer [env]]))

(def tx (transmitter))
(def dispatch-to (partial dispatch tx))

(defn- update-data! [client-id data]
  (dispatch-to :all [:test/remote-data data]))

(def rx (receiver
         {:tube/on-create
          (fn [from _]
            (dispatch-to from [:welcome from])
            from)

          :tube/on-destroy
          (fn [from _]
            (dispatch-to from [:bye from])
            from)

          :assets/get
          (fn [from [_ data]]
            (dispatch-to from [:assets/set (get-asset (:asset data) (:talib data))])
            from)

          :test/test-event
          (fn [from [_ data]]
            (update-data! (:tube/id from) data)
            from)}))

(defroutes handler
  (GET "/" []  (file-response "index.html" {:root "resources/public"}))
  (GET "/ws" [] (websocket-handler rx))
  (GET "/api" [] (response (str {:name "api" :version "0.0.1"})))
  (resources "/")
  (not-found "Not Found"))

(def app (wrap-defaults handler {:params {:urlencoded true
                                          :keywordize true}}))

(def app-reload (wrap-reload #'app))

(defn -main [& args]
  (let [port (Integer/parseInt (or (env :port) "3000"))]
    (run-server app {:port port :join? false})))
