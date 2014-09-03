(ns trumpet-server.domain.trumpeter-test
  (:require [midje.sweet :refer :all]
            [trumpet-server.domain.trumpeter :refer :all]))

(fact "distance-to calculates the correct distance"
      (def trumpeter1 (->Trumpeter 1, 55.583985, 12.957578))
      (def trumpeter2 (->Trumpeter 2, 55.584126, 12.957406))

      (int (distance-to trumpeter1 trumpeter2 :meters)) => 19)

(fact "distance-to uses meters as default distance unit"
      (def trumpeter1 (->Trumpeter 1, 55.583985, 12.957578))
      (def trumpeter2 (->Trumpeter 2, 55.584126, 12.957406))

      (int (distance-to trumpeter1 trumpeter2)) => 19)

(fact "distance-to can calculate distance in km"
      (def trumpeter1 (->Trumpeter 1, 55.583985, 12.957578))
      (def trumpeter2 (->Trumpeter 2, 55.584126, 12.957406))

      (distance-to trumpeter1 trumpeter2 :kilometers) => 0.019042546123347444)
