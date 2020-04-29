(ns idiot
  (:require [clojure.java.io :as io]
            [commands.cat-file :as do-cat]
            [commands.hash-object :as do-hash]
            [commands.help :as do-help]
            [commands.init :as do-init]
            [commands.commit-tree :as do-ctree]
            [commands.write-wtree :as do-wtree]
            [commands.utils.help-docs :as hmsg]
            [commands.rev-parse :as do-rev-parse]
            [commands.switch :as do-switch]
            [commands.branch :as do-branch]
            [commands.commit :as do-commit]
            [commands.rev-list :as do-rev-list]
            [commands.explore :as do-explore]))
(def top-args #{"-r" "-d"})
(def top-commands #{"help" "init" "hash-object" "cat-file" "-h" "--help" "commit-tree" "write-wtree" "rev-parse" "switch" "branch" "commit" "rev-list" "explore" nil})

(defn handle-r [argmap]
  (let [args (get argmap :args) desired-dir (second args)]
    (cond
      (= desired-dir nil) (do
                            (println "Error: the -r switch needs an argument")
                            (assoc argmap :error? "Y"))
      (not (.isDirectory (io/file desired-dir))) (do
                                                   (println "Error: the directory specified by -r does not exist")
                                                   (assoc argmap :error? "Y")))))

; parse-args takes in the whole string, and attempts to parse out the top level arguments, if they were provided.
; after this happens, an arglist will be passed back that main deciphers
; arglist is of this form (boolean string string list)
; boolean -> was an error encountered? string1 -> validated dir name, string2 -> validated dbase name, list -> list of rest of args, including cmd and all local arguments for cmd


(defn parse-args [args]

  (loop [currargs args is-r-handled false is-d-handled false spec-directory "." spec-dbase ".idiot"]
    (cond
      ; base case #1, when both potential flags have been encountered and handled
      (and is-d-handled is-r-handled) (list false spec-directory spec-dbase currargs)
      ; recur case, when a -r is encountered
      (and (= (first currargs) "-r") (not is-r-handled)) (let [[_ dir & more] currargs]
                                                           ; set desired directory from . to whatever they say and recur with rest
                                                           ;handle edge / failure cases
                                                           (cond
                                                             (= nil dir) (do
                                                                           (println "Error: the -r switch needs an argument")
                                                                           (list true spec-directory spec-dbase currargs))
                                                             (not (.isDirectory (io/file dir))) (do
                                                                                                  (println "Error: the directory specified by -r does not exist")
                                                                                                  (list true spec-directory spec-dbase more)) ; hit error, exit loop
                                                             :else (recur more true is-d-handled dir spec-dbase) ; recur with is-r-handled now set to true and spec-directory set to dir
                                                             ))
      ; recur case, when a -d is encountered
      (and (= (first currargs) "-d") (not is-d-handled)) (let [[_ database & more] currargs]
                                                           ; set desired file from .idiot to whatever they say
                                                           ;handle edge / failure cases
                                                           (cond
                                                             (= nil database) (do
                                                                                (println "Error: the -d switch needs an argument")
                                                                                (list true spec-directory spec-dbase currargs) ; error, get out
                                                                                )
                                                             :else (recur more is-r-handled true spec-directory database) ; recur with is-h-handled now set to true and spec-dbase set to database
                                                             ))
      (not (contains? top-commands (first currargs))) (do
                                                        (println "Error: invalid command")
                                                        (list true spec-directory spec-dbase currargs)) ; last case, will make error? true if it didn't reach a main-parsable command.
      :else (list false spec-directory spec-dbase currargs) ; base case #2 to give out an arglist that main can use with 0 or 1 flag applied to it
      )))

; evaluate command line arguments and pass to desired functionality


(defn -main [& xs]
  ; parse-args gives back a hashmap, this is just grabbing everything and putting a name to it
  (let [arglist (parse-args xs) error? (first arglist) dir (second arglist) dbase (nth arglist 2) [cmd & more] (nth arglist 3)]
    ; (println error? dir dbase cmd more)
    (cond
      error? nil ; no op when error was already encountered
      (not (contains? top-commands cmd)) (println "Error: invalid command")
      (= nil cmd) (do-help/help nil) ; guessing this still applies even in the case that -r or other top levels were provided
      (= cmd "help") (do-help/help more) ; in each case, I'm able to assume that the dir/dbase are legal because parse-args helps there
      (= cmd "init") (do-init/init dir dbase more)
      (= cmd "hash-object") (println (do-hash/hash-object dir dbase more))
      (= cmd "cat-file") (do-cat/cat-file dir dbase more)
      (= cmd "commit-tree") (do-ctree/commit-tree dir dbase more)
      (= cmd "write-wtree") (do-wtree/write-wtree dir dbase more)
      (= cmd "rev-parse") (do-rev-parse/rev-parse dir dbase more)
      (= cmd "switch") (do-switch/switch dir dbase more)
      (= cmd "branch") (do-branch/branch dir dbase more)
      (= cmd "commit") (do-commit/commit dir dbase more)
      (= cmd "rev-list") (do-rev-list/rev-list dir dbase more)
      (= cmd "explore") (do-explore/explore dir dbase more);
      :else (println hmsg/top-h-message))))
