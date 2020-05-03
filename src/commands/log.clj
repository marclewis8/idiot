(ns commands.log
  (:require [clojure.java.io :as io]
            [commands.utils.help-docs :as hmsg]
            [commands.utils.tools :as tools]
            [clojure.string :as String])
  (:import java.io.File))

(declare get-int get-commit-addr print-log log-commit print-log-n log-commit-n)

(defn log [dir dbase args]
  (cond
    (= (first args) "-h") (println hmsg/log-h-message)
    (= (first args) "--help") (println hmsg/log-h-message)
    (or (not (.exists (io/file (str dir File/separator dbase))))
        (not (.isDirectory (io/file (str dir File/separator dbase)))))
    (println "Error: could not find database. (Did you run `idiot init`?)")
    (not= (first args) "--oneline") (println "Error: log requires the --oneline switch")
    (and (= (first (rest args)) "-n")
         (empty? (rest (rest args))))
    (println "Error: you must specify a numeric count with '-n'.")
    (and (= (first (rest args)) "-n")
         (not (get-int (first (rest (rest args))))))
    (println "Error: the argument for '-n' must be a non-negative integer.")
    :else (let [refs-path (str dir File/separator dbase File/separator "refs" File/separator "heads" File/separator)
                args (rest args)]
            (if (= (first args) "-n")
              (let [[_ z ref-name &more] args
                    n (get-int z)]
                (when (> n 0)
                  (if (or (nil? ref-name) (= ref-name "HEAD") (= ref-name "@"))
                    (let [ref-path (str dir File/separator dbase File/separator "HEAD")]
                      (print-log-n dir dbase refs-path ref-path n))
                    (let [ref-path (str refs-path ref-name)]
                      (if (not (.exists (io/file ref-path)))
                        (println (str "Error: could not find ref named " ref-name "."))
                        (print-log-n dir dbase refs-path ref-path n))))))
              (let [ref-name (first args)]
                (if (or (nil? ref-name) (= ref-name "HEAD") (= ref-name "@"))
                  (let [ref-path (str dir File/separator dbase File/separator "HEAD")]
                    (print-log dir dbase refs-path ref-path))
                  (let [ref-path (str refs-path ref-name)]
                    (if (not (.exists (io/file ref-path)))
                      (println (str "Error: could not find ref named " ref-name "."))
                      (print-log dir dbase refs-path ref-path)))))))))

(defn print-log [dir dbase refs-path ref-path]
  (let [ref-contents (String/trim-newline (slurp (io/file ref-path)))
        is-ref (= (subs ref-contents 0 4) "ref:")]
    (if is-ref
      (let [addr (String/trim-newline (get-commit-addr refs-path (String/trim-newline (subs ref-contents 16))))]
        (log-commit dir dbase addr))
      (log-commit dir dbase (String/trim-newline ref-contents)))))

(defn log-commit [dir dbase addr]
  (let [addr-dir (subs addr 0 2)
        addr-fname (subs addr 2)
        addr-path (str dir File/separator dbase File/separator "objects" File/separator addr-dir File/separator addr-fname)
        commit-contents (tools/to-string (tools/byte-unzip (str addr-path)))
        splits (String/split commit-contents #"\n")
        msg (get (String/split (get (String/split commit-contents #"\n\n") 1) #"\n") 0)
        parent-line (get splits 1)
        first-7 (subs addr 0 7)
        output (str first-7 " " msg)]
    (println output)
    (if (= "parent" (subs parent-line 0 6))
      (let [parent-addr (get (String/split parent-line #" ") 1)]
        (log-commit dir dbase (String/trim-newline parent-addr))))))

(defn print-log-n [dir dbase refs-path ref-path n]
  (when (> n 0)
    (let [ref-contents (String/trim-newline (slurp (io/file ref-path)))
          is-ref (= (subs ref-contents 0 4) "ref:")]
      (if is-ref
        (let [addr (String/trim-newline (get-commit-addr refs-path (String/trim-newline (subs ref-contents 16))))]
          (log-commit-n dir dbase addr 1))
        (log-commit-n dir dbase (String/trim-newline ref-contents) n)))))

(defn log-commit-n [dir dbase addr n]
  (when (> n 0)
    (let [addr-dir (subs addr 0 2)
          addr-fname (subs addr 2)
          addr-path (str dir File/separator dbase File/separator "objects" File/separator addr-dir File/separator addr-fname)
          commit-contents (tools/to-string (tools/byte-unzip (str addr-path)))
          splits (String/split commit-contents #"\n")
          msg (get (String/split (get (String/split commit-contents #"\n\n") 1) #"\n") 0)
          parent-line (get splits 1)
          first-7 (subs addr 0 7)
          output (str first-7 " " msg)]
      (println output)
      (if (= "parent" (subs parent-line 0 6))
        (let [parent-addr (get (String/split parent-line #" ") 1)]
          (log-commit-n dir dbase (String/trim-newline parent-addr) (- n 1)))))))

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
        ref-contents))))
