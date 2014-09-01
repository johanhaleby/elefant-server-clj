(ns trumpet-server.repository-test
  (:require [midje.sweet :refer :all]
            [trumpet-server.repository :as repository]))

(with-state-changes [(before :facts (repository/clear-trumpets!))]
                    (fact "new-trumpet returns the id of the new trumpet"
                          (repository/new-trumpet! {:latitude 21 :longitude 22}) => 1)

                    (fact "get-trumpet returns a trumpet with the specified id"
                          (let [trumpet-id (repository/new-trumpet! {:latitude 21 :longitude 22})]
                            (repository/get-trumpet trumpet-id)
                            ) => {:id 1 :latitude 21 :longitude 22}))

(fact "new-trumpet throws IAE when latitude is missing"
      (repository/new-trumpet! {:longitude 20}) => (throws AssertionError))

(fact "new-trumpet throws IAE when longitude is missing"
      (repository/new-trumpet! {:latitude 20}) => (throws AssertionError))

(fact "new-trumpet throws IAE when latitude is not a number"
      (repository/new-trumpet! {:latitude "21" :longitude 20}) => (throws AssertionError))

(fact "new-trumpet throws IAE when longitude is not a number"
      (repository/new-trumpet! {:latitude 21 :longitude "20"}) => (throws AssertionError))

(fact "new-trumpet throws IAE when latitude is less than 0"
      (repository/new-trumpet! {:latitude -1 :longitude 20}) => (throws AssertionError))

(fact "new-trumpet throws IAE when longitude is less than 0"
      (repository/new-trumpet! {:latitude 10 :longitude -20}) => (throws AssertionError))



