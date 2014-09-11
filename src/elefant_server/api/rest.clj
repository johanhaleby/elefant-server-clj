(ns elefant-server.api.rest
  (:require [elefant-server.domain.repository :as trumpeteer-repository]
            [elefant-server.api.number :refer [to-number]]
            [elefant-server.domain.sse-service :as sse]
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

(defn render-entry-point [base-uri {:keys [id latitude longitude]}]
  (let [data (-> {:_links {}}
                 (update-in [:_links] assoc :self {:href (str base-uri "?latitude=" latitude "&longitude=" longitude)})
                 (update-in [:_links] assoc :subscribe {:href (str base-uri "/trumpeteers/" id "/subscribe")})
                 (update-in [:_links] assoc :location {:href (str base-uri "/trumpeteers/" id "/location")})
                 (update-in [:_links] assoc :trumpet {:href (str base-uri "/trumpeteers/" id "/trumpet")})
                 (update-in [:_links] assoc :trumpeteer {:href (str base-uri "/trumpeteers/" id)})
                 (assoc :trumpeteerId id))]
    data))

(defn json-response [data & [status]]
  {:status  (or status 200)
   :headers {"Content-Type" content-type-hal}
   :body    (json/generate-string data)})

(defn- add-links-to-trumpet [base-uri broadcaster]
  (fn [id trumpet]
    (let [trumpet-with-links (assoc trumpet :_links {:echo {:href (str base-uri "/trumpeteers/" id "/echo") :method "post"}})]
      (broadcaster id trumpet-with-links)
      )))

(defn- broadcast-trumpet! [{:keys [trumpet-id request distance message message-id broadcast-fn]}]
  (let [trumpet-id (to-number trumpet-id "trumpet-id")
        distance (if (nil? distance) nil (to-number distance "distance"))
        trumpeteer (trumpeteer-repository/get-trumpeteer trumpet-id)
        trumpetees (trumpeteer-repository/get-all-trumpeteers)
        subscriber-ids (sse/get-subscriber-ids)
        broadcast-fn (add-links-to-trumpet (get-base-uri request) broadcast-fn)
        subscribing-trumpetees (filter #(some #{(:id %)} subscriber-ids) trumpetees)]
    (log/info "Trumpet" message "received from" trumpet-id)
    (let [receivers (.trumpet! trumpeteer {:trumpet message :max-distance-meters distance :trumpetees subscribing-trumpetees :broadcast-fn broadcast-fn :message-id message-id})]
      (json-response {:trumpeteersWithinDistance (count receivers)}))))

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
                           (if (nil? trumpeteer)
                             (json-response {:errorMessage (str "Couldn't find trumpeteer with id " trumpet-id)} 400)
                             (sse/subscribe! trumpeteer))))
                    (GET ["/trumpeteers/:trumpet-id" :trumpet-id #"[0-9]+"] [trumpet-id :as request]
                         (let [trumpet-id (to-number trumpet-id)
                               trumpeteer (trumpeteer-repository/get-trumpeteer trumpet-id)
                               trumpeteers (trumpeteer-repository/get-all-trumpeteers)
                               base-uri (get-base-uri request)
                               subscriber-ids (sse/get-subscriber-ids)
                               subscribing-trumpeters (filter #(some #{(:id %)} subscriber-ids) trumpeteers)
                               subscribing-trumpeters-in-range (.filter-in-range trumpeteer subscribing-trumpeters)
                               anonymous-subscribing-trumpeters-in-range (map #(select-keys % [:latitude :longitude]) subscribing-trumpeters-in-range) ; Remove id from each trumpeteer
                               response-dto (assoc trumpeteer :_links {:self {:href (str base-uri "/trumpeteers/" trumpet-id)}} :trumpeteersInRange anonymous-subscribing-trumpeters-in-range)]
                           (json-response response-dto)))
                    (PUT ["/trumpeteers/:trumpet-id/location" :trumpet-id #"[0-9]+"] [trumpet-id latitude longitude :as request]
                         (let [trumpet-id (to-number trumpet-id "trumpet-id")
                               latitude (to-number latitude "latitude")
                               longitude (to-number longitude "longitude")
                               trumpeteer (trumpeteer-repository/get-trumpeteer trumpet-id)
                               trumpeteer-with-new-location (assoc trumpeteer :latitude latitude :longitude longitude)
                               trumpetees (trumpeteer-repository/get-all-trumpeteers)
                               number-of-trumpeteers-in-range (count (.filter-in-range trumpeteer-with-new-location trumpetees))]
                           (trumpeteer-repository/update-trumpeteer! trumpeteer-with-new-location)
                           (json-response {:trumpeteersInRange number-of-trumpeteers-in-range})))
                    (POST ["/trumpeteers/:trumpet-id/trumpet" :trumpet-id #"[0-9]+"] [trumpet-id message distance :as request]
                          (broadcast-trumpet! {:trumpet-id trumpet-id :request request :distance distance :message message :broadcast-fn sse/broadcast-message!}))
                    (POST ["/trumpeteers/:trumpet-id/echo" :trumpet-id #"[0-9]+"] [trumpet-id message distance messageId :as request]
                          (broadcast-trumpet! {:trumpet-id trumpet-id :request request :distance distance :message message :message-id messageId :broadcast-fn sse/broadcast-message!}))
                    (ANY "*" []
                         (route/not-found (slurp (io/resource "404.html"))))))

(def rest-api
  (wrap-defaults app api-defaults))