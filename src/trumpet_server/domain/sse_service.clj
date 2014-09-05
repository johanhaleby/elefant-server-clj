(ns trumpet-server.domain.sse-service
  (:require [clojure.core.async :refer [go >! chan close!]]
            [ninjudd.eventual.server :refer [json-events]]
            [clojure.tools.logging :as log]
            [trumpet-server.domain.time :as time]
            [schejulure.core :refer [schedule]]
            [useful.map :refer [remove-vals]]))

(def clients (atom {}))

(defn broadcast-message [trumpetee-id message]
  {:pre [trumpetee-id]}
  (log/info "Sending message with id" (:id message) "to trumpetee" trumpetee-id)
  (if-let [client (@clients trumpetee-id)]
    (do (reset! (:last-accessed client) (time/now))
        (go (>! (:sse-stream client) (with-meta message {:event-type "trumpet"}))))
    (log/warn "Failed to send message with id " (:id message) " because trumpetee " trumpetee-id " is not a subscriber.")))

(defn subscribe [{trumpeteer-id :id}]
  {:pre [trumpeteer-id]}
  (let [sse-stream (chan)
        client {:last-accessed (atom (time/now)) :sse-stream sse-stream}]
    (do (swap! clients assoc trumpeteer-id client)
        (json-events sse-stream))))

(defn clear-subscribers! []
  (do
    (reset! clients {})))

(defn- client-expiry-time []
  (- (time/now) (* 10 60 1000)))

(defn- stale? [client]
  (< @(:last-accessed client) (client-expiry-time)))

(defn- remove-stale-clients []
  (log/info "Removing stale clients")
  (let [stale-clients (filter #(stale? (val %)) @clients)]
    (doseq [stale-client stale-clients]
      (log/info "Closing SSE channel for trumpeteer with id " (first stale-client))
      (go (close! (:sse-stream (last stale-client))))))
  ; TODO Implement remove-vals instead of using lib
  (swap! clients #(remove-vals % stale?)))

#_(def stale-client-remover
  (schedule {:min (range 0 60 5)} remove-stale-clients))