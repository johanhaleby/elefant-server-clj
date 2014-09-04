(ns trumpet-server.api.rest-test
  (:require [midje.sweet :refer :all]
            [clj-http.client :as client]
            [clojure.java.io :as io]
            [trumpet-server.boot :refer [start-server]]
            [cheshire.core :as json]
            [trumpet-server.domain.sse-service :as sse-service]
            [trumpet-server.domain.repository :as repository]))

(def server (atom nil))

(defn- find-href-in-response [response]
  "Returns a function to which you can pass a rel that returns the href matching this rel"
  (fn [rel]
    (->> response :_links rel :href)))

(defn- read-until-emptyline [reader]
  (apply str (take-while #(seq %) (repeatedly #(.readLine reader)))))

(with-state-changes [(before :facts (reset! server (start-server)))
                     (after :facts (do (.stop @server)
                                       (reset! server nil)
                                       (repository/clear-trumpeteers!)
                                       (sse-service/clear-subscribers!)))]
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
                          (repository/get-trumpeteer (:trumpeteerId response)) => {:id 1, :latitude 23.2, :longitude 25.2})


                    (fact "/location updates location of trumpeteer"
                          ; Given
                          (client/get "http://127.0.0.1:5000" {:query-params {"latitude" 55.583985 "longitude" 12.957578}})
                          (client/get "http://127.0.0.1:5000" {:query-params {"latitude" 55.582111 "longitude" 12.957678}})
                          (def reg-response (->> (client/get "http://127.0.0.1:5000" {:query-params {"latitude" 23.2 "longitude" 12.406} :as :json}) :body))

                          ; When
                          (def location-response (:body (client/put (->> response :_links :location :href) {:form-params {"latitude" 55.583111 "longitude" 12.957688} :as :json})))

                          ; Then
                          (:trumpeteersInRange location-response) => 2)


                    (fact "/trumpet broadcast the trumpet to trumpetees"
                          ; Given

                          ; Create trumpeteers
                          (def trumpeteerResponse (->> (client/get "http://127.0.0.1:5000" {:query-params {"latitude" 55.583985 "longitude" 12.957578} :as :json}) :body))
                          (def trumpeteeResponse (->> (client/get "http://127.0.0.1:5000" {:query-params {"latitude" 55.584126 "longitude" 12.957406} :as :json}) :body))


                          ; Register trumpetee for subscription
                          (def subscription (future (let [inputstream (:body (client/get (->> trumpeteeResponse :_links :subscribe :href) {:as :stream}))
                                                          reader (io/reader inputstream)
                                                          event (read-until-emptyline reader)
                                                          trumpet (json/parse-string (re-find #"\{.*\}" event) true)
                                                          has-trumpet-event-type? (.contains (re-find #"event:.*data:" event) "trumpet")]
                                                      {:has-trumpet-event-type? has-trumpet-event-type? :trumpet trumpet})))

                          ; When
                          (client/post (->> trumpeteerResponse :_links :trumpet :href) {:form-params {"message" "My trumpet"}})

                          ; Then
                          (def event (deref subscription 3000 :timed-out))
                          (:has-trumpet-event-type? event) => true
                          (:trumpet event) => (just {:id anything, :timestamp anything :message "My trumpet" :distanceFromSource anything})))