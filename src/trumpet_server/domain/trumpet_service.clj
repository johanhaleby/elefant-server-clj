(ns trumpet-server.domain.trumpet-service
  (:require [clojure.core.async :refer [go >! chan close!]]
            [ninjudd.eventual.server :refer [edn-events]]))

(defn trumpet [trumpeters]

  )


;(defn sse-handler [request]
;  (let [events (chan)]
;    (go (loop [...]
;          (if ...
;            (>! events event)
;            (recur ...)))
;        (close! events))
;    (edn-events events)))


