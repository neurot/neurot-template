(ns neurot-template.views
    (:require [re-frame.core :refer [subscribe dispatch]]))

;; main panel

(defn main-panel []
  (let [name      (subscribe [:name])
        my-number (subscribe [:my-number])
        avg       (subscribe [:average])]
    (fn []
      [:div
       [:div.em33
        [:h1 @name "."]
        [:input {:type      :number
                 :on-change #(dispatch [:number-changed (-> % .-target .-value)])
                 :value     @my-number}]
        [:p "Overall avg: " @avg]]])))
