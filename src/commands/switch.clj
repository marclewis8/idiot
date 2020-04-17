(ns commands.switch
  (:require [clojure.java.io :as io]
            [commands.utils.help-docs :as hmsg]
            [clojure.string :as String])
  (:import java.io.File))

(declare update-head create-ref)

(defn switch [dir dbase args]
  (cond
    (= (count args) 0) (println "Error: you must specify a branch name.")
    (= (first args) "-h") (println hmsg/switch-h-message)
    (= (first args) "--help") (println hmsg/switch-h-message)
    (and (= (first args) "-c") (> (count args) 2)) (println "Error: you may only specify one branch name.")
    (not (and (.exists (io/file (str dir File/separator dbase)))
              (.isDirectory (io/file (str dir File/separator dbase)))))
    (println "Error: could not find database. (Did you run `idiot init`?)")
    :else (if (= (first args) "-c")
            ; -c flag
            (if (> (count args) 2)
              (println "Error: you may only specify one branch name.")
              (let [ref-name (first (rest args))]
                (if (nil? ref-name)
                  ; Check for branch name
                  (println "Error: you must specify a branch name.")
                  (let [refs-path (str dir File/separator dbase File/separator "refs" File/separator "heads" File/separator)]
                    ; Check to make sure branch doesn't already exist
                    (if (.exists (io/file (str refs-path ref-name)))
                      (println "Error: a ref with that name already exists.")
                      (do
                        (create-ref dir dbase ref-name)
                        (update-head dir dbase ref-name)
                        (println (str "Switched to a new branch '" ref-name "'"))))))))
            ; no -c flag
            (if (> (count args) 1)
              (println "Error: you may only specify one branch name.")
              (let [ref-name (first args)
                    refs-path (str dir File/separator dbase File/separator "refs" File/separator "heads" File/separator)]
                (if-not (.exists (io/file (str refs-path ref-name)))
                  (println "Error: no ref with that name exists.")
                  (do
                    (update-head dir dbase ref-name)
                    (println (str "Switched to branch '" ref-name "'")))))))))

(defn update-head [dir dbase new-ref]
  (let [head-path (str dir File/separator dbase File/separator "HEAD")]
    (io/copy (str "ref: refs/heads/" new-ref "\n") (io/file head-path))))

(defn create-ref [dir dbase new-ref]
  (let [head-contents (slurp (io/file (str dir File/separator dbase File/separator "HEAD")))
        refs-path (str dir File/separator dbase File/separator "refs" File/separator "heads" File/separator)
        is-ref (= "ref:" (subs head-contents 0 4))]
    (if is-ref
      (let [head-ref (String/trim-newline (subs head-contents 16))
            ref-contents (slurp (str refs-path head-ref))]
        (io/copy ref-contents (io/file (str refs-path new-ref))))
      (io/copy head-contents (io/file (str refs-path new-ref))))))
