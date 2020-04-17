(ns commands.branch
  (:require [clojure.java.io :as io]
            [commands.utils.help-docs :as hmsg]
            [clojure.string :as String])
  (:import java.io.File))

(declare delete-branch print-refs)

(defn branch [dir dbase args]
  (cond
    (or (= (first args) "-h") (= (first args) "--help")) (println hmsg/branch-h-message)
    (and (= (first args) "-d") (= 0 (count (rest args)))) (println "Error: you must specify a branch name.")
    (and (= (first args) "-d") (> (count (rest args)) 1)) (println "Error: invalid arguments.")
    (and (not= (first args) "-d") (> (count args) 0)) (println "Error: invalid arguments.")
    (not (and (.exists (io/file (str dir File/separator dbase)))
              (.isDirectory (io/file (str dir File/separator dbase)))))
    (println "Error: could not find database. (Did you run `idiot init`?)")
    :else (let [head-contents (slurp (io/file (str dir File/separator dbase File/separator "HEAD")))
                refs-path (str dir File/separator dbase File/separator "refs" File/separator "heads" File/separator)]
            (if (= (first args) "-d")
              (delete-branch refs-path (String/trim-newline (subs head-contents 16)) (first (rest args)))
              (print-refs head-contents refs-path)))))

(defn delete-branch [refs-path head-ref ref-to-del]
  (if (not (.exists (io/file (str refs-path ref-to-del))))
    (println (str "Error: branch '" ref-to-del "' not found."))
    (if (= head-ref ref-to-del)
      (println (str "Error: cannot delete checked-out branch '" head-ref "'."))
      (do
        (io/delete-file (str refs-path ref-to-del))
        (println (str "Deleted branch " ref-to-del "."))))))

(defn print-refs [head-ref refs-path]
  (loop [refs (sort (seq (.list (io/file refs-path))))]
    (if (empty? refs)
      nil
      (do
        (if (String/includes? head-ref (first refs))
          (println (str "* " (first refs)))
          (println (str "  " (first refs))))
        (recur (rest refs))))))
