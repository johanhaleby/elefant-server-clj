(ns trumpet-server.boot
  (:require [trumpet-server.api.rest :refer [rest-api]]
            [trumpet-server.site.elefant :refer [elefant-site]]
            [ring.adapter.jetty-async :refer [run-jetty-async]]
            [compojure.core :refer [routes]]
            [clojure.tools.logging :as log]
            [ring.middleware.defaults :refer [wrap-defaults]]
            [environ.core :refer [env]]))

(def site-and-api (routes rest-api elefant-site))

(defn start-server [& [port]]
  (let [port (Integer. (or port (env :port) 5000))]
    (run-jetty-async site-and-api {:port port :join? false}))
  )
(defn -main [& [port]]
  (start-server port))