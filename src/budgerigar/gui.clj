(ns budgerigar.gui
  (:import [java.awt Color Dimension Font Graphics2D RenderingHints Toolkit]
           [java.awt.font TextAttribute LineBreakMeasurer]
           [javax.swing JFrame JPanel JMenu JMenuItem JMenuBar JTextArea Timer ImageIcon]
           [java.awt.event ActionListener MouseListener MouseMotionListener]
           [java.text AttributedString])
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [budgerigar.server :as s]))

(defn- next-layout [line-measurer s position wrapping-width]
  (let [br-index (str/index-of s \newline (inc position))]
    (if (or (nil? br-index) (<= br-index position))
      (.nextLayout line-measurer (float wrapping-width))
      (.nextLayout line-measurer (float wrapping-width) br-index false))))

(defn- draw-string-in-rect
  "draw string inserting break-lines automatically"
  [g s x y w h]
  (let [astr (AttributedString. s)
        _ (doto astr
            (.addAttribute TextAttribute/FONT (.getFont g))
            (.addAttribute TextAttribute/FOREGROUND (.getColor g))
            (.addAttribute TextAttribute/BACKGROUND (s/make-color 0 0 0 0)))
        ac-iter (.getIterator astr)
        ctx (.getFontRenderContext g)
        line-measurer (LineBreakMeasurer. ac-iter ctx)
        begin-index (.getBeginIndex ac-iter)
        end-index (.getEndIndex ac-iter)
        _ (.setPosition line-measurer begin-index)]
    (loop [draw-x x draw-y y pos (.getPosition line-measurer)]
      (if (< pos end-index)
        (let [layout (next-layout line-measurer s pos w)]
          (if (< (+ draw-y (.getAscent layout) (.getDescent layout) (.getLeading layout)) (+ y h))
            (do (.draw layout g draw-x (+ draw-y (.getAscent layout)))
              (recur draw-x (+ draw-y (.getAscent layout) (.getDescent layout) (.getLeading layout)) (.getPosition line-measurer)))))))))

(defn- fill-rectangle-from-map [g mp width height]
  (if (map? mp)
    (let [x (:x mp) y (:y mp) w (:width mp) h (:height mp) frame-w (:frame-width mp)
          color (:color mp) body-color (:body color) frame-color (:frame color) char-color (:character color)
          body (:body mp) fade (:fade mp) font (:font mp) fontsize (:font-size mp)]
      (doto g
        (.setColor frame-color)
        (.fillRect x y w h)
        (.setColor body-color)
        (.fillRect (+ x frame-w) (+ y frame-w) (- w (bit-shift-left frame-w 1)) (- h (bit-shift-left frame-w 1)))
        (.setColor char-color)
        (.setFont (Font. font Font/PLAIN fontsize))
        (draw-string-in-rect body (+ x frame-w) (+ y frame-w) (- w (bit-shift-left frame-w 1)) (- h (bit-shift-left frame-w 1)))))))

(defn fade-action [mp]
  (if (map? mp)
    (let [color (:color mp) fade (:fade mp) alpha (-> (:alpha mp) (- fade))
          colors [(:body color) (:frame color) (:character color)]]
      (if (> alpha 0)
        (->> colors
          (map #(s/make-color (.getRed %) (.getGreen %) (.getBlue %) (int alpha)))
          (zipmap [:body :frame :character])
          (assoc mp :alpha alpha :color))))))

(defn- mouse-on [mouse x y w h]
  (and
    (< x (.getX mouse) (+ x w))
    (< y (.getY mouse) (+ y h))))

(defn- mouse-on-message [mp mouse-event]
  (if (map? mp)
    (if (:on-mouse mp)
      (if (mouse-on mouse-event (:x mp) (:y mp) (:width mp) (:height mp))
        mp
        (assoc mp :on-mouse false))
      (if (mouse-on mouse-event (:x mp) (:y mp) (:width mp) (:height mp))
        (assoc mp :on-mouse true :alpha 255)
        mp))))

(defn gui-panel [reader]
  (let [width 1024 height 768 messages (atom []) background-image (-> (Toolkit/getDefaultToolkit) (.getImage (io/resource "background.png")))]
    (s/painter-channel reader messages width height)
    (proxy [JPanel ActionListener MouseListener MouseMotionListener] []
      (paintComponent [g]
        (let [g2 (cast Graphics2D g)]
          (doto g2
            (.setRenderingHint RenderingHints/KEY_TEXT_ANTIALIASING RenderingHints/VALUE_TEXT_ANTIALIAS_GASP)
            (.drawImage background-image 0 0 width height this))
          (doall (map #(fill-rectangle-from-map g2 % width height) @messages))))
      (actionPerformed [e]
        (reset! messages (vec (keep fade-action @messages)))
        (.repaint this))
      (mouseEntered [e])
      (mouseMoved [e]
        (reset! messages (vec (sort-by :alpha (map #(mouse-on-message % e) @messages)))))
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
