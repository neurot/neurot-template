(ns neurot-template.views
  (:require [re-frame.core :refer [subscribe dispatch]]
            [neurot-template.utils :as util]))

;; main panel

(defn main-panel []
  (let [name      (subscribe [:name])
        my-number (subscribe [:my-number])
        avg       (subscribe [:average])]
    (fn []
      [:div
       [:div.em33.bgpink
        [:h1.bgb @name]
        [:p.bgw util/lorem]
        [:input {:type      :number
                 :on-change #(dispatch [:number-changed (-> % .-target .-value)])
                 :value     @my-number}]
        [:p "Overall avg: " @avg]]])))
