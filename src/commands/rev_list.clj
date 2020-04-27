(ns commands.rev-list
  (:require [clojure.java.io :as io]
            [commands.utils.help-docs :as hmsg]
            [commands.utils.tools :as tools]
            [clojure.string :as String])
  (:import java.io.File))

(declare get-int get-commit-addr print-rev-list follow-commits print-rev-list-n follow-commits-n)

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
    :else (let [refs-path (str dir File/separator dbase File/separator "refs" File/separator "heads" File/separator)]
            (if (= (first args) "-n")
              (let [[_ z ref-name &more] args
                    n (get-int z)]
                (when (> n 0)
                  (if (or (= ref-name "HEAD") (= ref-name "@"))
                    (let [ref-path (str dir File/separator dbase File/separator "HEAD")]
                      (print-rev-list-n dir dbase refs-path ref-path n))
                    (let [ref-path (str refs-path ref-name)]
                      (if (not (.exists (io/file ref-path)))
                        (println (str "Error: could not find ref named " ref-name "."))
                        (print-rev-list-n dir dbase refs-path ref-path n))))))
              (let [ref-name (first args)]
                (if (or (= ref-name "HEAD") (= ref-name "@"))
                  (let [ref-path (str dir File/separator dbase File/separator "HEAD")]
                    (print-rev-list dir dbase refs-path ref-path))
                  (let [ref-path (str refs-path ref-name)]
                    (if (not (.exists (io/file ref-path)))
                      (println (str "Error: could not find ref named " ref-name "."))
                      (print-rev-list dir dbase refs-path ref-path)))))))))

(defn print-rev-list-n [dir dbase refs-path ref-path n]
  (when (> n 0)
    (let [ref-contents (String/trim-newline (slurp (io/file ref-path)))
          is-ref (= (subs ref-contents 0 4) "ref:")]
      (if is-ref
        (let [addr (String/trim-newline (get-commit-addr refs-path (String/trim-newline (subs ref-contents 16))))]
          (follow-commits-n dir dbase addr (- n 1)))
        (do
          (println ref-contents)
          (follow-commits-n dir dbase (String/trim-newline ref-contents) (- n 1)))))))

(defn follow-commits-n [dir dbase addr n]
  (when (> n 0)
    (let [addr-dir (subs addr 0 2)
          addr-fname (subs addr 2)
          addr-path (str dir File/separator dbase File/separator "objects" File/separator addr-dir File/separator addr-fname)
          commit-contents (tools/to-string (tools/byte-unzip (str addr-path)))
          splits (String/split commit-contents #"\n")
          parent-line (get splits 1)]
      (if (= "parent" (subs parent-line 0 6))
        (let [parent-addr (get (String/split parent-line #" ") 1)]
          (println parent-addr)
          (follow-commits-n dir dbase (String/trim-newline parent-addr) (- n 1)))))))


(defn print-rev-list [dir dbase refs-path ref-path]
  (let [ref-contents (String/trim-newline (slurp (io/file ref-path)))
        is-ref (= (subs ref-contents 0 4) "ref:")]
    (if is-ref
      (let [addr (String/trim-newline (get-commit-addr refs-path (String/trim-newline (subs ref-contents 16))))]
        (follow-commits dir dbase addr))
      (do
        (println ref-contents)
        (follow-commits dir dbase (String/trim-newline ref-contents))))))

(defn follow-commits [dir dbase addr]
  (let [addr-dir (subs addr 0 2)
        addr-fname (subs addr 2)
        addr-path (str dir File/separator dbase File/separator "objects" File/separator addr-dir File/separator addr-fname)
        commit-contents (tools/to-string (tools/byte-unzip (str addr-path)))
        splits (String/split commit-contents #"\n")
        parent-line (get splits 1)]
    (if (= "parent" (subs parent-line 0 6))
      (let [parent-addr (get (String/split parent-line #" ") 1)]
        (println parent-addr)
        (follow-commits dir dbase (String/trim-newline parent-addr))))))

(defn get-int [n]
  (try
    (let [out (Integer/parseInt n)]
      out)
    (catch Exception e false)))

(defn get-commit-addr [refs-path ref-name]
  (let [ref-path (str refs-path ref-name)]
    (if (not (.exists (io/file ref-path)))
      (println (str "Error: could not find ref named " ref-name "."))
      (let [ref-contents (slurp (io/file ref-path))]
        (printf "%s", ref-contents)
        ref-contents))))
