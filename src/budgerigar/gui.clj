(ns budgerigar.gui
  (:import [java.awt Color Dimension Toolkit]
           [javax.swing JFrame JPanel JMenu JMenuItem JMenuBar Timer ImageIcon]
           [java.awt.event ActionListener MouseListener MouseMotionListener])
  (:require [clojure.java.io :as io]))

(defn gui-panel []
  (let [width 1080 height 810]
    (proxy [JPanel ActionListener MouseListener MouseMotionListener] []
      (paintComponent [g])
      (actionPerformed [e])
      (mouseEntered [e])
      (mouseMoved [e])
      (mouseClicked [e])
      (mousePressed [e])
      (mouseDragged [e])
      (mouseReleased [e])
      (mouseExited [e])
      (getPreferredSize []
        (Dimension. width height)))))

(defn make-menubar []
  (let [menubar (JMenuBar.)]
    menubar))

(defn set-gui [fps]
  (let [frame (JFrame. "Budgerigar - my versatile bulletin")
        panel (gui-panel)
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
