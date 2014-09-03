(ns trumpet-server.domain.trumpeteer-distance-to-test
  (:require [midje.sweet :refer :all]
            [trumpet-server.domain.trumpeteer :refer :all]))

(fact "distance-to calculates the correct distance"
      (def trumpeteer1 (->Trumpeteer 1, 55.583985, 12.957578))
      (def trumpeteer2 (->Trumpeteer 2, 55.584126, 12.957406))

      (int (distance-to trumpeteer1 trumpeteer2 :meters)) => 19)

(fact "distance-to uses meters as default distance unit"
      (def trumpeteer1 (->Trumpeteer 1, 55.583985, 12.957578))
      (def trumpeteer2 (->Trumpeteer 2, 55.584126, 12.957406))

      (int (distance-to trumpeteer1 trumpeteer2)) => 19)

(fact "distance-to can calculate distance in km"
      (def trumpeteer1 (->Trumpeteer 1, 55.583985, 12.957578))
      (def trumpeteer2 (->Trumpeteer 2, 55.584126, 12.957406))

      (distance-to trumpeteer1 trumpeteer2 :kilometers) => 0.019042546123347444)
