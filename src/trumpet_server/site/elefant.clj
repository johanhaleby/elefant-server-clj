(ns trumpet-server.site.elefant
  (:require [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [compojure.route :as route]
            [compojure.core :refer [defroutes GET PUT POST DELETE ANY]]
            [clojure.java.io :as io]))

(defroutes site
           (GET "/" [] (slurp (io/resource "public/index.html")))
           (route/resources "/")
           (route/not-found (slurp (io/resource "404.html"))))

(def elefant-site
  (wrap-defaults site (assoc-in site-defaults [:security :anti-forgery] false))) ; Turn off CSRF