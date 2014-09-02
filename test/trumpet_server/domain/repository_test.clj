(ns trumpet-server.domain.repository-test
  (:require [midje.sweet :refer :all]
            [trumpet-server.domain.repository :as repository]))

(with-state-changes [(before :facts (repository/clear-trumpets!))]
                    (fact "new-trumpet returns the id of the new trumpet"
                          (repository/new-trumpet! {:latitude 21 :longitude 22}) => {:id 1 :latitude 21 :longitude 22})

                    (fact "get-trumpet returns a trumpet with the specified id"
                          (let [trumpet-id (-> (repository/new-trumpet! {:latitude 21 :longitude 22}) :id)]
                            (repository/get-trumpet trumpet-id)
                            ) => {:id 1 :latitude 21 :longitude 22})

                    (fact "get-all-trumpets returns all defined trumpets"
                          (def trumpet1 (repository/new-trumpet! {:latitude 21 :longitude 22}))
                          (def trumpet2 (repository/new-trumpet! {:latitude 23 :longitude 24}))
                          (repository/get-all-trumpets) => (just (list trumpet1 trumpet2) :in-any-order))

                    (fact "get-all-trumpets returns empty list when no trumpets are defined"
                          (empty? (repository/get-all-trumpets)) => true))

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

(fact "get-trumpet throws IAE when id is not a number"
      (repository/get-trumpet "id") => (throws AssertionError))



