(ns neurot-template.server
  (:use org.httpkit.server
        [compojure.core :only [GET POST defroutes routes]]
        [compojure.handler :only [api]]
        [ring.util.response :only [file-response response]]
        [pneumatic-tubes.core :only [receiver transmitter dispatch]]
        [pneumatic-tubes.httpkit :only [websocket-handler]]))
  ;; (:use [org.httpkit.server :only [run-server]])
  ;; (:require [neurot-template.core :refer [handle-request]]
  ;;           [compojure.core :only [GET POST defroutes routes]]
  ;;           [compojure.handler :only [api]]
  ;;           ;; [bidi.ring :refer (make-handler)]
  ;;           ;; [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
  ;;           ;; [ring.middleware.cors :refer [wrap-cors]]
  ;;           ;; [ring.middleware.reload :refer [wrap-reload]]
  ;;           [ring.util.response :refer [response file-response]]
  ;;           [pneumatic-tubes.core :only [receiver transmitter dispatch]]
  ;;           [pneumatic-tubes.httpkit :only [websocket-handler]]))

(def tx (transmitter))
(def dispatch-to (partial dispatch tx))

(def numbers (atom {}))

(defn- average [numbers]
  (double (/ (apply + numbers) (count numbers))))

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
  (GET "/" [] (file-response "index.html" {:root "resources/public"}))
  (GET "/ws" [] (websocket-handler rx)))

;; (defn api-handler [request]
;;   (response (handle-request request)))

;; (def handler
;;   (make-handler ["" {"/" (resource-response "index.html" {:root "public"})
;;                      ;; "/api" api-handler
;;                      "/ws" (websocket-handler rx)}]))

;; (def app-dev (-> #'handler
;;                  (wrap-reload)
;;                  ;; (wrap-json-body {:keywords? true :bigdecimals? true})
;;                  ;; (wrap-json-response)
;;                  ;; (wrap-cors :access-control-allow-origin [#"http://localhost"]
;;                  ;;            :access-control-allow-methods [:get :put :post :delete])
;;                  ))

;; (def app (-> handler
;;              ;; (wrap-json-body {:keywords? true :bigdecimals? true})
;;              ;; (wrap-json-response)
;;              ;; (wrap-cors :access-control-allow-origin [#"http://localhost"]
;;              ;;            :access-control-allow-methods [:get :put :post :delete])
;;              ))

(defn -main []
  (run-server app {:port 3000 :join? false}))
