(ns trumpet-server.domain.trumpet-service
  (:require [clojure.core.async :refer [go >! chan close!]]
            [ninjudd.eventual.server :refer [json-events]]
            [trumpet-server.domain.repository :as repository]))

; implementera distance-to i trumpeter??
(defn in-range? [sender receiver]
  (< (distance-to sender receiver) 10))

(defn broadcast-trumpet! [trumpet trumpetee]
  (let [trumpeters repository/get-all-trumpeters]
    ; doseq
    (filter #(in-range? trumpetee %) trumpeters)
    ))

(defn- trumpet! [trumpet trumpeter]
  )

(defn subscribe [request]
  ;(let [events (chan)]
  ;  (go (loop [...]
  ;        (if ...
  ;          (>! events event)
  ;          (recur ...)))
  ;      (close! events))
  ;  (json-events events))
  )