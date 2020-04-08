(ns commands.init
  (:require [clojure.java.io :as io]
            [commands.utils.help-docs :as hmsg])
  (:import java.io.File))

(defn init [dir dbase args]
  (let [[flag] args]
    (cond
      (or (= flag "-h") (= flag "--help")) (println hmsg/init-h-message)
      (not= flag nil) (println "Error: init accepts no arguments")
      (.isDirectory (io/file (str dir File/separator dbase))) (println (str "Error: " dbase " directory already exists"))
      :else (do
              (.mkdir (io/file (str dir File/separator dbase)))
              (.mkdir (io/file (str dir File/separator dbase File/separator "objects")))
              (.mkdir (io/file (str dir File/separator dbase File/separator "refs")))
              (io/copy "ref: refs/heads/master\n" (io/file (str dir File/separator dbase File/separator "refs" File/separator "HEAD")))
              (println (str "Initialized empty Idiot repository in " dbase " directory"))))))
