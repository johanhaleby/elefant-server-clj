(ns trumpet-server.boot
  (:require [trumpet-server.api.rest :refer [rest-api]]
            [ring.adapter.jetty-async :refer [run-jetty-async]]))

(defn start-server [& [port]]
  (let [port (Integer. (or port 5000))]
    (run-jetty-async rest-api {:port port :join? false}))
  )
(defn -main [& [port]]
  (start-server port))