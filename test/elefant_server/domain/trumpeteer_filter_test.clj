(ns elefant-server.domain.trumpeteer-filter-test
  (:require [midje.sweet :refer :all]
            [elefant-server.domain.trumpeteer :refer [->Trumpeteer]]))

(fact "filter-in-range returns all trumpeteers that are in range exlcuding self"
      ; Given
      (def trumpeteer1 (->Trumpeteer 1, 55.583985, 12.957578)) ; self
      (def trumpeteer2 (->Trumpeteer 2, 55.584126, 12.957406))
      (def trumpeteer3 (->Trumpeteer 3, 55.58453, 12.957406))
      (def trumpeteer4 (->Trumpeteer 4, 55.58533, 12.95864))
      (def trumpeteer5 (->Trumpeteer 5, 55.59772, 12.97519)) ; out of range
      (def trumpeteer6 (->Trumpeteer 6, 55.60519, 13.00334)) ; out of range

      (let [in-range (.filter-in-range trumpeteer1 [trumpeteer2 trumpeteer3 trumpeteer4 trumpeteer5 trumpeteer6])
            ids (map #(:id %) in-range)]
        ids) => (just (list 2 3 4) :in-any-order))
