(ns neurot-template.events
    (:require [re-frame.core :refer [reg-event-db dispatch after]]
              [neurot-template.db :refer [default-db]]
              [pneumatic-tubes.core :as tubes]
              [klang.core :refer-macros [info! warn! erro! crit! fata! trac!]]))

; pneumatic tubes

(defn on-receive [event-v]
  (info! "Received from server:" event-v)
  (dispatch event-v))

(def tube (tubes/tube (str "ws://localhost:3449/ws") on-receive))
(def send-to-server (after (fn [_ v] (tubes/dispatch tube v))))


; events

(reg-event-db
 :initialize-db
 (fn [_ _]
   default-db))

(reg-event-db
 :average-changed
 (fn [db [_ avg]]
   (assoc db :avg avg)))

(reg-event-db
 :asset
 (fn [db [_ asset]]
   (assoc db :asset asset)))

(reg-event-db
 :number-changed
 send-to-server
 (fn [db [event num]]
   (info! "Request to server:" [event num])
   (assoc db :my-number num)))

(reg-event-db
 :request-asset
 send-to-server
 (fn [db [a b]]
      (info! "Request to server:" [a b])
      (assoc db :asset "---")))

(tubes/create! tube)
