(ns trumpet-server.domain.repository)

(def last-trumpet-id (atom 0))

(defn- next-trumpet-id []
  (swap! last-trumpet-id inc))

(def trumpets (atom {}))

(defn new-trumpet! [{:keys [latitude longitude] :as initial}]
  {:pre [(number? latitude) (> latitude 0) (number? longitude) (> longitude 0)]}
  (let [trumpet-id (next-trumpet-id)
        trumpet (assoc initial :id trumpet-id)]
    (swap! trumpets assoc trumpet-id trumpet)
    trumpet))

(defn get-all-trumpets []
  (vals (deref trumpets)))

(defn get-trumpet [id]
  {:pre [(number? id)]}
  (@trumpets id))

(defn clear-trumpets! []
  (do
    (reset! trumpets {})
    (reset! last-trumpet-id 0)))
