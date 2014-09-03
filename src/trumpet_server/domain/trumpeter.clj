(ns trumpet-server.domain.trumpeter)

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
   :targets - All trumpeters in the system
   :broadcaster - Function that knows how to perform the actual broadcast to target trumpeters
   :max-distance-unit - Optional distance (in meters) or 200 will be used"
  (trumpet [this args]))

(defrecord Trumpeter [id latitude longitude]
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
  (trumpet [this {:keys [trumpet targets broadcaster max-distance-meters] :or {max-distance-meters 200}}]
    {:pre [trumpet targets broadcaster]}
    (let [targets-without-this (filter #(not= (:id %) (:id this)) targets)
          targets-with-distance (map #(assoc % :distance (distance-to this % :meters)) targets-without-this)
          targets-in-range (filter #(<= (:distance %) max-distance-meters) targets-with-distance)
          messages-to-broadcast (map #(into {} {:id (:id %) :message {:message trumpet :distanceFromSource (:distance %)}}) targets-in-range)]
      (doseq [message messages-to-broadcast]
        (broadcaster (:id message) (:message message))))))