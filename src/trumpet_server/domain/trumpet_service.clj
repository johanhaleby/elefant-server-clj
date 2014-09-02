(ns trumpet-server.domain.trumpet-service
  (:require [clojure.core.async :refer [go >! chan close!]]
            [ninjudd.eventual.server :refer [edn-events]]
            [trumpet-server.domain.repository :as repository]
            [trumpet-server.domain.repository :as repository]))

; implementera distance-to i trumpeter??
(defn in-range? [trumpetee trumpeter]
  ())

(defn broadcast-trumpet! [trumpet trumpetee]
  (let [trumpeters repository/get-all-trumpets]
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
  ;  (edn-events events))
  )


