(ns trumpet-server.web
  (:require [trumpet-server.core :as core]
            [compojure.core :refer [defroutes GET PUT POST DELETE ANY]]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [clojure.java.io :as io]
            [ring.adapter.jetty :as jetty]
            [cheshire.core :as json]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [halresource.resource :as hal]))


(def content-type-hal "application/hal+json; charset=utf-8")

(defn new-uuid [] (str (java.util.UUID/randomUUID)))

(defn get-host [request]
  "Generate host from a ring request. For example 'http://localhost:5000'."
  (let [scheme (name (:scheme request))
        hostname (get (:headers request) "host")]
    (str scheme "://" hostname)))

(defn render-entry-point [hostname]
  (let [client-id (new-uuid)
        resource (-> (hal/new-resource hostname)
                     (hal/add-link :rel "subscribe" :href (str hostname "/clients/" client-id "/subscribe"))
                     (hal/add-link :rel "location" :href (str hostname "/clients/" client-id "/location"))
                     (hal/add-link :rel "trumpet" :href (str hostname "/clients/" client-id "/trumpet")))]
    (hal/resource->representation resource :json)))

(defroutes app
           (GET "/" [latitude longitude :as request]
                {
                  :status  200
                  :headers {"Content-Type" content-type-hal}
                  :body    (render-entry-point (get-host request))
                  })
           (GET "/test" r
                (str r))
           (ANY "*" []
                (route/not-found (slurp (io/resource "404.html")))))

(def rest-api
  (wrap-defaults app api-defaults))

(defn start-server [& [port]]
  (let [port (Integer. (or port 5000))]
    (jetty/run-jetty rest-api {:port port :join? false}))
  )
(defn -main [& [port]]
  (start-server port))