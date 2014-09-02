(ns trumpet-server.rest
  (:require [trumpet-server.core :as core]
            [trumpet-server.repository :as trumpet-repository]
            [trumpet-server.number :refer [to-number]]
            [compojure.core :refer [defroutes GET PUT POST DELETE ANY]]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [clojure.java.io :as io]
            [ring.adapter.jetty :as jetty]
            [cheshire.core :as json]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [halresource.resource :as hal]
            [clojure.core.async :refer [go >! chan close!]]
            [ninjudd.eventual.server :refer [edn-events]]
            [ring.adapter.jetty-async :refer [run-jetty-async]]))


(def content-type-hal "application/hal+json; charset=utf-8")

(defn get-host [request]
  "Generate host from a ring request. For example 'http://localhost:5000'."
  (let [scheme (name (:scheme request))
        hostname (get (:headers request) "host")]
    (str scheme "://" hostname)))

(defn render-entry-point [hostname trumpet-id]
  (let [resource (-> (hal/new-resource hostname)
                     (hal/add-link :rel "subscribe" :href (str hostname "/trumpeters/" trumpet-id "/subscribe"))
                     (hal/add-link :rel "location" :href (str hostname "/trumpeters/" trumpet-id "/location"))
                     (hal/add-link :rel "trumpet" :href (str hostname "/trumpeters/" trumpet-id "/trumpet")))]
    (hal/resource->representation resource :json)))

(defn json-response [data & [status]]
  {:status  (or status 200)
   :headers {"Content-Type" content-type-hal}
   :body    (json/generate-string data)})

(defroutes app
           (GET "/" [latitude longitude :as request]
                {
                  :status  200
                  :headers {"Content-Type" content-type-hal}
                  :body    (let [lat (to-number latitude "latitude")
                                 long (to-number longitude "longitude")
                                 host (get-host request)
                                 trumpet-id (trumpet-repository/new-trumpet! {:latitude lat :longitude long})]
                             (render-entry-point host trumpet-id))
                  })
           (GET ["/trumpeters/:trumpet-id/subscribe" :trumpet-id #"[0-9]+"] [trumpet-id :as request] ; trumpet-id must be an int otherwise route won't match
                (let [trumpet-id (to-number trumpet-id)]
                  (json-response (trumpet-repository/get-trumpet trumpet-id))))
           (GET "/reflect" r
                (str r))
           (ANY "*" []
                (route/not-found (slurp (io/resource "404.html")))))

(def rest-api
  (wrap-defaults app api-defaults))

(defn start-server [& [port]]
  (let [port (Integer. (or port 5000))]
    (run-jetty-async rest-api {:port port :join? false}))
  )
(defn -main [& [port]]
  (start-server port))