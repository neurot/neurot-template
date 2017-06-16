(ns neurot-template.db)

(def default-db
  {:name             "neurot-template"
   :connected?       false
   :local-test-data  "foo"
   :remote-test-data "foo"
   :asset            nil
   :chart-cnfg       {:rangeSelector {:selected 1}
                      :chart         {:type     "candlestick"
                                      :zoomType "x"}
                      :title         {:text "Stock Price"}
                      :series        nil;; [{:name    "AAPL"
                                     ;;   :data    nil
                                     ;;   :tooltip {:valueDecimals 4}}]
                      }})
