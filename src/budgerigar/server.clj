(ns budgerigar.server
  (:import [org.newsclub.net.unix AFUNIXSocket AFUNIXServerSocket AFUNIXSocketAddress]
           [java.io PrintWriter InputStreamReader BufferedReader])
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

(defn printer-channel
  [c socket]
  (let [out (-> socket .getOutputStream (PrintWriter. true))]
    (go-loop [line (<! c)]
      (println line)
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
