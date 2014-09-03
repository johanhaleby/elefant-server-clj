(ns trumpet-server.api.rest-test
  (:require [midje.sweet :refer :all]
            [clj-http.client :as client]
            [trumpet-server.boot :refer [start-server]]
            [trumpet-server.domain.repository :as repository]))

(def server (atom nil))

(defn find-href-in-response [response]
  "Returns a function to which you can pass a rel that returns the href matching this rel"
  (fn [rel]
    (->> response :_links rel :href)))

(with-state-changes [(before :facts (reset! server (start-server)))
                     (after :facts (do (.stop @server)
                                       (reset! server nil)
                                       (repository/clear-trumpeteers!)))]
                    (fact "Entry point returns the correct links"
                          (def href-for-rel (->> (client/get "http://127.0.0.1:5000" {:query-params {"latitude" 22.2 "longitude" 21.2} :as :json})
                                                 :body
                                                 find-href-in-response))
                          (href-for-rel :subscribe) => (just #"^http://127.0.0.1:5000/trumpeteers/\d/subscribe")
                          (href-for-rel :location) => (just #"^http://127.0.0.1:5000/trumpeteers/\d/location")
                          (href-for-rel :trumpet) => (just #"^http://127.0.0.1:5000/trumpeteers/\d/trumpet")
                          (href-for-rel :self) => "http://127.0.0.1:5000?latitude=22.2&longitude=21.2")

                    (fact "/location updates location of trumpeteer"
                          ; Given
                          (def response (->> (client/get "http://127.0.0.1:5000" {:query-params {"latitude" 22.2 "longitude" 21.2} :as :json}) :body))
                          ; When
                          (client/put (->> response :_links :location :href) {:form-params {"latitude" 23.2 "longitude" 25.2}})
                          ; Then
                          (repository/get-trumpeteer (:id response)) => {:id 1, :latitude 23.2, :longitude 25.2})

                    (fact "/trumpet broadcast the trumpet to trumpetees"
                          ; Given
                          (def response (->> (client/get "http://127.0.0.1:5000" {:query-params {"latitude" 55.583985 "longitude" 12.957578} :as :json}) :body))
                          (client/get "http://127.0.0.1:5000" {:query-params {"latitude" 55.584126 "longitude" 12.957406}})

                          ; When
                          (client/post (->> response :_links :trumpet :href) {:form-params {"message" "My trumpet"}})

                          ; Then
                          (repository/get-trumpeteer (:id response)) => {:id 1, :latitude 23.2, :longitude 25.2}))




