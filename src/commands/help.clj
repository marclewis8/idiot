(ns commands.help
  (:require [commands.utils.help-docs :as hmsg])
  )
; unlike the other functions, theres no need for help to grab dir or dbase names, even if they were provided
(defn help [args]
  (let [[flag-or-cmd] args]
    (cond
      (empty? args) (println hmsg/top-h-message)
      (or (= flag-or-cmd "-h") (= flag-or-cmd "--help")) (println hmsg/help-h-message)
      (= flag-or-cmd "help") (println hmsg/help-h-message)
      (= flag-or-cmd "init") (println hmsg/init-h-message)
      (= flag-or-cmd "hash-object") (println hmsg/hash-h-message)
      (= flag-or-cmd "cat-file") (println hmsg/cat-h-message)
      (= flag-or-cmd "write-wtree") (println hmsg/write-h-message)
      (= flag-or-cmd "commit-tree") (println hmsg/commit-tree-h-message)
      :else (println "Error: invalid command"))))