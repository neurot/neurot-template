(ns neurot-template.assets
  (:require [clojure.string :refer [split join]]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [clj-time.coerce :as tc]
            [taoensso.carmine :as car :refer [wcar]]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [neurot-template.talib :refer [tx ta ta-info price-holder open high low close volume]]))

;; Redis

(def server1-conn {:pool {} :spec {:host "127.0.0.1" :port 6379}})
(defmacro wcar* [& body] `(car/wcar server1-conn ~@body))

(def time-formatter (f/formatter "yyyyMMdd HHmmss"))

;; Data Input

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

(defn filename-to-key [filename]
  (let [name-array (-> (second (re-matches #"(.+?)(\.[^.]*$|$)" filename))
                       (str/split #"_"))]
    (str (nth name-array 2) "/" (nth name-array 3) "/" (nth name-array 4))))

(defn get-csv-files [dir]
  (filter #(.contains % ".csv") (.list (io/file dir))))

(defn write-to-redis [key value]
  (println (str "Processing " key))
  (wcar* (car/set key value)))

(defn import-raw-csv [dir]
  (map #(write-to-redis (filename-to-key %)
                        (format-raw-data (slurp (str dir "/" %))))
       (get-csv-files dir)))

; mass import
(defn import-data-dir! [] (import-raw-csv "data"))
; (write-to-redis "test" (format-raw-data (slurp "data/test/test.csv")))


;; Data Output

(defn get-talib [asset-data ta-func]
  (let [talib-data (reverse (seq (first (tx ta-func asset-data))))]
    (mapv #(vector (first %1) %2) asset-data talib-data)))

;; (def test-dta (take 5 (wcar* (car/get "TEST/M1/2017"))))


(defn recalc-ohlc [dta]
  [(first (last dta))
   (second (first dta))
   (reduce (fn [val dta] (let [[_ _ h l] dta] (max val h))) 0.0 dta)
   (reduce (fn [val dta] (let [[_ _ h l] dta] (min val l))) (nth (first dta) 3) dta)
   (last (last dta))])

(defn resample-data
  ([input freq]
   (resample-data input freq []))
  ([input freq output]
   (if (empty? input)
     output
     (recur (drop freq input) freq (conj output (recalc-ohlc (take freq input)))))))

(defn get-data [request]
  (let [meta (str/split request #"/")
        asset (first meta)
        freq (read-string (re-find #"\d+" (second meta)))
        year (nth meta 2)
        raw-data (wcar* (car/get (str asset "/M1/" year)))]
    (if (= freq 1)
      raw-data
      (resample-data raw-data freq))))

;; (first (take 1 test-dta))

;; (resample-data test-dta 3 [])

;; (get-data "TEST/M1/2017")

(defn get-asset [asset ta-function]
  (let [asset-data (get-data (str/upper-case asset))]
    {:asset {:name (str/upper-case asset)
             :data asset-data}
     :talib {:info (ta-info ta-function)
             :data (get-talib asset-data ta-function)}}))
