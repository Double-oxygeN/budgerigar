(ns budgerigar.core
  (:require [clojure.core.async :as async :refer [<! >! <!! chan go-loop]]
            [budgerigar.server :as s]
            [budgerigar.gui :as gui])
  (:gen-class))

(defn -main
  [& args]
  (case (first args)
    "test" (with-open [client (s/test-client s/budgie-socket-file)]
             (let [c (chan)]
               (s/printer-channel c client)
               (s/writer-channel c)
               (loop [] (recur))))
    (with-open [server (s/make-server s/budgie-socket-file)]
      (let [gatekeeper (chan) reader (chan)]
        (s/gatekeeper-channel gatekeeper server)
        (go-loop [client (<! gatekeeper)] (s/reader-channel reader client) (recur (<! gatekeeper)))
        (gui/set-gui 30)
        (loop [line (<!! reader)] (println line) (recur (<!! reader)))))))
