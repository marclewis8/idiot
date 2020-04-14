(ns commands.init
  (:require [clojure.java.io :as io]
            [commands.utils.help-docs :as hmsg])
  (:import java.io.File))

(defn branch [dir dbase args]

