(ns neurot-template.runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [neurot-template.core-test]))

(doo-tests 'neurot-template.core-test)
