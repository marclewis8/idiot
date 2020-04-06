(ns commands.write-wtree
  (:require [clojure.java.io :as io]
            [commands.utils.help-docs :as hmsg]
            [commands.utils.tools :as tools]
            [commands.hash-object :as hash]
            [clojure.string :as str])

  (:import java.io.File))

(declare write-entry
         dir-to-tree-entry
         write-blob-object-and-return-blob-entry
         dir-to-tree-object-contents
         hash-and-store-tree-object)

(defn write-entry [dir dbase f] ; returns byte array, processes entry type and passes to appropriate fn
  (cond
    (and (.isDirectory (io/file (str dir File/separator f))) (not (empty? (.list (io/file (str dir File/separator f)))))) ; filter out empty directories
      ;(println "Making a tree entry for " f "!")
    (dir-to-tree-entry (str dir File/separator f) (str ".." File/separator dbase))
    (not (.isDirectory (io/file (str dir File/separator f))))
      ;(println "Making a blob entry for " f "!")
    (write-blob-object-and-return-blob-entry dir dbase f) ;else
    :else
      ;(println "Skipping making a tree entry for empty directory " f ".\n")
    nil))

(defn write-blob-object-and-return-blob-entry [dir dbase file]
  (let [hash (hash/hash-object dir dbase (list file))
        first2 (subs hash 0 2)
        last38 (subs hash 2)
        alreadythere (.exists (io/file (str dir File/separator dbase File/separator "objects" File/separator first2 File/separator last38)))]

    (if alreadythere
      (let [mode "100644"
            path (str file)
            address (hash/hash-object dir dbase (list file)) ; skip writing when file is already there
            byte-addr (tools/from-hex-string address)
            entry (concat (.getBytes (str mode " " path "\000")) byte-addr)]
        ;(println "Didn't write to dbase")
        ;(println entry "\n")
        entry ; return the bytes of the entry, for concatenation usage
        )
      ; else
      (let [mode "100644"
            path (str file)
            address (hash/hash-object dir dbase (list "-w" file)) ; do write when file is not already there
            byte-addr (tools/from-hex-string address)
            entry (concat (.getBytes (str mode " " path "\000")) byte-addr)]
        ;(println "Wrote to dbase")
        ;(println entry "\n")
        entry ; return the bytes of the entry, for concatenation usage
        ))))

(defn dir-to-tree-entry [dir dbase]
  (let [address (tools/sha-bytes (dir-to-tree-object-contents dir dbase))
        mode "40000"
        fullpath dir ; returns a path from dir write-wtree was called on
        pathlist (str/split fullpath (re-pattern (str "\\" File/separator))) ; splits path into individual dir name chunks
        path (nth pathlist (- (count pathlist) 1)) ; takes last path (what i want)
        entry (concat (.getBytes (str mode " " path "\000")) address)]

    ;(println entry)
    entry))

(defn dir-to-tree-object-contents [dir dbase]
  (let [all (.list (io/file dir))
        str-all (->> all
                     (map str) ; turn them all into strings, should remove java.io.file nonsense
                     (filter (fn [f] (not (.contains f dbase)))) ; filter out the database and all its children
                     )
        str-bytes-all (map (fn [f] (write-entry dir dbase f)) str-all) ; should be sequence of byte arrays, with some nils from empty dirs and stuff
        ;str-bytes-no-nil (filter (fn [f] (not (nil? f))) str-bytes-all) ; the nils that came back from unsuccessful calls are skipped
        str-bytes-concat (apply concat str-bytes-all) ; should concatenate all bytes together, skipping nils
        header (str "tree" " " (count str-bytes-concat) "\000") ; header (count str-bytes-concat)
        header-bytes (.getBytes header) ; make a byte array from that
        tree-bytes (byte-array (concat header-bytes str-bytes-concat)) ; should put header bytes on front of concat byte array
        ]

      ;(ByteArrayOutputStream.) nah
      ;(println tree-bytes) ; remove later, just for testing
      ;(let [tree-readable (apply str (map char tree-bytes))]
      ;  (println tree-readable)
      ;  )
    (if (empty? str-bytes-concat)
      nil
      (do
        (hash-and-store-tree-object dir dbase tree-bytes) ; stores tree object, returns the hash for printing / entry-making
        tree-bytes))))

(defn hash-and-store-tree-object [dir dbase tree-bytes]
  (let [tree-hex (tools/to-hex-string (tools/sha-bytes tree-bytes))
        first2 (subs tree-hex 0 2)
        last38 (subs tree-hex 2 (count tree-hex))]
    (.mkdir (io/file (str dir File/separator dbase File/separator "objects" File/separator first2)))
    (let [zipped-tree (tools/zip-str tree-bytes)]
      (io/copy zipped-tree (io/file (str dir File/separator dbase File/separator "objects" File/separator first2 File/separator last38))))
    tree-hex ; return hash to print / use for address
    ))
;TODO make function that takes in contents, and hashes / stores it in dbase

(defn write-wtree [dir dbase args]
  (cond
    (or (= (first args) "-h") (= (first args) "--help")) (println hmsg/write-h-message)
    (not= args nil) (println "Error: write-wtree accepts no arguments")
    (not (.isDirectory (io/file (str dir File/separator dbase)))) (println "Error: could not find database. (Did you run `idiot init`?)")
    (= 1 (count (.list (io/file dir)))) (println "The directory was empty, so nothing was saved.") ; testing = 1 because it will have .idiot directory
    (= nil (dir-to-tree-object-contents dir dbase)) (println "The directory was empty, so nothing was saved")
    :else (println (tools/to-hex-string (tools/sha-bytes (dir-to-tree-object-contents dir dbase))))))
