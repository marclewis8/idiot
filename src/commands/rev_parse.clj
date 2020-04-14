(ns commands.rev-parse
  (:require [clojure.java.io :as io]
            [commands.utils.tools :as tool]
            [commands.utils.help-docs :as hmsg])
  (:import java.io.File))

(declare print-ref)

(defn rev-parse [dir dbase args]
  (cond
    (= (count args) 0) (println "Error: you must specify a branch name.")
    (= (first args) "-h") (println hmsg/commit-tree-h-message)
    (= (first args) "--help") (println hmsg/commit-tree-h-message)
    (> (count args) 1) (println "Error: you must specify a branch name and nothing else.")
    (not (and (.exists (io/file (str dir File/separator dbase)))
              (.isDirectory (io/file (str dir File/separator dbase)))))
    (println "Error: could not find database. (Did you run `idiot init`?)")
    :else (let [ref-name (first args)
                refs-path (str dir File/separator dbase File/separator "refs" File/separator "heads" File/separator)]
            (if (or (= ref-name "HEAD")
                    (= ref-name "@"))
              (let [ref-contents (slurp (io/file (str dir File/separator dbase File/separator "HEAD")))
                    isRef (= (subs ref-contents 0 4) "ref:")]
                (if isRef
                  (print-ref dir dbase refs-path (clojure.string/trim-newline (subs ref-contents 16)))
                  (printf "%s", ref-contents)))
              (print-ref dir dbase refs-path ref-name)))))

(defn print-ref [dir dbase refs-path ref-name]
  (let [ref-path (str refs-path ref-name)]
    (if (not (.exists (io/file ref-path)))
      (println (str "Error: could not find ref named " ref-name "."))
      (let [ref-contents (slurp (io/file ref-path))]
        (printf "%s", ref-contents)))))
