(ns neurot-template.views
  (:require [re-frame.core :refer [subscribe dispatch]]
            [reagent.core :as reagent]
            [neurot-template.utils :as util]
            [cljsjs.highstock]
            [goog.string :as gstring]))

(def stock-data [[1276128000000 35.79] [1276214400000 36.22] [1276473600000 36.33] [1276560000000 37.1] [1276646400000 38.18] [1276732800000 38.84] [1276819200000 39.15] [1277078400000 38.6] [1277164800000 39.12] [1277251200000 38.71] [1277337600000 38.43] [1277424000000 38.1]])

(def cnfg-atom (reagent/atom nil))

(def default-cnfg
  {:rangeSelector {:selected 1}
   :title {:text "Stock Price"}
   :series [{:name "AAPL"
             :data stock-data
             :tooltip {:valueDecimals 2}}]})

(reset! cnfg-atom default-cnfg)

(defn render-stock-fn [cnfg-atom]
  (fn [component]
    (.stockChart js/Highcharts (reagent/dom-node component) (clj->js @cnfg-atom))))

(defn stock-ui [cnfg-atom]
  (reagent/create-class
   {:component-did-mount (render-stock-fn cnfg-atom)
    :component-did-update (render-stock-fn cnfg-atom)
    :reagent-render (fn [cnfg-atom]
                      @cnfg-atom
                      [:div])}))

;; main panel

(defn main-panel []
  (let [name      (subscribe [:name])
        connected?    (subscribe [:connected?])
        local-test-data (subscribe [:local-test-data])
        remote-test-data (subscribe [:remote-test-data])]
    (fn []
      [:div
       ;; [:div.menu "menu"]
       ;; [:div.settings "settings"]
       [:div.status (if @connected? [:p.status (gstring/unescapeEntities "&#9901;")]  [:p.status.red (gstring/unescapeEntities "&#9902;")])]
       ;; [:div.info "info"]
       [:div.em33
        [:h1 @name]]
       ;; [:div.em33
       ;;  [:p.bgw util/lorem-m]]
       [:div.em33
        [:p.bgw-basker util/lorem-s]]
       ;; [:div.em33
       ;;  [:input {:type      :number
       ;;           :on-change #(dispatch [:number-changed (-> % .-target .-value)])
       ;;           :value     @my-number}]
       ;;  [:p "Overall avg: " @avg]]
       [:div.em33
        [:button.btn.btn-outline.black {:on-click #(dispatch [:test/test-event "bar"])} "Test"]
        [:div (str "local testdata: " @local-test-data)]
        [:div (str "remote testdata: " @remote-test-data)]]
       [:div.m1
        [:div.em33
         [:button.btn.btn-outline.black {:on-click #(dispatch [:assets/get "EURUSD"])} "Load TestAsset"]]
        [stock-ui cnfg-atom]]
       ])))
