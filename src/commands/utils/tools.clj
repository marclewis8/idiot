(ns commands.utils.tools
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.pprint :as pprint])
  (:import java.security.MessageDigest
           java.io.File
           java.util.zip.InflaterInputStream
           java.util.zip.DeflaterOutputStream
           (java.io ByteArrayInputStream ByteArrayOutputStream)))

;;;;; cat utilities ;;;;;


(defn unzip
  "Unzip the given data with zlib. Pass an opened input stream as the arg. The
  caller should close the stream afterwards."
  [input-stream]
  (with-open [unzipper (InflaterInputStream. input-stream)
              out (ByteArrayOutputStream.)]
    (io/copy unzipper out)
    (->> (.toByteArray out)
         (map char)
         (apply str))))


;;;;; hash utilities ;;;;;


(defn sha1-hash-bytes [data]
  (.digest (MessageDigest/getInstance "sha1")
           (.getBytes data)))

(defn byte->hex-digits [byte]
  (format "%02x"
          (bit-and 0xff byte)))

(defn bytes->hex-string [bytes]
  (->> bytes
       (map byte->hex-digits)
       (apply str)))

(defn sha1-sum [header+blob]
  (bytes->hex-string (sha1-hash-bytes header+blob)))

(defn zip-str
  "Zip the given data with zlib. Return a ByteArrayInputStream of the zipped
  content."
  [data]
  (let [out (ByteArrayOutputStream.)
        zipper (DeflaterOutputStream. out)]
    (io/copy data zipper)
    (.close zipper)
    (ByteArrayInputStream. (.toByteArray out))))


;;;;; write-wtree utilities ;;;;;


(defn sha-bytes [bytes]
  (.digest (MessageDigest/getInstance "sha1") bytes))

(defn to-hex-string
  "Convert the given byte array into a hex string, 2 characters per byte."
  [bytes]
  (letfn [(to-hex [byte]
            (format "%02x" (bit-and 0xff byte)))]
    (->> bytes (map to-hex) (apply str))))

(defn hex-digits->byte
  [[dig1 dig2]]
  ;; This is tricky because something like "ab" is "out of range" for a
  ;; Byte, because Bytes are signed and can only be between -128 and 127
  ;; (inclusive). So we have to temporarily use an int to give us the room
  ;; we need, then adjust the value if needed to get it in the range for a
  ;; byte, and finally cast to a byte.
  (let [i (Integer/parseInt (str dig1 dig2) 16)
        byte-ready-int (if (< Byte/MAX_VALUE i)
                         (byte (- i 256))
                         i)]
    (byte byte-ready-int)))

(defn from-hex-string
  [hex-str]
  (byte-array (map hex-digits->byte (partition 2 hex-str))))

;;;;; cat-file extension utilities ;;;;;

(defn split-at-byte [b bytes]
  (let [part1 (take-while (partial not= b) bytes)
        part2 (nthrest bytes (-> part1 count inc))]
    [part1 part2]))

(defn byte-unzip
  "Unzip the given file's contents with zlib."
  [path]
  (with-open [input (-> path io/file io/input-stream)
              unzipper (InflaterInputStream. input)
              out (ByteArrayOutputStream.)]
    (io/copy unzipper out)
    (.toByteArray out)))

(defn to-string [byte-seq]
  (clojure.string/join (map char byte-seq)))

(defn find-type [input]
  (to-string (first (split-at-byte 32 input))))

; this function takes in the working directory and database (which are both assumed to exist), as well as the abbreviated address, and returns
; 1. the full address, if any, or
; 2. NIL if it doesn't work. maybe will pass the error string based on what went wrong. can return more if utility demands it

(defn abbrev-to-full-hash [dir dbase abbrev]
  (cond
    (>= (count abbrev) 40) (println "That's not a valid address")
    (< (count abbrev) 4) (println "Not enough characters specified")
    :else (let [dirname (subs abbrev 0 2)
                fname (subs abbrev 2)
                targetdir (str dir File/separator dbase File/separator "objects" File/separator dirname)
                targetfileabbrev (str targetdir File/separator fname)]
            (if (not (.isDirectory (io/file targetdir)))
              (println "Error: that address doesn't exist as a dir") ; checking for directory existence
              (let [all-files (map str (.list (io/file targetdir)))
                    matches (filter (fn [x] (= (subs x 0 (count fname)) fname)) all-files)]
                ;(pprint/pprint all-files)
                ;(println matches)
                (case (count matches)
                  0 "Error: that address doesn't exist as a file"
                  1 (str dirname (first matches)) ; returns the entire file name
                  "Error: ambiguous" ; equiv to else, catches all other nums
                  ))))))
