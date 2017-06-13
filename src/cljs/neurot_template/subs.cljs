(ns neurot-template.subs
    (:require-macros [reagent.ratom :refer [reaction]])
    (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
 :name
 (fn [db]
   (:name db)))

;; (reg-sub
;;  :status
;;  (fn [db]
;;    (:status db)))

(reg-sub
 :remote-test-data
 (fn [db]
   (:remote-test-data db)))

(reg-sub
 :local-test-data
 (fn [db]
   (:local-test-data db)))
