(ns neurot-template.asset
  (:require [re-frame.core :refer [subscribe dispatch]]
            [reagent.core :as reagent]
            [cljsjs.highstock]))

(defn chart-config [data]
  {:rangeSelector {:selected 1}
   ;; :chart         {:type     "candlestick"
   ;;                 :zoomType "x"}
   :title         {:text (-> data :asset :name)}
   :yAxis         [{:labels    {:align "right"
                                :x     -3}
                    :title     {:text "OHLC"}
                    :height    "60%"
                    :lineWidth 2}
                   {:labels    {:align "right"
                                :x     -3}
                    :title     {:text (-> data :talib :info :name)}
                    :top       "65%"
                    :height    "35%"
                    :offset    0
                    :lineWidth 2}]
   :tooltip       {:split true}

   :series [{:name "Asset"
             :type "candlestick"
             :color "red"
             :upColor "#00F72C"
             :data (-> data :asset :data)
             ;; :tooltip {:valueDecimals 4}
}
            {:name  "TA"
             :type  "column"
             :color "black"
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
     [:div.em33.mb3
      [:div.flex
       [:input.input {:style {:width "200px"
                              :margin-bottom "0px"}
                      :type        "text"
                      :placeholder "Asset"
                      :value       @(subscribe [:asset])
                      :on-change   #(dispatch [:asset-change (-> % .-target .-value)])}]
       [:input.input.ml1 {:style {:width "200px"
                              :margin-bottom "0px"}
                      :type        "text"
                      :placeholder "Technical Analyzer"
                      :value       @(subscribe [:talib])
                      :on-change   #(dispatch [:talib-change (-> % .-target .-value)])}]
       [:button.btn.btn-outline.black.ml2
        {:on-click #(dispatch [:assets/get {:asset @asset :talib @talib}])}
        "Load"]]]
     (if @data
       [:div.stock.m1
        [chart data]])]))
