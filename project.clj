(defproject hs-2 "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [net.cgrand/moustache "1.1.0" :exclusions [org.clojure/clojure]]
                 [enlive "1.0.1" :exclusions [org.clojure/clojure]]
                 [ring "1.1.5"]
                 [clj-time "0.4.4"]
                 [clj-http "0.5.3"]
                 [org.thnetos/cd-client "0.3.4" :exclusions [[org.clojure/clojure] cheshire]]
                 [clojurewerkz/urly "1.0.0"]
                 [org.bovinegenius/exploding-fish "0.3.0"]]
  :profiles {:dev {:dependencies [[ring-serve "0.1.2"]
                                  [ring-mock "0.1.3"]
                                  ;[clj-ns-browser "1.3.0"]
                                  [ritz/ritz-debugger "0.4.2"]
                                  [ritz/ritz-repl-utils "0.4.2"]
                                  [midje "1.4.0" :exclusions [org.clojure/clojure]
                                   [lein-midje "1.0.10"]]]}}
  :ring {:handler hs-2.routes/my-app}
  :repl-options {:init-ns hs-2.core
                 :init (do
                         ;(use 'hs-2.core)
                         ;(use 'ring.util.serve)
                         ;(serve 'hs_2.routes/my-app)
                         ;(use 'ring.mock.request)
                         )}
  :resource-paths ["resources/public"]
  :pedantic :warn)
