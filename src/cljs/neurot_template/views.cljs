(ns neurot-template.views
  (:require [re-frame.core :refer [subscribe dispatch]]
            [reagent.core :as reagent]
            [neurot-template.utils :as util]
            [neurot-template.asset :as asset]
            [goog.string :as gstring]))

;; main panel

(defn main-panel []
  (let [name       (subscribe [:name])
        connected? (subscribe [:connected?])]
    (fn []
      [:div
       ;; [:div.menu "menu"]
       ;; [:div.settings "settings"]
       ;; [:div.info "info"]
       [:div.em33
        [:h1 @name]]
       [:div.em33
        [:p.bgw-basker util/lorem-tweet]]

       [asset/ui]

       [:div.status (if @connected? [:p.status (gstring/unescapeEntities "&#9901;")]  [:p.status.red (gstring/unescapeEntities "&#9902;")])]])))
