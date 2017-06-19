(defproject neurot-template "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.562"]
                 [reagent "0.6.2"]
                 [re-frame "0.9.4"]
                 [re-frisk "0.4.5"]
                 [yogthos/config "0.8"]
                 [compojure "1.6.0"]
                 [ring "1.6.1"]
                 [ring/ring-defaults "0.3.0"]
                 [pneumatic-tubes "0.2.0"]
                 [environ "1.1.0"]
                 [klang "0.5.7"]
                 [cljsjs/highstock "5.0.12-0"]
                 [clj-time "0.13.0"]
                 [com.taoensso/timbre "4.10.0"]
                 [com.taoensso/carmine "2.16.0"]]


  :java-source-paths ["src/java"]

  :target-path "target/%s"

  :plugins [[lein-cljsbuild "1.1.4"]]

  :min-lein-version "2.5.3"

  :source-paths ["src/clj" "src/java"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"
                                    "test/js"]

  :figwheel {:css-dirs ["resources/public/css"]
             :ring-handler neurot-template.server/app-reload}

  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

  :profiles
  {:dev
   {:dependencies [[binaryage/devtools "0.9.4"]
                   [figwheel-sidecar "0.5.10"]
                   [com.cemerick/piggieback "0.2.2"]]

    :plugins [[lein-figwheel "0.5.10"]
              [lein-doo "0.1.7"]]}}

  :cljsbuild
  {:builds
   [{:id           "dev"
     :source-paths ["src/cljs"]
     :figwheel     {:on-jsload "neurot-template.core/mount-root"}
     :compiler     {:main                 neurot-template.core
                    :output-to            "resources/public/js/compiled/app.js"
                    :output-dir           "resources/public/js/compiled/out"
                    :asset-path           "js/compiled/out"
                    :source-map-timestamp true
                    :preloads             [devtools.preload]
                    :external-config      {:devtools/config {:features-to-install :all}}}}

    {:id           "min"
     :source-paths ["src/cljs"]
     :jar          true
     :compiler     {:main            neurot-template.core
                    :output-to       "resources/public/js/compiled/app.js"
                    :optimizations   :advanced
                    :closure-defines {goog.DEBUG false}
                    :pretty-print    false}}

    {:id           "test"
     :source-paths ["src/cljs" "test/cljs"]
     :compiler     {:main          neurot-template.runner
                    :output-to     "resources/public/js/compiled/test.js"
                    :output-dir    "resources/public/js/compiled/test/out"
                    :optimizations :none}}]}

  :main neurot-template.server

  :aot [clojure.tools.logging.impl
        neurot-template.server]

  :uberjar-name "neurot-template.jar"

  :prep-tasks [["cljsbuild" "once" "min"] "ancient" ;; "kibit"
               "javac" "compile"])
