(ns neurot-template.views
    (:require [re-frame.core :as re-frame]))

;; main panel

(defn main-panel []
  (let [name (re-frame/subscribe [:name])]
    (fn []
      [:div "This is " @name "."])))
