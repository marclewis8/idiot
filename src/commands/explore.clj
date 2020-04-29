(ns commands.explore
  (:require [ring.adapter.jetty :refer [run-jetty]]))

(defn handler [request]
  {:status 200  ; meaning "OK"
   :headers {"Content-Type" "text/html"}
   :body "<!DOCTYPE html>
            <html>
              <head>
                  <title>Test</title>
              </head>
              <body>
                <p> This is a comment. </p>
              </body>
            </html>"}) ; any valid html will work here. TODO get hiccup conversion

(defn explore [dir dbase more]

  (run-jetty handler {:port 3000})
  )