(ns hs-2.core
  (:use [clj-time.core :only [now interval within? plus minus secs]]
        [net.cgrand.enlive-html :only [deftemplate content html-content defsnippet clone-for html-resource]]
        [clojure.string :only [lower-case]])
  (:require [clj-http.client :as client]
            [clojurewerkz.urly.core :as urly]
            [net.cgrand.enlive-html :as en]))

(def all-headings (atom {}))

(def visited-domains (atom {}))

(def prohibited-domains (atom #{"prohib.com"}))

(defn wrap-check-prohibited-domains
  [app]
  (fn [{:keys [domain-name] :as req}]
    (if-let [prohibited? (@prohibited-domains domain-name)]
      (assoc req :error-msg "Beware!! Clojure on Guard ! Thats a prohibited domain.")
      (app req))))

(defn defdomain
  [domain-name]
  (swap! prohibited-domains #(conj % domain-name)))

(defdomain "prohib2.com")

(defn in-last-5?
  "Returns true if the given-time is between the start-time and the end-time"
  [given-time]
  (let [start-time (minus (now) (secs 5))
        end-time (now)]
    (within?
     (interval start-time end-time) given-time)))

(defn wrap-throttle-req
  "Returns true if the domain has been requested atleast twice in last 5 secs"
  [app]
  (fn [{:keys [domain-name] :as req}]
    (if-let [second-last-req-time (-> @visited-domains (get domain-name) (nth 1 nil))]
      (when (in-last-5? (first second-last-req-time))
        ;(Thread/sleep 5000)
        (app req))
      (app req))))

(defn wrap-get-domain
  [app]
  (fn [{:keys [url] :as req}]
    (let [domain (urly/host-of url)]
      (app (assoc req :domain-name domain)))))

(defn is-html?
  [resp]
  (let [content-type (-> resp :headers (get "content-type"))]
    (re-find #"text/html" content-type)))

(defn wrap-http-scheme
  [app]
  (fn [{:keys [url] :as req}]
    (if-let [scheme (#{"http" "https"} (urly/protocol-of url))]
      (app (assoc req :url-scheme scheme))
      (assoc req :url-scheme nil :error-msg "The URL is not of http scheme. The URL should start either with http or https."))))

(defn request-url
  [url]
  (let [resp (client/get url)
        body (:body resp)
        html? (is-html? resp)]
    (when html?
      (html-resource body))))

(defn wrap-add-request-time
  [app]
  (fn [{:keys [domain-name] :as req}]
    (let [new-value (merge-with conj @visited-domains {domain-name [(now)]})]
          (reset! visited-domains new-value)
          (app req))))

(defn wrap-get-html
  [app]
  (fn [{:keys [url] :as req}]
    (let [resp (client/get url)
          body (:body resp)
          html? (is-html? resp)]
      (if html?
        (-> req (assoc :html-body body) (assoc :is-html? html?) app)
        (-> req (assoc :is-html? html?) (assoc :error-msg "The content-type of the response is not HTML"))))))

(defn get-selector-func
  [selector]
  (fn [[headings, data]]
    (let [nodes (en/select data [selector])
          text-col (map en/text nodes)]
      [(assoc headings selector text-col), data])))

(def heading-selectors (map get-selector-func [:h1 :h2 :h3 :h4 :h5 :h6]))

(defn get-headings
  [html-body]
  (let [data (en/html-snippet html-body)]
    (reduce (fn [v f] (f v)) [{} data] heading-selectors)))

(defn wrap-get-headings
  [app]
  (fn [{:keys [html-body] :as req}]
    (let [headings (first (get-headings html-body))]
      (app (-> req (assoc :headings headings) (dissoc :html-body))))))

(defn counter
  [h-map [heading heading-text]]
  (let [vowel-count (map (fn [text]
                           (let [text-seq (seq (char-array (lower-case text)))
                                 heading-vowels (filter char? (map #{\a \e \i \o \u} text-seq))]
                             (count heading-vowels)))
                         heading-text)]
    (assoc h-map heading (map (fn [count text] [count text])
                              vowel-count
                              heading-text))))

(defn wrap-count-vowels
  [app]
  (fn [{:keys [headings] :as req}]
    (let [headings-with-count (reduce counter {} headings)]
      (app (assoc req :headings headings-with-count)))))

(defn wrap-save-headings
  [app]
  (fn [{:keys [headings] :as req}]
    (swap! all-headings #(merge-with concat % headings))
    (app @all-headings)))

(defn sort-headers
  [headings]
  (let [h-vec (map identity headings)
        h-with-headers (mapcat (fn [[k v]]
                                 (map #(conj % k) v))
                               h-vec)
        sorted-h (sort-by #(first %) h-with-headers)]
    (reverse sorted-h)))

(defn get-all-headings
  []
  (sort-headers @all-headings))

(defn wrap-sort-headers
  [app]
  (fn [all-headings]
    (app (sort-headers all-headings))))

(defn process-request
  []
  (-> identity
      wrap-sort-headers
      wrap-save-headings
      wrap-count-vowels
      wrap-get-headings
      wrap-get-html
      wrap-add-request-time
      wrap-throttle-req
      wrap-check-prohibited-domains
      wrap-get-domain
      wrap-http-scheme))