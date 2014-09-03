(ns trumpet-server.api.rest
  (:require [trumpet-server.domain.repository :as trumpeter-repository]
            [trumpet-server.api.number :refer [to-number]]
            [compojure.core :refer [defroutes GET PUT POST DELETE ANY]]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [clojure.java.io :as io]
            [cheshire.core :as json]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]))

(def content-type-hal "application/hal+json; charset=utf-8")

(defn get-host [request]
  "Generate host from a ring request. For example 'http://localhost:5000'."
  (let [scheme (name (:scheme request))
        hostname (get (:headers request) "host")]
    (str scheme "://" hostname)))

(defn render-entry-point [hostname {:keys [id latitude longitude]}]
  (let [data (-> {:_links {}}
                 (update-in [:_links] assoc :self {:href (str hostname "?latitude=" latitude "&longitude=" longitude)})
                 (update-in [:_links] assoc :subscribe {:href (str hostname "/trumpeters/" id "/subscribe")})
                 (update-in [:_links] assoc :location {:href (str hostname "/trumpeters/" id "/location")})
                 (update-in [:_links] assoc :trumpet {:href (str hostname "/trumpeters/" id "/trumpet")}))]
    data))

(defn json-response [data & [status]]
  {:status  (or status 200)
   :headers {"Content-Type" content-type-hal}
   :body    (json/generate-string data)})

(defroutes app
           (GET "/" [latitude longitude :as request]
                (let [lat (to-number latitude "latitude")
                      long (to-number longitude "longitude")
                      host (get-host request)
                      trumpeter (trumpeter-repository/new-trumpeter! {:latitude lat :longitude long})]
                  (json-response (render-entry-point host trumpeter))))
           (GET ["/trumpeters/:trumpet-id/subscribe" :trumpet-id #"[0-9]+"] [trumpet-id :as request] ; trumpet-id must be an int otherwise route won't match
                (let [trumpet-id (to-number trumpet-id)]
                  (json-response (trumpeter-repository/get-trumpeter trumpet-id))))
           (ANY "*" []
                (route/not-found (slurp (io/resource "404.html")))))

(def rest-api
  (wrap-defaults app api-defaults))