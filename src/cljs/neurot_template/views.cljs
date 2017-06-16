(ns neurot-template.views
  (:require [re-frame.core :refer [subscribe dispatch]]
            [reagent.core :as reagent]
            [neurot-template.utils :as util]
            [cljsjs.highstock]
            [goog.string :as gstring]))

(defn render-stock-fn [cnfg]
  (fn [component]
    (.stockChart js/Highcharts (reagent/dom-node component) (clj->js @cnfg))))

(defn stock-ui [cnfg]
  (reagent/create-class
   {:component-did-mount (render-stock-fn cnfg)
    :component-did-update (render-stock-fn cnfg)
    :reagent-render (fn [cnfg]
                      @cnfg
                      [:div])}))

;; main panel

(defn main-panel []
  (let [name      (subscribe [:name])
        connected?    (subscribe [:connected?])
        local-test-data (subscribe [:local-test-data])
        remote-test-data (subscribe [:remote-test-data])
        cnfg (subscribe [:chart-cnfg])]
    (fn []
      [:div
       ;; [:div.menu "menu"]
       ;; [:div.settings "settings"]
       [:div.status (if @connected? [:p.status (gstring/unescapeEntities "&#9901;")]  [:p.status.red (gstring/unescapeEntities "&#9902;")])]
       ;; [:div.info "info"]
       [:div.em33
        [:h1 @name]]
       [:div.em33
        [:p.bgw-basker util/lorem-s]]
       [:div.em33
        [:button.btn.btn-outline.black {:on-click #(dispatch [:assets/get "USDCAD/M1/2015"])} "Load Test Asset"]]
       [:div.m1
        [stock-ui cnfg]]
       ])))
