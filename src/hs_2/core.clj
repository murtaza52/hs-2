(ns hs-2.core
  (:use [clj-time.core :only [now interval within? plus minus secs]])
  (:require [clj-http.client :as client]))

(comment

  (defn to-throttle?
  [url]
  ())

(defn fetch-url [url]
  ;if (to-throttle? url)
  (do
    ;(sleep 5s)
    ;(request url)
    )
  ;(request)
  )
)

(defn in-last-5?
  "Returns true if the given-time is between the start-time and the end-time"
  [given-time]
  (let [start-time (minus (now) (secs 5))
        end-time (now)]
    (within?
     (interval start-time end-time) given-time)))

(def first-time (now))
(def second-time (plus (now) (secs 2)))
(def third-time (plus (now) (secs 6)))

(def domain { "invoize" [second-time first-time]
              "github" [third-time first-time]
             })

(defn request-in-last-5?
  "Returns true if the domain has been requested atleast twice in last 5 secs"
  [domain-name]
  (let [second-last-req-time (-> domain (get domain-name) (nth 1))]
    (in-last-5? (now))))

(defn is-html?
  [resp]
  (let [content-type (-> resp :headers (get "content-type"))]
    (re-find #"text/html" content-type)))

(defn get-body
  [url]
  (-> (client/get url) :body))

(defn request-url
  [url]
  (let [resp (client/get url)
        body (:body resp)
        html? (is-html? resp)]
    (when html? true)))
