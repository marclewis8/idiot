(ns tree_demo
  (:require [clojure.java.io :as io])

  (:import java.io.File)
  )

; jeff doing some io/file stuff. isDirectory, exists, etc.
; for A3 - list contents of directory .list
; to look at something in another directory,

(.exists (io/file "src")) ; --> true
(.list (io/file "src")) ; --> vector of stuff
(.exists (File. "src")) ; --> true
; exist can only go one level deep, parent and child, not parent, parent, chilc
; deal with recursive nature of directories
(defrecord FileSystemEntry [type parent-path name contents])

(declare ->Entry)

(defn ->FileEntry [parent-path name]
  (->FileSystemEntry :file parent-path name (slurp (str parent-path File/separator name)))
  )

(defn ->DirEntry [parent-path name]
  (->FileSystemEntry :dir parent-path name
                     (for [sub-entry-name (.list (io/file parent-path name))]
                       (->Entry (str parent-path File/separator name))))
  )

(defn ->Entry [parent-path name]
  (let [file (io/file parent-path name)]
    (if (.isDirectory file)
      (->DirEntry parent-path name)
      (->FileEntry parent-path name)
      )
    )
  )
; Find way to filter out .idiot from this


; modified to work recursively
;create one with
(->FileSystemEntry :file "src" "tree_demo.clj" "<contents>") ; like a constructor
; --> {:type :file, etc.} it makes a hm, but with your properties
;path separator / vs. \
File/separator ; will be the file separator of the testing OS

