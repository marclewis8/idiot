(ns commands.utils.help-docs)


;;;;; help messages ;;;;;

(def top-h-message
  "idiot: the other stupid content tracker

Usage: idiot [<top-args>] <command> [<args>]

Top-level arguments:
   -r <dir>   run from the given directory instead of the current one
   -d <dir>   store the database in <dir> (default: .idiot)

Commands:
   branch [-d <branch>]
   cat-file {-p|-t} <address>
   commit <tree> -m \"message\" [(-p parent)...]
   commit-tree <tree> -m \"message\" [(-p parent)...]
   hash-object [-w] <file>
   help
   init
   rev-parse <ref>
   switch [-c] <branch>
   write-wtree")

(def help-h-message
  "idiot help: print help for a command

Usage: idiot help <command>

Arguments:
   <command>   the command to print help for

Commands:
   branch [-d <branch>]
   cat-file {-p|-t} <address>
   commit <tree> -m \"message\" [(-p parent)...]
   commit-tree <tree> -m \"message\" [(-p parent)...]
   hash-object [-w] <file>
   help
   init
   rev-parse <ref>
   switch [-c] <branch>
   write-wtree")

(def init-h-message
  "idiot init: initialize a new database

Usage: idiot init

Arguments:
   -h   print this message")

(def hash-h-message
  "idiot hash-object: compute address and maybe create blob from file

Usage: idiot hash-object [-w] <file>

Arguments:
   -h       print this message
   -w       write the file to database as a blob object
   <file>   the file")

(def cat-h-message
  "idiot cat-file: print information about an object

Usage: idiot cat-file {-p|-t} <address>

Arguments:
   -h          print this message
   -p          pretty-print contents based on object type
   -t          print the type of the given object
   <address>   the SHA1-based address of the object")

(def write-h-message
  "idiot write-wtree: write the working tree to the database

Usage: idiot write-wtree

Arguments:
   -h       print this message")

(def commit-tree-h-message
  "idiot commit-tree: write a commit object based on the given tree

Usage: idiot commit-tree <tree> -m \"message\" [(-p parent)...]

Arguments:
   -h               print this message
   <tree>           the address of the tree object to commit
   -m \"<message>\"   the commit message
   -p <parent>      the address of a parent commit")

(def rev-parse-h-message
  "idiot rev-parse: determine which commit a ref points to

Usage: idiot rev-parse <ref>

<ref> can be:
- a branch name, like 'master'
- literally 'HEAD'
- literally '@', an alias for 'HEAD'")

(def switch-h-message
  "idiot switch: change what HEAD points to

Usage: idiot switch [-c] <branch>

Arguments:
   -c   create the branch before switching to it")

(def branch-h-message
  "idiot branch: list or delete branches

Usage: idiot branch [-d <branch>]

Arguments:
   -d <branch>   delete branch <branch>")

(def commit-h-message
  "idiot commit: create a commit and advance the current branch

Usage: idiot commit <tree> -m \"message\" [(-p parent)...]

Arguments:
   -h               print this message
   <tree>           the address of the tree object to commit
   -m \"<message>\"   the commit message
   -p <parent>      the address of a parent commit")
