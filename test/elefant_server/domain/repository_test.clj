(ns elefant-server.domain.repository-test
  (:require [midje.sweet :refer :all]
            [elefant-server.domain.repository :as repository]))

(with-state-changes [(before :facts (repository/clear-trumpeteers!))]
                    (fact "new-trumpeteer returns the id of the new trumpeteer"
                          (repository/new-trumpeteer! {:latitude 21 :longitude 22}) => {:id 1 :latitude 21 :longitude 22})

                    (fact "get-trumpet returns a trumpeteer with the specified id"
                          (let [trumpet-id (-> (repository/new-trumpeteer! {:latitude 21 :longitude 22}) :id)]
                            (repository/get-trumpeteer trumpet-id)
                            ) => {:id 1 :latitude 21 :longitude 22})

                    (fact "get-all-trumpeteers returns all defined trumpeteers"
                          (def trumpet1 (repository/new-trumpeteer! {:latitude 21 :longitude 22}))
                          (def trumpet2 (repository/new-trumpeteer! {:latitude 23 :longitude 24}))
                          (repository/get-all-trumpeteers) => (just (list trumpet1 trumpet2) :in-any-order))

                    (fact "get-all-trumpets returns empty list when no trumpets are defined"
                          (empty? (repository/get-all-trumpeteers)) => true)

                    (fact "update-trumpeteer stores updated properties"
                          (def trumpeteer (repository/new-trumpeteer! {:latitude 10 :longitude 20}))
                          (repository/update-trumpeteer! (assoc trumpeteer :latitude 11 :longitude 21))

                          (repository/get-trumpeteer (:id trumpeteer)) => {:id (:id trumpeteer) :latitude 11 :longitude 21})

                    (fact "update-trumpeteer doesn't store unknown properties"
                          ; Given
                          (def trumpeteer (repository/new-trumpeteer! {:latitude 10 :longitude 20}))
                          ; When
                          (repository/update-trumpeteer! (assoc trumpeteer :latitude 11 :longitude 21 :unknown 34))
                          ; Then
                          (repository/get-trumpeteer (:id trumpeteer)) => {:id (:id trumpeteer) :latitude 11 :longitude 21}) ; unknown should not be persisted
                    )

(fact "new-trumpeteer throws IAE when latitude is missing"
      (repository/new-trumpeteer! {:longitude 20}) => (throws AssertionError))

(fact "new-trumpeteer throws IAE when longitude is missing"
      (repository/new-trumpeteer! {:latitude 20}) => (throws AssertionError))

(fact "new-trumpeteer throws IAE when latitude is not a number"
      (repository/new-trumpeteer! {:latitude "21" :longitude 20}) => (throws AssertionError))

(fact "new-trumpeteer throws IAE when longitude is not a number"
      (repository/new-trumpeteer! {:latitude 21 :longitude "20"}) => (throws AssertionError))

(fact "new-trumpeteer throws IAE when latitude is less than 0"
      (repository/new-trumpeteer! {:latitude -1 :longitude 20}) => (throws AssertionError))

(fact "new-trumpeteer throws IAE when longitude is less than 0"
      (repository/new-trumpeteer! {:latitude 10 :longitude -20}) => (throws AssertionError))

(fact "get-trumpet throws IAE when id is not a number"
      (repository/get-trumpeteer "id") => (throws AssertionError))

(fact "update-trumpeteer throws IAE when trumpeteer cannot be found"
      (repository/update-trumpeteer! {:id 2 :latitude 11 :longitude 21}) => (throws IllegalArgumentException))


