(ns budgerigar.gui
  (:import [java.awt Color Dimension Toolkit]
           [javax.swing JFrame JPanel JMenu JMenuItem JMenuBar JTextArea Timer ImageIcon]
           [java.awt.event ActionListener MouseListener MouseMotionListener])
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [budgerigar.server :as s]))

(defn- fill-rectangle-from-map [g mp width height]
  (let [x (:x mp) y (:y mp) w (:width mp) h (:height mp) frame-w (:frame-width mp)
        color (:color mp) body-color (:body color) frame-color (:frame color) char-color (:character color)
        body (:body mp) fade (:fade mp)]
    (doto g
      (.setColor frame-color)
      (.fillRect x y w h)
      (.setColor body-color)
      (.fillRect (+ x frame-w) (+ y frame-w) (- w (bit-shift-left frame-w 1)) (- h (bit-shift-left frame-w 1)))
      (.setColor char-color)
      (.drawString body (+ x frame-w) (+ y 15 frame-w)))))

(defn gui-panel [reader]
  (let [width 1024 height 768 messages (atom []) background-image (-> (Toolkit/getDefaultToolkit) (.getImage (io/resource "background.png")))]
    (s/painter-channel reader messages width height)
    (proxy [JPanel ActionListener MouseListener MouseMotionListener] []
      (paintComponent [g]
        (doto g
          (.drawImage background-image 0 0 width height this))
        (doall (map #(fill-rectangle-from-map g % width height) @messages)))
      (actionPerformed [e]
        (.repaint this))
      (mouseEntered [e])
      (mouseMoved [e])
      (mouseClicked [e]
        (.add this (JTextArea. 20 20)))
      (mousePressed [e])
      (mouseDragged [e])
      (mouseReleased [e])
      (mouseExited [e])
      (getPreferredSize []
        (Dimension. width height)))))

(defn make-menubar []
  (let [menubar (JMenuBar.)]
    menubar))

(defn set-gui [fps reader]
  (let [frame (JFrame. "Budgerigar - my versatile bulletin")
        panel (gui-panel reader)
        menubar (make-menubar)
        timer (Timer. (int (/ 1000 fps)) panel)
        icon (-> (Toolkit/getDefaultToolkit) (.getImage (io/resource "icon.png")))]
    (System/setProperty "apple.laf.useScreenMenuBar" "true")
    (System/setProperty "com.apple.mrj.application.apple.menu.about.name" "Budgerigar")
    (doto panel
      (.setFocusable true)
      (.addMouseListener panel)
      (.addMouseMotionListener panel))
    (doto frame
      (.setDefaultCloseOperation javax.swing.WindowConstants/EXIT_ON_CLOSE)
      (.setResizable false)
      (.setIconImage icon)
      (.add panel)
      (.setJMenuBar menubar)
      .pack
      (.setVisible true))
    (.start timer)))
