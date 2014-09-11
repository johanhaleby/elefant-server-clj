(ns elefant-server.api.rest-test
  (:require [midje.sweet :refer :all]
            [clj-http.client :as client]
            [clojure.java.io :as io]
            [elefant-server.boot :refer [start-server]]
            [cheshire.core :as json]
            [elefant-server.domain.sse-service :as sse-service]
            [elefant-server.domain.repository :as repository]))

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
                    (fact "Entry point returns the correct links" :it
                          (def href-for-rel (->> (client/get "http://127.0.0.1:5000/api" {:query-params {"latitude" 22.2 "longitude" 21.2} :as :json})
                                                 :body
                                                 find-href-in-response))
                          (href-for-rel :subscribe) => (just #"^http://127.0.0.1:5000/api/trumpeteers/\d/subscribe")
                          (href-for-rel :location) => (just #"^http://127.0.0.1:5000/api/trumpeteers/\d/location")
                          (href-for-rel :trumpet) => (just #"^http://127.0.0.1:5000/api/trumpeteers/\d/trumpet")
                          (href-for-rel :trumpeteer) => (just #"^http://127.0.0.1:5000/api/trumpeteers/\d")
                          (href-for-rel :self) => "http://127.0.0.1:5000/api?latitude=22.2&longitude=21.2")

                    (fact "/location updates location of trumpeteer" :it
                          ; Given
                          (def response (->> (client/get "http://127.0.0.1:5000/api" {:query-params {"latitude" 22.2 "longitude" 21.2} :as :json}) :body))
                          ; When
                          (client/put (->> response :_links :location :href) {:form-params {"latitude" 23.2 "longitude" 25.2}})
                          ; Then
                          (repository/get-trumpeteer (:trumpeteerId response)) => {:id 1, :latitude 23.2, :longitude 25.2})

                    (fact "/location returns the number of trumpeteers in range" :it
                          ; Given
                          (client/get "http://127.0.0.1:5000/api" {:query-params {"latitude" 55.583985 "longitude" 12.957578}})
                          (client/get "http://127.0.0.1:5000/api" {:query-params {"latitude" 55.582111 "longitude" 12.957678}})
                          (def reg-response (->> (client/get "http://127.0.0.1:5000/api" {:query-params {"latitude" 23.2 "longitude" 12.406} :as :json}) :body))

                          ; When
                          (def location-response (:body (client/put (->> reg-response :_links :location :href) {:form-params {"latitude" 55.583111 "longitude" 12.957688} :as :json})))

                          ; Then
                          (:trumpeteersInRange location-response) => 2)

                    (fact "/subscribe returns status code 400 when trumpeteer wasn't found" :it
                          ; Create trumpeteers
                          (def response (client/get "http://127.0.0.1:5000/api/trumpeteers/2/subscribe" {:throw-exceptions false :as :json :coerce :always}))

                          (:status response) => 400
                          (->> response :body :errorMessage) => "Couldn't find trumpeteer with id 2")

                    (fact "/echo broadcast the trumpet to trumpetees with the same messageId" :it
                          ; Given

                          ; Create trumpeteers
                          (def trumpeteerResponse (->> (client/get "http://127.0.0.1:5000/api" {:query-params {"latitude" 55.583985 "longitude" 12.957578} :as :json}) :body))
                          (def trumpeteeResponse (->> (client/get "http://127.0.0.1:5000/api" {:query-params {"latitude" 55.584126 "longitude" 12.957406} :as :json}) :body))


                          ; Register trumpetee for subscription
                          (def subscription (future (let [inputstream (:body (client/get (->> trumpeteeResponse :_links :subscribe :href) {:as :stream}))
                                                          reader (io/reader inputstream)
                                                          event (read-until-emptyline reader)
                                                          trumpet (json/parse-string (re-find #"\{.*\}" event) true)
                                                          has-trumpet-event-type? (.contains (re-find #"event:.*data:" event) "trumpet")]
                                                      {:has-trumpet-event-type? has-trumpet-event-type? :trumpet trumpet})))

                          ; Race-condition because subscription will return immeditaley and we'll post the trumpet before the subscription is registered.
                          (Thread/sleep 500)

                          ; When
                          (client/post (str "http://127.0.0.1:5000/api/trumpeteers/" (->> trumpeteerResponse :trumpeteerId) "/echo") {:form-params {"message" "My trumpet" "messageId" "ABC123"}})

                          ; Then
                          (def event (deref subscription 3000 :timed-out))
                          (:has-trumpet-event-type? event) => true
                          (:trumpet event) => (just {:id "ABC123", :timestamp anything :message "My trumpet" :distanceFromSource anything :_links anything})
                          (->> event :trumpet :_links :echo :href) => "http://127.0.0.1:5000/api/trumpeteers/2/echo")

                    (fact "/trumpeteers/:trumpet-id returns all subscribers that are nearby to the trumpeteer" :it
                          ; Create trumpeteers
                          (def trumpeteerResponse (->> (client/get "http://127.0.0.1:5000/api" {:query-params {"latitude" 55.583985 "longitude" 12.957578} :as :json}) :body))
                          (def trumpeteeResponse1 (->> (client/get "http://127.0.0.1:5000/api" {:query-params {"latitude" 55.584126 "longitude" 12.957406} :as :json}) :body))
                          (def trumpeteeResponse2 (->> (client/get "http://127.0.0.1:5000/api" {:query-params {"latitude" 55.58453 "longitude" 12.957406} :as :json}) :body))
                          (def trumpeteeResponse3 (->> (client/get "http://127.0.0.1:5000/api" {:query-params {"latitude" 55.58533 "longitude" 12.95864} :as :json}) :body))

                          ; Subscribe
                          (def subscription0 (future (->> (client/get (->> trumpeteerResponse :_links :subscribe :href) {:as :stream}) :body)))
                          (def subscription1 (future (->> (client/get (->> trumpeteeResponse1 :_links :subscribe :href) {:as :stream}) :body)))
                          (def subscription3 (future (->> (client/get (->> trumpeteeResponse3 :_links :subscribe :href) {:as :stream}) :body)))

                          ; Wait for subscriptions to register
                          (map #(deref % 1000 :timed-out) [subscription0 subscription1 subscription3])

                          ; Race-condition because subscription will return immeditaley and we'll post the trumpet before the subscription is registered.
                          (Thread/sleep 500)

                          ; When
                          (def response (:body (client/get (->> trumpeteerResponse :_links :trumpeteer :href)  {:as :json})))

                          ; Then
                          (:trumpeteersInRange response) => (just [{:longitude 12.95864, :latitude 55.58533} {:longitude 12.957406, :latitude 55.584126}] :in-any-order)
                          (->> response :_links :self :href) => "http://127.0.0.1:5000/api/trumpeteers/1")


                    (fact "/trumpet broadcast the trumpet to trumpetees" :it
                          ; Given

                          ; Create trumpeteers
                          (def trumpeteerResponse (->> (client/get "http://127.0.0.1:5000/api" {:query-params {"latitude" 55.583985 "longitude" 12.957578} :as :json}) :body))
                          (def trumpeteeResponse (->> (client/get "http://127.0.0.1:5000/api" {:query-params {"latitude" 55.584126 "longitude" 12.957406} :as :json}) :body))


                          ; Register trumpetee for subscription
                          (def subscription (future (let [inputstream (:body (client/get (->> trumpeteeResponse :_links :subscribe :href) {:as :stream}))
                                                          reader (io/reader inputstream)
                                                          event (read-until-emptyline reader)
                                                          trumpet (json/parse-string (re-find #"\{.*\}" event) true)
                                                          has-trumpet-event-type? (.contains (re-find #"event:.*data:" event) "trumpet")]
                                                      {:has-trumpet-event-type? has-trumpet-event-type? :trumpet trumpet})))

                          ; Race-condition because subscription will return immeditaley and we'll post the trumpet before the subscription is registered.
                          (Thread/sleep 500)

                          ; When
                          (client/post (->> trumpeteerResponse :_links :trumpet :href) {:form-params {"message" "My trumpet"}})

                          ; Then
                          (def event (deref subscription 3000 :timed-out))
                          (:has-trumpet-event-type? event) => true
                          (:trumpet event) => (just {:id anything, :timestamp anything :message "My trumpet" :distanceFromSource anything :_links anything})
                          (->> event :trumpet :_links :echo :href) => "http://127.0.0.1:5000/api/trumpeteers/2/echo")

                    (fact "/trumpet returns number of subscribed trumpeteers within distance" :it
                          ; Create trumpeteers
                          (def trumpeteerResponse (->> (client/get "http://127.0.0.1:5000/api" {:query-params {"latitude" 55.583985 "longitude" 12.957578} :as :json}) :body))
                          (def trumpeteeResponse (->> (client/get "http://127.0.0.1:5000/api" {:query-params {"latitude" 55.584126 "longitude" 12.957406} :as :json}) :body))

                          (def subscription (future (->> (client/get (->> trumpeteeResponse :_links :subscribe :href) {:as :stream}) :body)))

                          (deref subscription 3000 :timed-out)
                          (->> (client/post (->> trumpeteerResponse :_links :trumpet :href) {:form-params {"message" "My trumpet"} :as :json}) :body :trumpeteersWithinDistance) => 1)

                    (fact "/trumpet returns doesn't return unsubscribed trumpeteers or self" :it
                          ; Create trumpeteers
                          (def trumpeteerResponse (->> (client/get "http://127.0.0.1:5000/api" {:query-params {"latitude" 55.583985 "longitude" 12.957578} :as :json}) :body))
                          (def trumpeteeResponse1 (->> (client/get "http://127.0.0.1:5000/api" {:query-params {"latitude" 55.584326 "longitude" 12.958406} :as :json}) :body))
                          (def trumpeteeResponse2 (->> (client/get "http://127.0.0.1:5000/api" {:query-params {"latitude" 55.584126 "longitude" 12.957406} :as :json}) :body))
                          (def trumpeteeResponse3 (->> (client/get "http://127.0.0.1:5000/api" {:query-params {"latitude" 55.584137 "longitude" 12.953406} :as :json}) :body))

                          (def subscription0 (future (->> (client/get (->> trumpeteerResponse :_links :subscribe :href) {:as :stream}) :body)))
                          (def subscription1 (future (->> (client/get (->> trumpeteeResponse1 :_links :subscribe :href) {:as :stream}) :body)))
                          (def subscription3 (future (->> (client/get (->> trumpeteeResponse3 :_links :subscribe :href) {:as :stream}) :body)))

                          ; Wait for subscriptions to register
                          (map #(deref % 3000 :timed-out) [subscription0 subscription1 subscription3])

                          ; Race-condition because subscription will return immeditaley and we'll post the trumpet before the subscription is registered.
                          (Thread/sleep 500)

                          (->> (client/post (->> trumpeteerResponse :_links :trumpet :href) {:form-params {"message" "My trumpet"} :as :json}) :body :trumpeteersWithinDistance) => (eval 3) (shutdown-agents)))
