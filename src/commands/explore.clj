(ns commands.explore
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [commands.utils.help-docs :as hmsg])
  (:import (java.io File))
  )
(declare get-branches-from-dir)
(declare explore dir dbase)

(defn handler [request]
  {:status 200  ; meaning "OK"
   :headers {"Content-Type" "text/html"}
   :body (str "<!DOCTYPE html>
                <html>
                  <head>
                    <title>Test</title>
                  </head>
                  <body>
                    <p> " (get-branches-from-dir dir dbase) " </p>
                  </body>
                </html>")}) ; any valid html will work here. TODO get hiccup conversion

(defn get-branches-from-dir [dir dbase]
  (let [all (.list (io/file (str dir File/separator dbase File/separator "refs" File/separator "heads")))
        all-str (map str all)
        all-together (str/join " " all-str)
        ]
    all-together
    )
  )

(defn explore [dir dbase more]
  (def dir dir)
  (def dbase dbase)
  (if (= (count more) 0)
    (do
      (printf "%s" "Starting server on port 3000.")
      (run-jetty handler {:port 3000})
      )
    (let [flag (first more)]
      (cond
        (or (= flag "-h") (= flag "--help")) (println hmsg/explore-h-message)
        (not (.isDirectory (io/file (str dir File/separator dbase)))) (println "Error: could not find database. (Did you run `idiot init`?)")
        (not= flag "-p") (println "Error: unrecognized flag")
        (= (count more) 1) (println "Error: you must specify a numeric port with '-p'.")
        :else (let [port (Long/parseLong (second more))]
                (if (or (not= (type port) (type 30)) (not (> port 0)))
                  (println "Error: The argument for '-p' must be a non-negative integer.")
                  (do
                    (println (str "Starting server on port " (str port) "."))
                    (run-jetty handler {:port port})
                    )
                  )
                )
          )
        )
      )
    )
