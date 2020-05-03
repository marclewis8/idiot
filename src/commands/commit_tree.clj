(ns commands.commit-tree
  (:require [clojure.java.io :as io]
            [commands.utils.help-docs :as hmsg]
            [commands.utils.tools :as tool]
            [clojure.string :as String])
  (:import java.io.File))

(declare write-object)

(defn commit-tree [dir dbase args]
  (let [[tree-abb mflag msg & more] args
        tree (tool/abbrev-to-full-hash dir dbase tree-abb)]
    (cond
      (or (= tree-abb "-h") (= tree-abb "--help")) (println hmsg/commit-tree-h-message)
      (not (.exists (io/file (str dir File/separator dbase)))) (println "Error: could not find database. (Did you run `idiot init`?)")
      (or (nil? tree-abb) (= tree-abb "-m")) (println "Error: you must specify a tree address.")
      (String/includes? tree "Error") (println tree)
      (or (not (.exists (io/file (str dir File/separator dbase File/separator "objects" File/separator (subs tree 0 2)))))
          (not (.exists (io/file (str dir File/separator dbase File/separator "objects" File/separator (subs tree 0 2) File/separator (subs tree 2))))))
      (println "Error: no tree object exists at that address.")
      (not (= "tree" (tool/find-type (tool/byte-unzip (str dir
                                                           File/separator
                                                           dbase
                                                           File/separator
                                                           "objects"
                                                           File/separator
                                                           (subs tree 0 2)
                                                           File/separator
                                                           (subs tree 2))))))
      (println "Error: an object exists at that address, but it isn't a tree.")
      (not= mflag "-m") (println "Error: you must specify a message.")
      (nil? msg) (println "Error: you must specify a message with the -m switch.")
      :else (if (= "-p" (first more))
              ; Handle commit with parents`
              (if (= (count (rest more)) 0)
                (println "Error: you must specify a commit object with the -p switch.")
                (loop [parent-entries ""
                       parent-list more]
                  (if (= (count parent-list) 0)
                    ; Done adding parents
                    (let [author-str "Linus Torvalds <torvalds@transmeta.com> 1581997446 -0500"
                          commit-format (str "tree %s\n"
                                             "%s"
                                             "author %s\n"
                                             "committer %s\n"
                                             "\n"
                                             "%s\n")
                          commit-str (format commit-format
                                             tree
                                             parent-entries
                                             author-str
                                             author-str
                                             msg)
                          commit-object (format "commit %d\000%s"
                                                (count commit-str)
                                                commit-str)
                          commit-object-addr (tool/to-hex-string (tool/sha-bytes (.getBytes commit-object)))]
                      (write-object dir dbase commit-object-addr commit-object)
                      (println commit-object-addr))
                    ; Add more parents
                    (let [pname (first (rest parent-list))]
                      (if (nil? pname)
                        (println "Error: you must specify a commit object with the -p switch.")
                        (let [pname (tool/abbrev-to-full-hash dir dbase pname)
                              pdir (subs pname 0 2)
                              pfname (subs pname 2)]
                          (if (String/includes? pname "Error")
                            (println pname)
                            (if (not (and (.exists (io/file (str dir File/separator dbase File/separator "objects" File/separator pdir)))
                                          (.isDirectory (io/file (str dir File/separator dbase File/separator "objects" File/separator pdir)))
                                          (.exists (io/file (str dir File/separator dbase File/separator "objects" File/separator pdir File/separator pfname)))))
                              (println (str "Error: no commit object exists at address " pname "."))
                              (if (not (= (tool/find-type (tool/byte-unzip (str dir File/separator dbase File/separator
                                                                                "objects" File/separator pdir File/separator
                                                                                pfname))) "commit"))
                                (println (str "Error: an object exists at address " pname ", but it isn't a commit."))
                                (recur (str parent-entries (str "parent " pname "\n")) (rest (rest parent-list))))))))))))
              ; Handle case with no parents
              (let [author-str "Linus Torvalds <torvalds@transmeta.com> 1581997446 -0500"
                    commit-format (str "tree %s\n"
                                       "author %s\n"
                                       "committer %s\n"
                                       "\n"
                                       "%s\n")
                    commit-str (format commit-format
                                       tree
                                       author-str
                                       author-str
                                       msg)
                    commit-object (format "commit %d\000%s"
                                          (count commit-str)
                                          commit-str)
                    commit-object-addr (tool/to-hex-string (tool/sha-bytes (.getBytes commit-object)))]
                (write-object dir dbase commit-object-addr commit-object)
                (println commit-object-addr))))))

; Write object to databse given dir, dbase, address, and object contents.
(defn write-object [dir dbase object-addr object]
  (let [object-dir (subs object-addr 0 2)
        object-fname (subs object-addr 2)]
    (when (not (.exists (io/file (str dir
                                      File/separator
                                      dbase
                                      File/separator
                                      "objects"
                                      File/separator
                                      object-dir))))
      (.mkdir (io/file (str dir File/separator
                            dbase File/separator
                            "objects" File/separator
                            object-dir))))

    (let [zipped-content (tool/zip-str object)]
      (io/copy zipped-content (io/file (str dir File/separator
                                            dbase File/separator
                                            "objects" File/separator
                                            object-dir File/separator
                                            object-fname))))))
