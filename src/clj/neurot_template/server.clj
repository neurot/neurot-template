(ns neurot-template.server
  (:use org.httpkit.server
        [neurot-template.core :refer [handle-request]]
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

(def numbers (atom {}))

(defn- average [numbers]
  (double (if (empty? numbers)
            -1
            (/ (apply + numbers) (count numbers)))))

(defn- update-number! [client-id num]
  (let [nums (swap! numbers assoc client-id num)]
    (dispatch-to :all [:average-changed (average (vals nums))])))

(defn- remove-number! [client-id]
  (let [nums (swap! numbers dissoc client-id)]
    (dispatch-to :all [:average-changed (average (vals nums))])))

(def rx (receiver
         {:tube/on-create
          (fn [from _]
            (update-number! (:tube/id from) 0)
            from)

          :tube/on-destroy
          (fn [from _]
            (remove-number! (:tube/id from))
            from)

          :number-changed
          (fn [from [_ num]]
            (update-number! (:tube/id from) (read-string num))
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
