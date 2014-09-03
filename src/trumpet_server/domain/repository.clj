(ns trumpet-server.domain.repository
  (:require [trumpet-server.domain.trumpeteer :refer [->Trumpeteer]]))

(def last-trumpeteer-id (atom 0))

(defn- next-trumpeteer-id []
  (swap! last-trumpeteer-id inc))

(def trumpeteers (atom {}))

(defn new-trumpeteer! [{:keys [latitude longitude]}]
  {:pre [(number? latitude) (> latitude 0) (number? longitude) (> longitude 0)]}
  (let [trumpeteer-id (next-trumpeteer-id)
        trumpeteer (->Trumpeteer trumpeteer-id latitude longitude)]
    (swap! trumpeteers assoc trumpeteer-id trumpeteer)
    trumpeteer))

(defn update-trumpeteer! [trumpeteer]
  {:pre [trumpeteer (> (:id trumpeteer) 0)
         (number? (:latitude trumpeteer)) (> (:latitude trumpeteer) 0)
         (number? (:longitude trumpeteer)) (> (:longitude trumpeteer) 0)]}
  (let [trumpeteer-id (:id trumpeteer)
        trumpeteer (select-keys trumpeteer [:id :latitude :longitude])]
    (if (nil? (@trumpeteers trumpeteer-id))
      (throw (IllegalArgumentException. (str "Cannot update trumpeteer with id " trumpeteer-id " because it doesn't exists"))))
    (swap! trumpeteers assoc trumpeteer-id trumpeteer)
    trumpeteer))

(defn get-all-trumpeteers []
  (vals (deref trumpeteers)))

(defn get-trumpeteer [id]
  {:pre [(number? id)]}
  (@trumpeteers id))

(defn clear-trumpeteers! []
  (do
    (reset! trumpeteers {})
    (reset! last-trumpeteer-id 0)))
