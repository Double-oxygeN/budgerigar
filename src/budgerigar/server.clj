(ns budgerigar.server
  (:import [org.newsclub.net.unix AFUNIXSocket AFUNIXServerSocket AFUNIXSocketAddress]
           [java.io PrintWriter InputStreamReader BufferedReader]
           [java.awt Color])
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.core.async :as async :refer [>! <! go go-loop]]
            [taoensso.timbre :as tim]))

(def budgie-socket-file "/var/tmp/budgerigar.socket")

(defn make-server [socket-file-src]
  (let [server (AFUNIXServerSocket/newInstance)]
    (->> socket-file-src
      io/file
      AFUNIXSocketAddress.
      (.bind server))
    (tim/info "created server")
    server))

(defn gatekeeper-channel
  "set channel as a gatekeeper watching and accepting arrival of new client"
  [c server]
  (go-loop [client (.accept server) id 12000]
    (let [in (-> client .getInputStream InputStreamReader. BufferedReader.)
          out (-> client .getOutputStream (PrintWriter. true))]
      (>! c {:client client :id id :input in :output out})
      (tim/info "accepting client No." id)
      (recur (.accept server) (inc id)))))

(defn reader-channel
  "set channel as a reader of what a client sends"
  [c client]
  (go-loop [line (.readLine (:input client))]
    (tim/info line " from client No." (:id client))
    (>! c line)
    (recur (.readLine (:input client)))))

(defn- make-color [r g b]
  (Color. r g b))

(defn painter-channel [c mes w h]
  (go-loop [line (<! c)]
    (let [new-element (json/read-str line :key-fn keyword)]
      (->> (assoc new-element :x (or (:x new-element) (rand-int (bit-shift-right w 1)))
                              :y (or (:y new-element) (rand-int (bit-shift-right h 1)))
                              :width (or (:width new-element) (rand-int (bit-shift-right w 1)))
                              :height (or (:height new-element) (rand-int (bit-shift-right h 1)))
                              :color (apply make-color (or (:color new-element) [255 255 255])))
        (swap! mes conj)))
    (recur (<! c))))

;; for testing (below)

(defn printer-channel
  [c socket]
  (let [out (-> socket .getOutputStream (PrintWriter. true))]
    (go-loop [line (<! c)]
      (.println out line)
      (recur (<! c)))))

(defn writer-channel
  [c]
  (go-loop [line (read-line)]
    (>! c line)
    (recur (read-line))))

(defn test-client [socket-file-src]
  (let [client (AFUNIXSocket/newInstance)]
    (->> socket-file-src io/file AFUNIXSocketAddress. (.connect client))
    client))
