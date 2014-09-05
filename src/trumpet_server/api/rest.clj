(ns trumpet-server.api.rest
  (:require [trumpet-server.domain.repository :as trumpeteer-repository]
            [trumpet-server.api.number :refer [to-number]]
            [trumpet-server.domain.sse-service :as sse]
            [compojure.core :refer [defroutes GET PUT POST DELETE ANY context]]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [clojure.java.io :as io]
            [cheshire.core :as json]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [clojure.tools.logging :as log]))

(def content-type-hal "application/hal+json; charset=utf-8")

(defn get-base-uri [request]
  "Generate host from a ring request. For example 'http://localhost:5000'."
  (let [scheme (name (:scheme request))
        context (:context request)
        hostname (get (:headers request) "host")]
    (str scheme "://" hostname context)))

(defn render-entry-point [hostname {:keys [id latitude longitude]}]
  (let [data (-> {:_links {}}
                 (update-in [:_links] assoc :self {:href (str hostname "?latitude=" latitude "&longitude=" longitude)})
                 (update-in [:_links] assoc :subscribe {:href (str hostname "/trumpeteers/" id "/subscribe")})
                 (update-in [:_links] assoc :location {:href (str hostname "/trumpeteers/" id "/location")})
                 (update-in [:_links] assoc :trumpet {:href (str hostname "/trumpeteers/" id "/trumpet")})
                 (assoc :trumpeteerId id))]
    data))

(defn json-response [data & [status]]
  {:status  (or status 200)
   :headers {"Content-Type" content-type-hal}
   :body    (json/generate-string data)})

(defroutes app
           (context "/api" []
                    (GET "/" [latitude longitude :as request]
                         (let [lat (to-number latitude "latitude")
                               long (to-number longitude "longitude")
                               host (get-base-uri request)
                               trumpeteer (trumpeteer-repository/new-trumpeteer! {:latitude lat :longitude long})]
                           (log/info "Registering trumpeteer")
                           (json-response (render-entry-point host trumpeteer))))
                    (GET ["/trumpeteers/:trumpet-id/subscribe" :trumpet-id #"[0-9]+"] [trumpet-id :as request] ; trumpet-id must be an int otherwise route won't match
                         (let [trumpet-id (to-number trumpet-id)
                               trumpeteer (trumpeteer-repository/get-trumpeteer trumpet-id)]
                           (sse/subscribe trumpeteer)))
                    (PUT ["/trumpeteers/:trumpet-id/location" :trumpet-id #"[0-9]+"] [trumpet-id latitude longitude :as request]
                         (let [trumpet-id (to-number trumpet-id "trumpet-id")
                               latitude (to-number latitude "latitude")
                               longitude (to-number longitude "longitude")
                               trumpeteer (trumpeteer-repository/get-trumpeteer trumpet-id)
                               updated-trumpeteer (assoc trumpeteer :latitude latitude :longitude longitude)
                               trumpetees (trumpeteer-repository/get-all-trumpeteers)
                               number-of-trumpeteers-in-range (count (.filter-in-range updated-trumpeteer trumpetees))]
                           (trumpeteer-repository/update-trumpeteer! updated-trumpeteer)
                           (json-response {:trumpeteersInRange number-of-trumpeteers-in-range})))
                    (POST ["/trumpeteers/:trumpet-id/trumpet" :trumpet-id #"[0-9]+"] [trumpet-id message distance :as request]
                          (let [trumpet-id (to-number trumpet-id "trumpet-id")
                                distance (if (nil? distance) nil (to-number distance "distance"))
                                trumpeteer (trumpeteer-repository/get-trumpeteer trumpet-id)
                                trumpetees (trumpeteer-repository/get-all-trumpeteers)
                                subscriber-ids (sse/get-subscriber-ids)
                                subscribing-trumpetees (filter #(some #{(:id %)} subscriber-ids) trumpetees)]
                            (log/info "Trumpet" message "received from" trumpet-id)
                            (let [receivers (.trumpet! trumpeteer {:trumpet message :max-distance-meters distance :trumpetees subscribing-trumpetees :broadcast-fn sse/broadcast-message})]
                              (json-response {:trumpeteersWithinDistance (count receivers)})))))
           (ANY "*" []
                (route/not-found (slurp (io/resource "404.html")))))

(def rest-api
  (wrap-defaults app api-defaults))