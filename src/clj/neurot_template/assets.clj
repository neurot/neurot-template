(ns neurot-template.assets
  (:require [clojure.string :refer [split join]]
            [clj-time.format :as f]
            [clj-time.coerce :as tc]
            [taoensso.carmine :as car :refer [wcar]]))

(def server1-conn {:pool {} :spec {:host "127.0.0.1" :port 6379}})
(defmacro wcar* [& body] `(car/wcar server1-conn ~@body))



(defn get-asset [asset]
  (wcar* (car/get asset)))

(def time-formatter (f/formatter "yyyyMMdd HHmmss"))

(defn format-raw-data [dta]
  (map (fn [dta] [(+ (tc/to-long (f/parse time-formatter (first dta)))
                     18000000) ; est -> utc compensation
                  (read-string (nth dta 1))
                  (read-string (nth dta 2))
                  (read-string (nth dta 3))
                  (read-string (nth dta 4))])
       (map #(split % #";") ;split each line by ;
            ;; (string/reverse)
            (split dta #"\n"))))

;; (def raw-dta (slurp "data/DAT_ASCII_EURUSD_M1_2016.csv"))

(defn asset [asset]
  (format-raw-data (slurp (str "data/" asset ".csv"))))

(def test-asset (format-raw-data (slurp "data/test.csv")))
;; (spit "form.csv" (format-data (slurp "data/DAT_ASCII_EURUSD_M1_2016.csv")))


;; (time (count (wcar* (car/get "EURUSD/M1/201705"))))
;; (time (count (format-raw-data (slurp "data/DAT_ASCII_EURUSD_M1_201705.csv"))))
