(ns neurot-template.assets
  (:require [clojure.string :refer [split join]]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [clj-time.coerce :as tc]
            [taoensso.carmine :as car :refer [wcar]]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [neurot-template.talib :refer [ta price-holder open high low close volume]]))

; Redis

(def server1-conn {:pool {} :spec {:host "127.0.0.1" :port 6379}})
(defmacro wcar* [& body] `(car/wcar server1-conn ~@body))

(def time-formatter (f/formatter "yyyyMMdd HHmmss"))

; Data Creation

(defn format-raw-data [dta]
  (mapv (fn [dta] [(+ (tc/to-long (f/parse time-formatter (first dta)))
                     18000000) ; est -> utc compensation
                  (read-string (nth dta 1))
                  (read-string (nth dta 2))
                  (read-string (nth dta 3))
                  (read-string (nth dta 4))])
       (map #(split % #";") ;split each line by ;
            ;; (reverse)
            (split dta #"\n"))))


(defn get-talib [asset-data]
  (let [talib-data (reverse (seq (first (ta "wma" [(close (price-holder asset-data))] 30))))]
    (mapv #(vector (first %1) %2) asset-data talib-data)))


(defn get-asset [asset]
  (let [asset-data (wcar* (car/get asset))]
    [asset-data (get-talib asset-data)]
    ))

(defn filename-to-key [filename]
  (let [name-array (-> (second (re-matches #"(.+?)(\.[^.]*$|$)" filename))
                       (str/split #"_"))]
    (str (nth name-array 2) "/" (nth name-array 3) "/" (nth name-array 4))))

(defn get-csv-files [dir]
  (filter #(.contains % ".csv") (.list (io/file dir))))

(defn write-to-redis [key value]
  (wcar* (car/set key value)))

(defn import-raw-csv [dir]
  (map #(write-to-redis (filename-to-key %)
                        (format-raw-data (slurp (str dir "/" %))))
       (get-csv-files dir)))


;; (write-to-redis "test" (format-raw-data (slurp "data/test/test.csv")))

;; mass import
;; (import-raw-csv "data")



;; (def DEMO-data (wcar* (car/get "test")))

;; (def DEMO (price-holder (wcar* (car/get "test"))))

;; (filter #(> 0 %) (seq (first (ta "rsi" [(close DEMO)] 20))))

;; (def DEMO-l (price-holder "test-l"))

;; (filter #(> 0 %) (seq (first (ta "cdlharami" [DEMO-l]))))

;; (ta "ADD" [(double-array [10 100 1000]) (double-array [1 2 3])])

;; (wcar* (car/get "asset/test"))
