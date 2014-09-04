(ns trumpet-server.domain.sse-service
  (:require [clojure.core.async :refer [go >! chan close!]]
            [ninjudd.eventual.server :refer [json-events]]))

(def subscribers (atom {}))

(defn broadcast-message [trumpetee-id message]
  {:pre [trumpetee-id]}
  (println "Sending message with id " (:id message) " to trumpetee " trumpetee-id)
  (if-let [sse-stream (@subscribers trumpetee-id)]
    (go (>! sse-stream (with-meta message {:event-type "trumpet"})))
    ; TODO Use loggging library
    (println (str "Failed to send message with id " (:id message) " because trumpetee " trumpetee-id " is not a subscriber."))))

(defn subscribe [{trumpeteer-id :id}]
  {:pre [trumpeteer-id]}
  (let [sse-stream (chan)]
    (do (swap! subscribers assoc trumpeteer-id sse-stream)
        (json-events sse-stream))))

(defn clear-subscribers! []
  (do
    (reset! subscribers {})))