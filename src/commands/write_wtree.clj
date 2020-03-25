(ns commands.write-wtree
  (:require [clojure.java.io :as io]
            [commands.utils.help-docs :as hmsg]
            [commands.utils.tools :as tools]
            [clojure.pprint :refer :all]
            [commands.hash-object :as hash]
            )

  (:import java.io.File)
  )

(declare write-entry
         dir-to-tree-entry
         write-blob-object-and-return-blob-entry
         dir-to-tree-object-contents
         hash-and-store-tree-object)

(defn write-entry [dir dbase f] ; returns byte array, processes entry type and passes to appropriate fn
  (if (.isDirectory (io/file (str dir File/separator f)))
    (do
      ;(println "Making a tree entry!")
      (dir-to-tree-entry (str dir File/separator f) (str ".." File/separator dbase)))
    (do
      ;(println "Making a blob entry!")
      (write-blob-object-and-return-blob-entry dir dbase f)) ;else
    )
  )

(defn write-blob-object-and-return-blob-entry [dir dbase file]
  (let [mode "100644"
        path dir
        address (hash/hash-object dir dbase (list "-w" file))
        entry (str mode " " path "\000" address)]
    ;(println entry) ; just for my sanity, not to keep
    ;(println (.getBytes entry))
    (.getBytes entry) ; return the bytes of the entry, for concatenation usage
    )
  )

(defn dir-to-tree-entry [dir dbase]
  (let [address (dir-to-tree-object-contents dir dbase)
        mode "040000"
        path dir
        entry (str mode " " path "\000" address)]
    ;(println entry)
    (.getBytes entry)
  ))

(defn dir-to-tree-object-contents [dir dbase]
  (let [all (.list (io/file dir))
        str-all (->> all
                       (map str) ; turn them all into strings, should remove java.io.file nonsense
                       (filter (fn [f] (not (.contains f dbase)))) ; filter out the database and all its children
                       )
        ]
    ;(println "Location of dbase: " dbase)
    ;(println "Contents of " dir " directory:") TESTING MATERIAL
    ;(pprint str-all)

    (let [str-bytes-all (map (fn [f] (write-entry dir dbase f)) str-all) ; should be sequence of byte arrays
          str-bytes-concat (apply concat str-bytes-all) ; SHOULD BE just one byte array, but its still a lazy sequence
          header (str "tree" (count str-bytes-concat) "\000") ; header
          header-bytes (.getBytes header) ; make a byte array from that
          tree-bytes (byte-array (concat header-bytes str-bytes-concat)) ; should put header bytes on front of concat byte array
          ]

      ;(println tree-bytes) ; remove later, just for testing
      (if (= 0 (count all)) ; didn't work
             nil
             (hash-and-store-tree-object dir dbase tree-bytes)
             )
      ;(hash-and-store-tree-object dir dbase tree-bytes); return
      )
  ))



(defn hash-and-store-tree-object [dir dbase tree-bytes]
  (let [tree-hex (tools/to-hex-string (tools/sha-bytes tree-bytes))
        first2 (subs tree-hex 0 2)
        last38 (subs tree-hex 2 (count tree-hex))]
    (.mkdir (io/file (str dir File/separator dbase File/separator "objects" File/separator first2)))
    (let [zipped-tree (tools/zip-str tree-bytes)]
      (io/copy zipped-tree (io/file (str dir File/separator dbase File/separator "objects" File/separator first2 File/separator last38)))
      )
    tree-hex ; return hash to print
    )
  )
;TODO make function that takes in contents, and hashes / stores it in dbase

(defn write-wtree [dir dbase args]
  (cond
    (or (= (first args) "-h") (= (first args) "--help")) (println hmsg/write-h-message)
    (not= args nil) (println "Error: write-wtree accepts no arguments")
    (not (.isDirectory (io/file (str dir File/separator dbase)))) (println "Error: could not find database. (Did you run `idiot init`?)")
    (= 1 (count (.list (io/file dir)))) (println "The directory was empty, so nothing was saved.") ; testing = 1 because it will have .idiot directory
    :else (println (dir-to-tree-object-contents dir dbase))
    ))