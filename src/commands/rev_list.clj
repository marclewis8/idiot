(ns commands.rev-list
  (:require [clojure.java.io :as io]
            [commands.utils.help-docs :as hmsg]
            [clojure.string :as String])
  (:import java.io.File))

(declare get-int print-ref)

(defn rev-list [dir dbase args]
  (cond
    (= (count args) 0) (println "Error: you must specify a branch name.")
    (= (first args) "-h") (println hmsg/rev-list-h-message)
    (= (first args) "--help") (println hmsg/rev-list-h-message)
    (not (and (.exists (io/file (str dir File/separator dbase)))
              (.isDirectory (io/file (str dir File/separator dbase)))))
    (println "Error: could not find database. (Did you run `idiot init`?)")
    (and (= (first args) "-n")
         (empty? (rest args)))
    (println "Error: you must specify a numeric count with '-n'.")
    (and (= (first args) "-n")
         (not (get-int (first (rest args)))))
    (println "Error: the argument for '-n' must be a non-negative integer.")
    :else (if (= (first args) "-n")
            (let [ref-name (first args)
                refs-path (str dir File/separator dbase File/separator "refs" File/separator "heads" File/separator)]
            (if (or (= ref-name "HEAD")
                    (= ref-name "@"))
              (let [ref-contents (slurp (io/file (str dir File/separator dbase File/separator "HEAD")))
                    isRef (= (subs ref-contents 0 4) "ref:")]
                (if isRef
                  (print-ref refs-path (String/trim-newline (subs ref-contents 16)))
                  (printf "%s", ref-contents)))
              (print-ref refs-path ref-name)))))

(defn get-int [n]
  (try
    (let [out (Integer/parseInt n)]
      out)
    (catch Exception e false)))

(defn print-ref [refs-path ref-name]
  (let [ref-path (str refs-path ref-name)]
    (if (not (.exists (io/file ref-path)))
      (println (str "Error: could not find ref named " ref-name "."))
      (let [ref-contents (slurp (io/file ref-path))]
        (printf "%s", ref-contents)))))
