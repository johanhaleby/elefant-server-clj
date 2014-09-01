(ns trumpet-server.core-test
  (:require [midje.sweet :refer :all]
            [trumpet-server.core :refer :all]))

;(fact "Returns map of distancess to predefined colors"
;      (color-distance-to 17 6 143) => {:beige 339, :black 144, :blue 113, :brown 183, :green 288, :grey 166, :indigo 60, :orange 320, :pink 308, :purple 112, :red 278, :turquoise 232, :yellow 373, :white 362}
;      )
;
;(fact "First color in map is the one closest to the entered RGB"
;      (first  (color-distance-to 17 6 143)) => [:indigo 60]
;      )
;
;
;(fact "Throws AssertionError when r is less than 0"
;      (color-distance-to -17 6 143) => (throws AssertionError)
;      )
;
;(fact "Throws AssertionError when g is less than 0"
;      (color-distance-to 17 -6 143) => (throws AssertionError)
;      )
;
;(fact "Throws AssertionError when b is less than 0"
;      (color-distance-to 17 6 -143) => (throws AssertionError)
;      )