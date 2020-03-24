(ns commands.hash-object
  (:require [commands.utils.tools :as tool]
            [clojure.java.io :as io]
            [commands.utils.help-docs :as hmsg]

            )
  (:import java.io.File)
  )

(defn hash-object [dir dbase args]
  ; depending on arity of call, do different things
  ; arity 3 - expect -h,--help, or file
  ; arity 4 - expect -w and file name
  (if (= (count args) 0) (println "Error: you must specify a file.") nil)

  (if (= (count args) 1)
    ; arity 1, no blob creation, f is file or help request
    (let [[f] args]
      (cond
        (or (= f "-h") (= f "--help")) (println hmsg/hash-h-message)
        (= f "-w") (println "Error: you must specify a file.") ; special case
        (not (.isDirectory (io/file (str dir File/separator dbase)))) (println "Error: could not find database. (Did you run `idiot init`?)")
        (not (.exists (io/file (str dir File/separator f)))) (println "Error: that file isn't readable")
        :else
        (let [content (slurp (str dir File/separator f))
              content+header (str "blob " (count content) "\000" content)
              address (tool/sha1-sum content+header)]
          ;(println address)
          (str address)))))
  ; case of other arity, working off of 2
  (if (= (count args) 2)
    (let [[flag file] args]
      (cond
        (or (= flag "-h") (= flag "--help")) (println hmsg/hash-h-message)
        (not (.isDirectory (io/file (str dir File/separator dbase)))) (println "Error: could not find database. (Did you run `idiot init`?)")
        (not= flag "-w") (println "Error: invalid flag given")
        (not (.exists (io/file (str dir File/separator file)))) (println "Error: that file isn't readable")
        :else
        (let [content (slurp (str dir File/separator file))
              content+header (str "blob " (count content) "\000" content)
              address (tool/sha1-sum content+header)
              dirname (subs address 0 2)
              fname (subs address 2)]
          ;(println address)
          (.mkdir (io/file (str dir File/separator dbase File/separator "objects" File/separator dirname)))
          (let [zipped-content (tool/zip-str content+header)]
            (io/copy zipped-content (io/file (str dir File/separator dbase File/separator "objects" File/separator dirname File/separator fname))))

          (str address) ; format so that hash-object returns the address, instead of printing it, for use in other functions
          )))))
