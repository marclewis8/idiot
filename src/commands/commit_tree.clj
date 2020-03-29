(ns commands.commit-tree
  (:require [clojure.java.io :as io]
            [commands.utils.help-docs :as hmsg]
            [commands.utils.tools :as tool])
  (:import java.io.File))

(declare write-object)

(defn commit-tree [dir dbase args]
  (let [[tree mflag msg & more] args]
    (cond
      (or (= tree "-h") (= tree "--help")) (println hmsg/commit-tree-h-message)
      (not (.exists (io/file (str dir File/separator dbase)))) (println "Error: could not find database. (did you run `idiot init`?)")
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
      (or (= tree nil) (= tree "-m")) (println "Error: you must specify a tree address.")
      (not= mflag "-m") (println "Error: you must specify a message.")
      (nil? msg) (println "Error: you must specify a message with the -m switch.")
      :else (if (= "-p" (first more))
              ; Handle commit with parents`
              (loop [parent-entries ""
                     parent-list (rest more)]
                (if (= (count parent-list) 0)
                  ; Done adding parents
                  (do
                    (let [author-str "Linus Torvalds <torvalds@transmeta.com> 1581997446 -0500"
                          commit-format (str "tree %s\n"
                                             "%s"
                                             "author %s\n"
                                             "commiter %s\n"
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
                      ;(println commit-object)
                      (println commit-object-addr)))
                  ; Add more parents
                  (recur (str parent-entries (str "parent " (first parent-list) "\n")) (rest parent-list))))
              ; Handle case with no parents
              (let [author-str "Linus Torvalds <torvalds@transmeta.com> 1581997446 -0500"
                    commit-format (str "tree %s\n"
                                       "author %s\n"
                                       "commiter %s\n"
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
                ;(println commit-object)
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
