(ns neurot-template.subs
    (:require-macros [reagent.ratom :refer [reaction]])
    (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
 :name
 (fn [db]
   (:name db)))

(reg-sub
 :my-number
 (fn [db]
   (:my-number db)))

(reg-sub
 :average
 (fn [db]
   (:avg db)))

(reg-sub
 :status
 (fn [db]
   (:status db)))

(reg-sub
 :asset
 (fn [db]
   (:asset db)))
