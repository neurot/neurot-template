(ns neurot-template.server
  (:use org.httpkit.server
        [compojure.core :only [GET POST defroutes routes]]
        [ring.util.response :only [file-response response]]
        [pneumatic-tubes.core :only [receiver transmitter dispatch]]
        [pneumatic-tubes.httpkit :only [websocket-handler]])
  (:require [neurot-template.core :refer [handle-request]]))

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

(defroutes app
  (GET "/" []  (file-response "index.html" {:root "resources/public"}))
  (GET "/ws" [] (websocket-handler rx))
  (GET "/api" [] (response (str {:name "api" :version "0.0.1"}))))

(defn -main []
  (run-server app {:port 3000 :join? false}))
