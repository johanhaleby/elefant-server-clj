(ns trumpet-server.domain.trumpeteer)

(defn- deg2rad [deg]
  (* deg (/ Math/PI 180)))

(defn- rad2deg [rad]
  (* rad (/ 180 Math/PI)))

(defmulti convert-to-unit (fn [_ unit] unit))
(defmethod convert-to-unit :meters [dist _] (* dist 1.609344 1000))
(defmethod convert-to-unit :kilometers [dist _] (* dist 1.609344))
(defmethod convert-to-unit :nautical_miles [dist _] (* dist 0.8684))
(defmethod convert-to-unit :default [_ unit] (throw (IllegalArgumentException. (str "Unrecognized distance unit: " (name unit)))))


; Protocols doesn't support optional arguments so we need to make two methods
(defprotocol Location
  (distance-to [this other]
               [this other distance-unit]))

(defprotocol Trumpet
  "A Trumpet take a map (args) containing:

   :trumpet - The trumpet (message) to broadcast
   :trumpetees - All trumpetees in the system
   :broadcast-fn - Function that knows how to perform the actual broadcast to target trumpeteers
   :max-distance-meters - Optional distance (in meters) or 200 will be used"
  (trumpet! [this args]))

(def default-max-distance 200)

(defn- new-uuid [] (.toString (java.util.UUID/randomUUID)))
(defn- time-now [] (System/currentTimeMillis))

(defrecord Trumpeteer [id latitude longitude]
  Location Trumpet
  (distance-to [this other]
    (distance-to this other :meters))
  (distance-to [_ {other-latitude :latitude other-longitude :longitude} distance-unit]
    (let [theta (- longitude other-longitude)
          lhs (* (-> latitude deg2rad Math/sin) (-> other-latitude deg2rad Math/sin))
          rhs (* (-> latitude deg2rad Math/cos) (-> other-latitude deg2rad Math/cos) (-> theta deg2rad Math/cos))
          sum (+ lhs rhs)
          dist (-> sum Math/acos rad2deg (* 60 1.1515))]
      (convert-to-unit dist (or distance-unit :meters))))
  (trumpet! [this {:keys [trumpet trumpetees broadcast-fn max-distance-meters] :or {max-distance-meters default-max-distance}}]
    {:pre [trumpet trumpetees broadcast-fn]}
    (let [targets-without-this (filter #(not= (:id %) (:id this)) trumpetees)
          targets-with-distance (map #(assoc % :distance (distance-to this % :meters)) targets-without-this)
          targets-in-range (filter #(<= (:distance %) (or max-distance-meters default-max-distance)) targets-with-distance)
          messages-to-broadcast (map #(into {} {:id (:id %) :trumpet {:message trumpet :distanceFromSource (:distance %) :id (new-uuid) :timestamp (time-now)}}) targets-in-range)]
      (doseq [message messages-to-broadcast]
        (broadcast-fn (:id message) (:trumpet message))))))