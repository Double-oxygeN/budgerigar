(defproject budgerigar "0.1.0"
  :description "BUDGERIGAR: my versatile bulletin"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [com.kohlschutter.junixsocket/junixsocket-native-common "2.0.4"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/core.async "0.2.395"]
                 [com.taoensso/timbre "4.7.0"]]
  :main ^:skip-aot budgerigar.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
