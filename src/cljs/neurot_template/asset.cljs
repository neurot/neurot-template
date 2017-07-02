(ns neurot-template.asset
  (:require [re-frame.core :refer [subscribe dispatch]]
            [reagent.core :as reagent]
            [clojure.string :as string]
            [cljsjs.highstock]))

(defn chart-config [data]
  {:rangeSelector {:selected 1}
   ;; :chart         {:type     "candlestick"
   ;;                 :zoomType "x"}
   :title         {:text (-> data :asset :name)}
   :yAxis         [{:labels    {:align "right"
                                :x     -3}
                    :title     {:text "OHLC"}
                    :height    "80%"
                    :lineWidth 2}
                   {:labels    {:align "right"
                                :x     -3}
                    :title     {:text (-> data :talib :info :name)}
                    :top       "85%"
                    :height    "15%"
                    :offset    0
                    :lineWidth 2}]
   :tooltip       {:split true}

   :series [{:name (first (string/split (-> data :asset :name) #"/"))
             :type "candlestick"
             :color "red"
             :upColor "#00F72C"
             :data (-> data :asset :data)
             ;; :tooltip {:valueDecimals 4}
}
            {:name  (-> data :talib :info :name)
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
        talib (subscribe [:talib])
        loading? (subscribe [:loading?])]
    [:div
     [:div.em33.mb3
      [:div.flex
       [:input.input {:style {:width "200px"
                              :margin-bottom "0px"}
                      :type        "text"
                      :placeholder "Asset"
                      :value       @asset
                      :on-change   #(dispatch [:asset-change (-> % .-target .-value)])}]
       [:input.input.ml1 {:style {:width "200px"
                              :margin-bottom "0px"}
                      :type        "text"
                      :placeholder "Technical Analyzer"
                      :value       @talib
                      :on-change   #(dispatch [:talib-change (-> % .-target .-value)])}]
       [:button.btn.btn-outline.black.ml2
        {:on-click #(dispatch [:assets/get {:asset @asset :talib @talib}])}
        "Load"]]]
     (if @loading? [:div.loading "loading..."])
     (if (and @data (not @loading?))
       [:div.stock.m1
        [chart data]])]))
