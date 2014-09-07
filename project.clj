(defproject trumpet-server "0.1.0-SNAPSHOT"
            :description "FIXME: write description"
            :url "http://example.com/FIXME"
            :license {:name "Eclipse Public License"
                      :url  "http://www.eclipse.org/legal/epl-v10.html"}
            :dependencies [[org.clojure/clojure "1.6.0"]
                           [compojure "1.1.8"]
                           [ring/ring-defaults "0.1.1"]
                           [ring/ring-jetty-adapter "1.3.0"]
                           [cheshire "5.3.1"]
                           [clj-http "1.0.0"]
                           [com.ninjudd/eventual "0.4.1"]
                           [com.ninjudd/ring-async "0.3.1"]
                           [org.clojure/tools.logging "0.3.0"]
                           [schejulure "1.0.0"] [useful "0.8.8"]
                           [useful "0.8.8"]]
            :profiles {:dev {
                              :dependencies [[midje "1.6.3"]]
                              :plugins      [[lein-midje "3.1.3"]
                                             [lein-ring "0.8.11"]]
                              :ring         {:handler trumpet-server.boot/site-and-api
                                             :port    5000}}
                       }
            :main trumpet-server.boot)
