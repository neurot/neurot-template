(ns neurot-template.asset
  (:require [re-frame.core :refer [subscribe dispatch]]
            [reagent.core :as reagent]
            [cljsjs.highstock]))

(defn chart-config [data]
  {:rangeSelector {:selected 1}
   ;; :chart         {:type     "candlestick"
   ;;                 :zoomType "x"}
   :title         {:text "Asset Demo"}
   :yAxis         [{:labels    {:align "right"
                                :x     -3}
                    :title     {:text "OHLC"}
                    :height    "60%"
                    :lineWidth 2}
                   {:labels    {:align "right"
                                :x     -3}
                    :title     {:text "XXX"}
                    :top       "65%"
                    :height    "35%"
                    :offset    0
                    :lineWidth 2}]
   :tooltip       {:split true}

   :series [{:name "Asset"
             :type "candlestick"
             :data (-> data :asset :data)
             ;; :tooltip {:valueDecimals 4}
}
            {:name  "TA"
             :type  "column"
             :color "red"
             :data  (-> data :talib :data)
             :yAxis 1
             :tooltip {:valueDecimals 2}}]})

(defn render-stock-fn [data]
  (fn [component]
    (.stockChart js/Highcharts (reagent/dom-node component) (clj->js (chart-config @data)))))

(defn chart [data]
  (reagent/create-class
   {:component-did-mount (render-stock-fn data)
    :component-did-update (render-stock-fn data)
    :reagent-render (fn [data]
                      @data
                      [:div.chart])}))

(defn ui []
  (let [data (subscribe [:chart-data])
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
     (if @data
       [:div.stock.m1
        [chart data]])]))
