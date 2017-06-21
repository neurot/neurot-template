(ns neurot-template.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reframe-utils.core :refer [reg-basic-sub multi-generation]]))

(multi-generation reg-basic-sub
                  :name
                  :asset
                  :talib
                  :connected?
                  :chart-data)
