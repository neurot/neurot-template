(ns neurot-template.db)

(def default-db
  {:name       "neurot-template"
   :connected? false
   :error      nil
   :asset      ""
   :talib      ""
   :loading? false
   :chart-data nil})
