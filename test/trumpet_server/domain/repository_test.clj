(ns trumpet-server.domain.repository-test
  (:require [midje.sweet :refer :all]
            [trumpet-server.domain.repository :as repository]))

(with-state-changes [(before :facts (repository/clear-trumpeters!))]
                    (fact "new-trumpeter returns the id of the new trumpeter"
                          (repository/new-trumpeter! {:latitude 21 :longitude 22}) => {:id 1 :latitude 21 :longitude 22})

                    (fact "get-trumpet returns a trumpeter with the specified id"
                          (let [trumpet-id (-> (repository/new-trumpeter! {:latitude 21 :longitude 22}) :id)]
                            (repository/get-trumpeter trumpet-id)
                            ) => {:id 1 :latitude 21 :longitude 22})

                    (fact "get-all-trumpeters returns all defined trumpeters"
                          (def trumpet1 (repository/new-trumpeter! {:latitude 21 :longitude 22}))
                          (def trumpet2 (repository/new-trumpeter! {:latitude 23 :longitude 24}))
                          (repository/get-all-trumpeters) => (just (list trumpet1 trumpet2) :in-any-order))

                    (fact "get-all-trumpets returns empty list when no trumpets are defined"
                          (empty? (repository/get-all-trumpeters)) => true)

                    (fact "update-trumpeter stores updated properties"
                          (def trumpeter (repository/new-trumpeter! {:latitude 10 :longitude 20}))
                          (repository/update-trumpeter! (assoc trumpeter :latitude 11 :longitude 21))

                          (repository/get-trumpeter (:id trumpeter)) => {:id (:id trumpeter) :latitude 11 :longitude 21})

                    (fact "update-trumpeter doesn't store unknown properties"
                          ; Given
                          (def trumpeter (repository/new-trumpeter! {:latitude 10 :longitude 20}))
                          ; When
                          (repository/update-trumpeter! (assoc trumpeter :latitude 11 :longitude 21 :unknown 34))
                          ; Then
                          (repository/get-trumpeter (:id trumpeter)) => {:id (:id trumpeter) :latitude 11 :longitude 21}) ; unknown should not be persisted
                    )

(fact "new-trumpeter throws IAE when latitude is missing"
      (repository/new-trumpeter! {:longitude 20}) => (throws AssertionError))

(fact "new-trumpeter throws IAE when longitude is missing"
      (repository/new-trumpeter! {:latitude 20}) => (throws AssertionError))

(fact "new-trumpeter throws IAE when latitude is not a number"
      (repository/new-trumpeter! {:latitude "21" :longitude 20}) => (throws AssertionError))

(fact "new-trumpeter throws IAE when longitude is not a number"
      (repository/new-trumpeter! {:latitude 21 :longitude "20"}) => (throws AssertionError))

(fact "new-trumpeter throws IAE when latitude is less than 0"
      (repository/new-trumpeter! {:latitude -1 :longitude 20}) => (throws AssertionError))

(fact "new-trumpeter throws IAE when longitude is less than 0"
      (repository/new-trumpeter! {:latitude 10 :longitude -20}) => (throws AssertionError))

(fact "get-trumpet throws IAE when id is not a number"
      (repository/get-trumpeter "id") => (throws AssertionError))

(fact "update-trumpeter throws IAE when trumpeter cannot be found"
      (repository/update-trumpeter! {:id 2 :latitude 11 :longitude 21}) => (throws IllegalArgumentException))


