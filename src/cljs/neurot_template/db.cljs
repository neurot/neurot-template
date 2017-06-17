(ns neurot-template.db)

(def default-db
  {:name             "neurot-template"
   :connected?       false
   :asset            0
   :chart-cnfg       {:rangeSelector {:selected 1}
                      ;; :chart         {:type     "candlestick"
                      ;;                 :zoomType "x"}
                      :title         {:text "Asset Demo"}
                      :yAxis [{:labels {:align "right"
                                        :x -3}
                               :title {:text "OHLC"}
                               :height "60%"
                               :lineWidth 2}
                              {:labels {:align "right"
                                        :x -3}
                               :title {:text "XXX"}
                               :top "65%"
                               :height "35%"
                               :offset 0
                               :lineWidth 2}]
                      :tooltip {:split true}

                      :series        [{:name    "Asset"
                                       :type "candlestick"
                                       :data    nil
                                       ;; :tooltip {:valueDecimals 4}
                                       }
                                      {:name    "TA"
                                       :type "column"
                                       :color   "red"
                                       :data    nil
                                       :yAxis 1
                                       ;; :tooltip {:valueDecimals 4}
                                       }]}})
