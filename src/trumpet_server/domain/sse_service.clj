(ns trumpet-server.domain.sse-service
  (:require [clojure.core.async :refer [go >! chan close!]]
            [ninjudd.eventual.server :refer [json-events]]))

(def subscribers (atom {}))

(defn broadcast-message [trumpeter-id message]
  {:pre [trumpeter-id]}
  (println (str trumpeter-id " " message))
  (let [sse-stream (@subscribers trumpeter-id)]
    go ((>! sse-stream message))))

(defn subscribe [{trumpeter-id :id}]
  {:pre [trumpeter-id]}
  (let [sse-stream (chan)]
    (do (swap! subscribers assoc trumpeter-id sse-stream)
        (json-events sse-stream))))