(ns trumpet-server.rest-test
  (:require [midje.sweet :refer :all]
            [clj-http.client :as client]
            [trumpet-server.rest :refer [start-server]]))

(def server (atom nil))

(defn find-href-in-response [response]
  "Returns a function to which you can pass a rel that returns the href matching this rel"
  (fn [rel]
    (->> response :_links rel :href)))

(with-state-changes [(before :facts (reset! server (start-server)))
                     (after :facts (do (.stop @server)
                                       (reset! server nil)))]
                    (fact "Entry point returns the correct links"
                          (def href-for-rel (->> (client/get "http://127.0.0.1:5000" {:query-params {"latitude" 22.2 "longitude" 21.2} :as :json})
                                                 :body
                                                 find-href-in-response))
                          (href-for-rel :subscribe) => (just #"^http://127.0.0.1:5000/trumpeters/\d/subscribe")
                          (href-for-rel :location) => (just #"^http://127.0.0.1:5000/trumpeters/\d/location")
                          (href-for-rel :trumpet) => (just #"^http://127.0.0.1:5000/trumpeters/\d/trumpet")
                          (href-for-rel :self) => "http://127.0.0.1:5000?latitude=22.2&longitude=21.2"))




