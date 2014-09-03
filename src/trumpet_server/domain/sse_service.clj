(ns trumpet-server.domain.sse-service
  (:require [clojure.core.async :refer [go >! chan close!]]
            [ninjudd.eventual.server :refer [json-events]]))

(def subscribers (atom {}))

(defn broadcast-message [trumpeteer-id message]
  {:pre [trumpeteer-id]}
  (println (str trumpeteer-id " " message))
  (let [sse-stream (@subscribers trumpeteer-id)]
    (go (>! sse-stream message))))

(defn subscribe [{trumpeteer-id :id}]
  {:pre [trumpeteer-id]}
  (let [sse-stream (chan)]
    (do (swap! subscribers assoc trumpeteer-id sse-stream)
        (json-events sse-stream))))