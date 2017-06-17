(ns neurot-template.subs
    (:require-macros [reagent.ratom :refer [reaction]])
    (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
 :name
 (fn [db]
   (:name db)))

(reg-sub
 :asset
 (fn [db]
   (:asset db)))

(reg-sub
 :talib
 (fn [db]
   (:talib db)))

(reg-sub
 :connected?
 (fn [db]
   (:connected? db)))

(reg-sub
 :chart-cnfg
 (fn [db]
   (:chart-cnfg db)))

(reg-sub
 :remote-test-data
 (fn [db]
   (:remote-test-data db)))

(reg-sub
 :local-test-data
 (fn [db]
   (:local-test-data db)))
