(ns neurot-template.core
    (:require [reagent.core :as reagent]
              [re-frame.core :as re-frame]
              [re-frisk.core :refer [enable-re-frisk!]]
              [neurot-template.events]
              [neurot-template.subs]
              [neurot-template.views :as views]
              [neurot-template.config :as config]))

(defn dev-setup []
  (when config/debug?
    (enable-console-print!)
    (enable-re-frisk!)
    (println "app is running in dev-mode")))



(defn mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn ^:export init []
  (re-frame/dispatch-sync [:initialize-db])
  (dev-setup)
  (mount-root))
