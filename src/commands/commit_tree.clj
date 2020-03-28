(ns commands.commit-tree
  (:require [clojure.java.io :as io]
            [commands.utils.help-docs :as hmsg])
  )

(defn commit-tree [dir dbase args]
  (let [[tree mflag msg & more] args]
    (cond
      (or (= tree "-h") (= tree "--help")) (println hmsg/commit-tree-h-message)
      (not (.exists (io/file dbase))) (println "Error: could not find database (did you run `idiot init`?)")
      (or (= tree nil) (= tree "-m")) (println "Error: you must specify a tree address.")
      (not= mflag "-m") (println "Error: you must specify a message.")
      :else (let [author+committer "Linus Torvalds <torvalds@transmeta.com> 1581997446 -0500"]



              )

      )
    )
  )
