(ns neurot-template.events
    (:require [re-frame.core :refer [subscribe reg-event-db dispatch after]]
              [neurot-template.db :refer [default-db]]
              [pneumatic-tubes.core :as tubes]
              [klang.core :refer-macros [info! warn! erro! crit! fata! trac!]]))

; pneumatic tubes

(defn on-receive [event-v]
  (info! "ws/received" ;; event-v
         )
  (dispatch event-v))

(def tube (tubes/tube (str "ws://localhost:3449/ws") on-receive))
(def send-to-server (after (fn [_ v] (tubes/dispatch tube v))))


; events

(reg-event-db
 :initialize-db
 (fn [_ _]
   (info! "loading default db")
   default-db))

(reg-event-db
 :asset-change
 (fn [db [_ new-asset]]
   (assoc db :asset new-asset)))

(reg-event-db
 :talib-change
 (fn [db [_ new-talib]]
   (assoc db :talib new-talib)))

(reg-event-db
 :bye
 (fn [db [_ data]]
   (assoc db :connected? false)))

(reg-event-db
 :welcome
 (fn [db [_ data]]
   (assoc db :connected? true)))

(reg-event-db
 :assets/set
 (fn [db [_ data]]
   ;; (assoc-in db [:asset] data)
   ;; (info! :asset/set)
   ;; (.log js/console data)
   ;; (assoc-in db [:chart-cnfg :series 0 :data] (second data))
   (reduce-kv assoc-in db {[:chart-cnfg :title :text] (-> data :asset :name)
                           [:chart-cnfg :series 0 :data] (-> data :asset :data)
                           [:chart-cnfg :yAxis 1 :title :text] (-> data :talib :info :name)
                           [:chart-cnfg :series 1 :data] (-> data :talib :data)})
   ))

(reg-event-db
 :test/remote-data
 (fn [db [_ data]]
   (assoc db :remote-test-data data)))


; remote-events

(reg-event-db
 :assets/get
 send-to-server
 (fn [db [event data]]
   (info! "ws/request:" [event data])
   db))

(reg-event-db
 :test/test-event
 send-to-server
 (fn [db [event data]]
   (info! "ws/request:" [event data])
   (assoc db :local-test-data data)))


; main

(tubes/create! tube)
