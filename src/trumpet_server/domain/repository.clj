(ns trumpet-server.domain.repository
  (:require [trumpet-server.domain.trumpeter :refer [->Trumpeter]]))

(def last-trumpeter-id (atom 0))

(defn- next-trumpeter-id []
  (swap! last-trumpeter-id inc))

(def trumpeters (atom {}))

(defn new-trumpeter! [{:keys [latitude longitude]}]
  {:pre [(number? latitude) (> latitude 0) (number? longitude) (> longitude 0)]}
  (let [trumpeter-id (next-trumpeter-id)
        trumpeter (->Trumpeter trumpeter-id latitude longitude)]
    (swap! trumpeters assoc trumpeter-id trumpeter)
    trumpeter))

(defn get-all-trumpeters []
  (vals (deref trumpeters)))

(defn get-trumpeter [id]
  {:pre [(number? id)]}
  (@trumpeters id))

(defn clear-trumpeters! []
  (do
    (reset! trumpeters {})
    (reset! last-trumpeter-id 0)))
