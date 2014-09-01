(ns trumpet-server.repository)

(def last-trumpet-id (atom 0))

(defn next-trumpet-id []
  (swap! last-trumpet-id inc))

(def trumpets (atom {}))

(defn now []
  (System/currentTimeMillis))

(defn new-trumpet [initial]
  ; Add pre-condition to checck that lat and long are present
  (let [trumpet-id (next-trumpet-id)
        trumpet (assoc initial :last-referenced (atom (now)))]
    (swap! trumpets assoc trumpet-id trumpet)
    trumpet-id))

(defn get-trumpet [id]
  (let [trumpet (@trumpets id)]
    (reset! (:last-referenced trumpet) (now))
    trumpet))

