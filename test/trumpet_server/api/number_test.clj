(ns trumpet-server.api.number-test
  (:require [midje.sweet :refer :all]
            [trumpet-server.api.number :refer :all]))

(fact "Throws NPE if potential-number is nul"
      (to-number nil) => (throws NullPointerException "Number is required for parameter but no value was supplied.")
      (to-number nil "param-name") => (throws NullPointerException "Number is required for parameter param-name but no value was supplied."))

(fact "Throws IAE if potential-number is not a number"
      (to-number "gfd") => (throws IllegalArgumentException "Number is required for parameter but gfd was supplied.")
      (to-number "gfd" "param-name") => (throws IllegalArgumentException "Number is required for parameter param-name but gfd was supplied."))

(fact "Returns number if potential-number is a number"
      (to-number "2.3") => 2.3)



