(ns neurot-template.assets
  (:require [clojure.string :refer [split join]]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [clj-time.coerce :as tc]
            [taoensso.carmine :as car :refer [wcar]]
            [clojure.java.io :as io]
            [clojure.string :as str]))

; Redis

(def server1-conn {:pool {} :spec {:host "127.0.0.1" :port 6379}})
(defmacro wcar* [& body] `(car/wcar server1-conn ~@body))

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

(defn get-asset [asset]
  (wcar* (car/get asset)))

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


;; (import-raw-csv "data")
