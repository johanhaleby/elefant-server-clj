(ns trumpet-server.repository)

(def last-trumpet-id (atom 0))

(defn- next-trumpet-id []
  (swap! last-trumpet-id inc))

(def trumpets (atom {}))

(defn- now []
  (System/currentTimeMillis))

(defn new-trumpet! [{:keys [latitude longitude] :as initial}]
  {:pre [(number? latitude) (> latitude 0) (number? longitude) (> longitude 0)]}
  (let [trumpet-id (next-trumpet-id)
        trumpet (assoc initial :id trumpet-id)]
    (swap! trumpets assoc trumpet-id trumpet)
    trumpet-id))

(defn get-trumpet [id]
  (@trumpets id))

(defn clear-trumpets! []
  (do
    (reset! trumpets {})
    (reset! last-trumpet-id 0)))
