(ns commands.cat-file
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [commands.utils.tools :as tool]
            [commands.utils.help-docs :as hmsg])
  (:import java.io.File))

(declare print-blob print-tree print-commit)

(defn cat-file [dir dbase args]
  ; working off of arity 2
  (let [[flag address] args]
    (cond
      (or (= flag "-h") (= flag "--help")) (println hmsg/cat-h-message)
      (not (.isDirectory (io/file (str dir File/separator dbase)))) (println "Error: could not find database. (Did you run `idiot init`?)")
      (and (not= flag "-p") (not= flag "-t")) (println "Error: the -p or -t switch is required")
      (= address nil) (println "Error: you must specify an address")
      ; check existence of address in database
      :else
      (let [dirname (subs address 0 2)
            fname (subs address 2)]
        (if (not (.isDirectory (io/file (str dir File/separator dbase File/separator "objects" File/separator dirname))))
          (println "Error: that address doesn't exist")
          (if (not (.exists (io/file (str dir File/separator dbase File/separator "objects" File/separator dirname File/separator fname))))
            (println "Error: that address doesn't exist")
            (let [targ-path (str dir File/separator dbase File/separator "objects" File/separator dirname File/separator fname)
                  contents (tool/byte-unzip targ-path)
                  obj-type (tool/find-type contents)]
              (case flag
                "-p" (case obj-type
                       "blob" (print-blob contents)
                       "tree" (print-tree contents)
                       "commit" (println "lo"))
                "-t" (println obj-type)))))))))

(defn print-blob [contents]
  (printf "%s" (tool/to-string (get (tool/split-at-byte 0 contents) 1))))

(defn print-tree [contents]
  (loop [remaining-contents (get (tool/split-at-byte 0 contents) 1) output ""]
    (let [obj-type (tool/to-string (get (tool/split-at-byte 32 remaining-contents) 0))
          rem1 (get (tool/split-at-byte 32 remaining-contents) 1)
          obj-name (tool/to-string (get (tool/split-at-byte 0 rem1) 0))
          rem2 (get (tool/split-at-byte 0 rem1) 1)
          ]
      (println obj-type)
      (println obj-name))))



;            (let [unzipped-contents+header
;                  (with-open [input (-> (str dir File/separator dbase File/separator "objects" File/separator dirname File/separator fname) io/file io/input-stream)]
;                    (tool/unzip input))
;                  split-contents (str/split unzipped-contents+header (re-pattern "\000"))
;                  type+length (first split-contents)
;                  type (first (str/split type+length (re-pattern " ")))
;                  contents-header-removed (second split-contents)
;                  contents (str/join contents-header-removed)
;                  contents-newline-trimmed (clojure.string/trim-newline contents)
;                  targ-path (str dir File/separator dbase File/separator "objects" File/separator dirname File/separator fname)]
;              (case flag
;                ;"-p" (println contents-newline-trimmed)
;                "-p" (p-print dir dbase targ-path)
;                "-t" (println (tool/find-type (tool/byte-unzip targ-path)))
;                )))))))) ; this is what I want

