(ns elefant-server.domain.sse-service
  (:require [clojure.core.async :refer [go >! chan close!]]
            [ninjudd.eventual.server :refer [json-events]]
            [clojure.tools.logging :as log]
            [elefant-server.domain.time :as time]
            [schejulure.core :refer [schedule]]
            [useful.map :refer [remove-vals]]))

(def subscribers (atom {}))

(defn broadcast-message! [trumpetee-id message]
  {:pre [trumpetee-id]}
  (log/info "Sending message \"" (:message message) "\" with id" (:id message) "to trumpetee" trumpetee-id)
  (if-let [subscriber (@subscribers trumpetee-id)]
    (do (reset! (:last-accessed subscriber) (time/now))
        (go (>! (:sse-stream subscriber) (with-meta message {:event-type "trumpet"}))))
    (log/warn "Failed to send message with id " (:id message) " because trumpetee " trumpetee-id " is not a subscriber.")))

(defn subscribe! [{trumpeteer-id :id}]
  {:pre [trumpeteer-id]}
  (log/info "New subscriber " trumpeteer-id)
  (let [sse-stream (chan)
        subscriber {:last-accessed (atom (time/now)) :sse-stream sse-stream}]
    (do (swap! subscribers assoc trumpeteer-id subscriber)
        (json-events sse-stream))))

(defn get-subscriber-ids []
  (keys @subscribers))

(defn clear-subscribers! []
  (do
    (reset! subscribers {})))

(defn- subscriber-expiry-time []
  (- (time/now) (* 10 60 1000)))

(defn- stale? [subscriber]
  (< @(:last-accessed subscriber) (subscriber-expiry-time)))

(defn- evict-stale-subscribers! []
  (log/info "Evicting stale SSE subscribers")
  (let [stale-subscribers (filter #(stale? (val %)) @subscribers)]
    (doseq [stale-subscriber stale-subscribers]
      (log/info "Closing SSE channel for trumpeteer with id " (first stale-subscriber))
      (go (close! (:sse-stream (last stale-subscriber))))))
  ; TODO Implement remove-vals instead of using lib
  (swap! subscribers #(remove-vals % stale?)))

(def stale-subscribers-evictor
  (schedule {:min (range 0 60 5)} evict-stale-subscribers!))