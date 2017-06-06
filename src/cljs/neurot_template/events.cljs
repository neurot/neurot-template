(ns neurot-template.events
    (:require [re-frame.core :refer [reg-event-db dispatch after]]
              [neurot-template.db :refer [default-db]]
              [pneumatic-tubes.core :as tubes]))

(defn on-receive [event-v]
  (.log js/console "received from server:" (str event-v))
  (dispatch event-v))

(def tube (tubes/tube (str "ws://neurot.herokuapp.com/ws") on-receive))
(def send-to-server (after (fn [_ v] (tubes/dispatch tube v))))

(reg-event-db
 :initialize-db
 (fn [_ _]
   default-db))

(reg-event-db
 :number-changed
 send-to-server
 (fn [db [_ num]]
   (assoc db :my-number num)))

(reg-event-db
 :average-changed
 (fn [db [_ avg]]
   (assoc db :avg avg)))

(tubes/create! tube)
