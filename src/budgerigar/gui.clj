(ns budgerigar.gui
  (:import [java.awt Color Dimension Toolkit]
           [javax.swing JFrame JPanel JMenu JMenuItem JMenuBar JTextArea Timer ImageIcon]
           [java.awt.event ActionListener MouseListener MouseMotionListener])
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [budgerigar.server :as s]))

(defn- fill-rectangle-from-map [g mp width height]
  (let [x (:x mp) y (:y mp) w (:width mp) h (:height mp) color (:color mp)]
    (doto g
      (.setColor color)
      (.fillRect x y w h))))

(defn gui-panel [reader]
  (let [width 1080 height 810 messages (atom [])]
    (s/painter-channel reader messages width height)
    (proxy [JPanel ActionListener MouseListener MouseMotionListener] []
      (paintComponent [g]
        (doto g
          (.setColor Color/black)
          (.fillRect 0 0 width height)
          (.setColor Color/green))
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
