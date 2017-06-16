(ns neurot-template.talib
  (:import [com.tictactec.ta.lib.meta PriceHolder]
           [com.tictactec.ta.lib.meta CoreMetaData TaGrpService TaFuncService PriceHolder]
           [com.tictactec.ta.lib.meta.annotation OptInputParameterType InputFlags OptInputParameterType InputParameterType OutputParameterType]
           [com.tictactec.ta.lib MInteger]
           [java.lang Exception])
  ;; (:use [talib.clj-ta-lib yahoo]
  ;;       [talib.clj-ta-lib util])
  )


;; Price Holder

(defn- talib-vector [asset]
  " Returns a vector of index-aligned vectors for stock history of provided ticker
Dates maintain default string format while other values are converted to BigDecimal
[ [date] [open] [high] [low] [close] [volume] ] "
  (let [column-data (apply map vector (asset))]
    [(into [] (nth column-data 0))  ;Date
     (into []  (nth column-data 1)) ;Open
     (into []  (nth column-data 2)) ;High
     (into []  (nth column-data 3)) ;Low
     (into []  (nth column-data 4)) ;Close
     (into []  (nth column-data 4)) ;Volume
     ]))

(defn price-holder
    ([asset]
    (let [data (talib-vector asset)]
      (PriceHolder. (double-array (nth data 1));open
                    (double-array (nth data 2));high
                    (double-array (nth data 3));low
                    (double-array (nth data 4));close
                    (double-array (nth data 5));volume
                    (double-array (count (nth data 1));open interest
                                 )))))

;; (defn open [price-holder] (:o (bean price-holder)))
;; (defn high [price-holder] (:h (bean price-holder)))
;; (defn low [price-holder] (:l (bean price-holder)))
;; (defn close [price-holder] (:c (bean price-holder)))
;; (defn volume [price-holder] (:v (bean price-holder)))



;; ** Util **


;This proxy is implementing an inteface method with a return value of void.
;With no return value from the method we must use side-effects to expose
;the group name and function names within the group
(def group-service (proxy [TaGrpService] []
                     (execute [group function-set]
                       (println (str "Group: " group))
                       (doseq [ta-func (seq function-set)]
                         (println (str "   " (-> ta-func .getFuncInfo .name)))
      ;(print-function function)
))))

(defn print-groups []
  (CoreMetaData/forEachGrp group-service))

