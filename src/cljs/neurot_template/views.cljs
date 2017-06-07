(ns neurot-template.views
  (:require [re-frame.core :refer [subscribe dispatch]]
            [neurot-template.utils :as util]))

;; main panel

(defn main-panel []
  (let [name      (subscribe [:name])
        my-number (subscribe [:my-number])
        avg       (subscribe [:average])
        status    (subscribe [:status])]
    (fn []
      [:div
       ;; [:div.menu "menu"]
       ;; [:div.settings "settings"]
       [:div.status @status]
       ;; [:div.info "info"]
       [:div.em33
        [:h1 @name]]
       [:div.em33
        [:p.bgw util/lorem-tweet]]
       [:div.em33
        [:input {:type      :number
                 :on-change #(dispatch [:number-changed (-> % .-target .-value)])
                 :value     @my-number}]
        [:p "Overall avg: " @avg]]
       [:div
        [:img {:src "http://jmartinho.net/wp-content/uploads/2014/10/camara-neuronal-banner_web.jpg" :width "100%"}]]])))
