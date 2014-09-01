(ns trumpet-server.web-test
  (:require [midje.sweet :refer :all]
            [clj-http.client :as client]
            [trumpet-server.web :refer [start-server]]))

(def server (atom nil))

(defn find-first [func data]
  (some #(if (func %) %) data))

(defn find-href-in-response [response]
  (fn [rel]
    (->> response :_links (find-first #(= (:rel %) rel)) :href)))

(with-state-changes [(before :facts (reset! server (start-server)))
                     (after :facts (do (.stop @server)
                                       (reset! server nil)))]
                    (fact "Entry point returns the correct resources"
                          (def href-for-rel (->> (client/get "http://127.0.0.1:5000" {:query-params {"latitude" 22.2 "longtiude" 21.2} :as :json})
                                                 :body
                                                 find-href-in-response))
                          (href-for-rel "subscribe") => (just #"^http://127.0.0.1:5000/clients/\w.+/subscribe")
                          (href-for-rel "location") => (just #"^http://127.0.0.1:5000/clients/\w.+/location")
                          (href-for-rel "trumpet") => (just #"^http://127.0.0.1:5000/clients/\w.+/trumpet")
                          (href-for-rel "self") => "http://127.0.0.1:5000"))




