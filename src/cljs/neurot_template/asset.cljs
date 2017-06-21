(ns neurot-template.asset
  (:require [re-frame.core :refer [subscribe dispatch]]
            [reagent.core :as reagent]
            [cljsjs.highstock]))

(defn render-stock-fn [cnfg]
  (fn [component]
    (.stockChart js/Highcharts (reagent/dom-node component) (clj->js @cnfg))))

(defn chart [cnfg]
  (reagent/create-class
   {:component-did-mount (render-stock-fn cnfg)
    :component-did-update (render-stock-fn cnfg)
    :reagent-render (fn [cnfg]
                      @cnfg
                      [:div])}))

(defn ui []
  (let [cnfg  (subscribe [:chart-cnfg])
        asset (subscribe [:asset])
        talib (subscribe [:talib])]
    [:div
     [:div.em33
      [:input.input {:type        "text"
                     :placeholder "Asset"
                     :value       @(subscribe [:asset])
                     :on-change   #(dispatch [:asset-change (-> % .-target .-value)])}]
      [:input.input {:type        "text"
                     :placeholder "Technical Analyzer"
                     :value       @(subscribe [:talib])
                     :on-change   #(dispatch [:talib-change (-> % .-target .-value)])}]
      [:button.btn.btn-outline.black
       {:on-click #(dispatch [:assets/get {:asset @asset :talib @talib}])}
       "Load Asset"]]
     [:div.stock.m1
      [chart cnfg]]]))
