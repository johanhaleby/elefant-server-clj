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

(defrecord Trumpeter [id latitude longitude]
  Location
  (distance-to [this other]
    (distance-to this other :meters))
  (distance-to [_ {other-latitude :latitude other-longitude :longitude} distance-unit]
    (let [theta (- longitude other-longitude)
          lhs (* (-> latitude deg2rad Math/sin) (-> other-latitude deg2rad Math/sin))
          rhs (* (-> latitude deg2rad Math/cos) (-> other-latitude deg2rad Math/cos) (-> theta deg2rad Math/cos))
          sum (+ lhs rhs)
          dist (-> sum Math/acos rad2deg (* 60 1.1515))]
      (convert-to-unit dist (or distance-unit :meters)))))