(defn print-function [function]
  (println (str "Name: " (.name (.getFuncInfo function))))

    ;PRINT INPUTS
  (doseq [i (range (-> function .getFuncInfo .nbInput))]
    (let [pinfo (.getInputParameterInfo function i)]
      (println "Input: " (-> pinfo .paramName) (-> pinfo .type .name))))

    ;PRINT OPTIONAL INPUTS
  (doseq [i (range (-> function .getFuncInfo .nbOptInput))]
    (let [pinfo (.getOptInputParameterInfo function i)]
      (print "Option:" (-> pinfo .paramName))
      (cond
        (= (-> pinfo .type) OptInputParameterType/TA_OptInput_RealRange)
        (let [rrange (.getOptInputRealRange function i)]
          (println
           (str " min=" (.min rrange)
                " max=" (.max rrange)
                " precision=" (.precision rrange)
                " default=" (.defaultValue rrange))))

        (= (-> pinfo .type) OptInputParameterType/TA_OptInput_RealList)
        (let [rlist (.getOptInputRealList function i)]
          (println
           (map #(str %2 "-" %1) (.string rlist) (.value rlist))
           "default=" (.defaultValue rlist)))

        (= (-> pinfo .type) OptInputParameterType/TA_OptInput_IntegerRange)
        (let [irange (.getOptInputIntegerRange function i)]
          (println
           (str " min=" (.min irange)
                " max=" (.max irange)
                " default=" (.defaultValue irange))))

        (= (-> pinfo .type) OptInputParameterType/TA_OptInput_IntegerList)
        (let [ilist (.getOptInputIntegerList function i)]
          (println
           (map #(str %2 "-" %1) (.string ilist) (.value ilist))
           "default=" (.defaultValue ilist))))))

    ;PRINT OUTPUTS
  (doseq [i (range (-> function .getFuncInfo .nbOutput))]
    (let [pinfo (.getOutputParameterInfo function i)]
      (println "Output:" (-> pinfo .paramName) (-> pinfo .type .name)))))

(def function-service (proxy [TaFuncService] []
                        (execute [function]
                          (print-function function))))

(defn print-functions []
  (CoreMetaData/forEachFunc function-service))


;; ** Core **

(defn- getFunc [func]
  (CoreMetaData/getInstance func))

(defn- addflags [price-holder flags]
  (let [bean (bean price-holder)]
    (PriceHolder. flags
                  (:o bean);open
                  (:h bean);high
                  (:l bean);low
                  (:c bean);close
                  (:v bean);volume
                  (:i bean);open interest
)))

(defn- getFunctionInputFlags [func]
  (let [flags (.flags (.getInputParameterInfo func 0))]
    (if (zero? flags)
      (bit-or InputFlags/TA_IN_PRICE_OPEN
              InputFlags/TA_IN_PRICE_HIGH
              InputFlags/TA_IN_PRICE_LOW
              InputFlags/TA_IN_PRICE_CLOSE
              InputFlags/TA_IN_PRICE_VOLUME
              InputFlags/TA_IN_PRICE_OPENINTEREST)
      flags)))

(defn get-out-array [func ticks]
  (let [output (atom [])]
    (doseq [i (range (-> func .getFuncInfo .nbOutput))]
      (let [pinfo (.getOutputParameterInfo func i)]
        (cond
          (= (-> pinfo .type) OutputParameterType/TA_Output_Real)
          (swap! output conj (double-array ticks))

          (= (-> pinfo .type) OutputParameterType/TA_Output_Integer)
          (swap! output conj (int-array ticks)))))

    @output))

(defn ta
  ([name]
   (print-function (getFunc name)))
  ([name input & options]
   (let [func          (getFunc name)
         funcInfo      (.getFuncInfo func)
         nbOptInputs   (-> funcInfo .nbOptInput)
         nbInputs      (-> funcInfo .nbInput)
         nbOutputs     (-> funcInfo .nbOutput)
         begIndex      (MInteger.)
         outNbElements (MInteger.)
         inputSize     (atom nil)
         output        (atom nil)
         outputCols    (atom [])]

      ;Set Options
     (if (= (count options) nbOptInputs)
       (doseq [i (range nbOptInputs)]
         (let [pinfo (.getOptInputParameterInfo func i)]
           (cond
             (or
              (= (-> pinfo .type) OptInputParameterType/TA_OptInput_RealRange)
              (= (-> pinfo .type) OptInputParameterType/TA_OptInput_RealList))
             (.setOptInputParamReal func i (str (nth options i)))

             (or
              (= (-> pinfo .type) OptInputParameterType/TA_OptInput_IntegerRange)
              (= (-> pinfo .type) OptInputParameterType/TA_OptInput_IntegerList))
             (.setOptInputParamInteger func i (nth options i))

             :else
             (throw (Exception. "InvalidArgument - Options")))))
       ((print-function func)
        (throw (Exception. "Invalid number of options"))))

      ;Set Inputs
     (if (= (count input) nbInputs)
       (doseq [i (range nbInputs)]
         (let [pinfo (.getInputParameterInfo func i)]
           (cond
             (= (-> pinfo .type) InputParameterType/TA_Input_Price)
             (.setInputParamPrice func i (addflags (nth input i) (getFunctionInputFlags func)))

             (= (-> pinfo .type) InputParameterType/TA_Input_Real)
             (.setInputParamReal func i (nth input i))

             (= (-> pinfo .type) InputParameterType/TA_Input_Integer)
             (.setInputParamInteger func i (nth input i)))))
       ((print-function func)
        (throw (Exception. "Invalid number of inputs"))))

      ; At this point we need the size or number of ticks of the inputs
     (let [pinfo (.getInputParameterInfo func 0)]
       (if (= (-> pinfo .type) InputParameterType/TA_Input_Price)
         (compare-and-set! inputSize nil (count (:c (bean (first input)))))
         (compare-and-set! inputSize nil (count (first input)))))

      ;Construct output arrays
     (compare-and-set! output nil (get-out-array func @inputSize))

      ;Set Output Paramters
     (doseq [i (range nbOutputs)]
       (let [pinfo (.getOutputParameterInfo func i)]
         (cond
           (= (-> pinfo .type) OutputParameterType/TA_Output_Real)
           (.setOutputParamReal func i (nth @output i))

           (= (-> pinfo .type) OutputParameterType/TA_Output_Integer)
           (.setOutputParamInteger func i (nth @output i)))

         (swap! outputCols conj (-> pinfo .paramName))))

     (.callFunc func 0 (- @inputSize 1) begIndex outNbElements)

     (with-meta
       @output
       {:begIndex   (.value begIndex)
        :nbElements (.value outNbElements)
        :lookback   (.getLookback func)
        :name       (.name (.getFuncInfo func))
        :options    options
        :columns    @outputCols}))))

;; (def DEMO-l (price-holder "test-l"))

;; (filter #(> 0 %) (seq (first (ta "cdl2crows" [DEMOo]))))

;; (ta "ADD" [(double-array [10 100 1000]) (double-array [1 2 3])])

;; (wcar* (car/get "asset/test"))
