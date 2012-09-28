(ns hs-2.routes
  (:use [net.cgrand.moustache :only [app pass]]
        [net.cgrand.enlive-html :only [deftemplate content html-content defsnippet clone-for after]]
        [ring.middleware.file :only [wrap-file]]
        [ring.middleware.stacktrace :only [wrap-stacktrace]]
        [ring.middleware.file-info :only [wrap-file-info]]
        [ring.middleware.params :only [wrap-params]]
        [ring.middleware.keyword-params :only [wrap-keyword-params]]
        [ring.util.response :only [response status redirect]]
        [clojure.pprint :only [pprint]]
        [hs-2.core :only [process-request]]
        [clj-airbrake.core :only [notify]]
        [clj-airbrake.ring :only [request-to-message]]))

(def prev-h (atom nil))

(defn wrap-logger
  [app flag]
  (fn [req]
    (when flag (do (println "******************************************** Request Map ***********************************************")
                   (println req)
                   (println "******************************************** Response Map ***********************************************")))
    (app req)))

(defsnippet toolbar "templates/toolbar.html" [:#toolbar]
  [])

(defsnippet message "templates/message.html" [:#error_box]
  [msgs]
  [:#error_content] (content msgs))

(defsnippet form "templates/form.html" [:#url_form]
  [])

(defsnippet table "templates/table.html" [:#headings_table]
  [headings]
  [:tbody :tr] (clone-for [h headings]
                          [:.num] (content (str (nth h 0)))
                          [:.heading](content (name (nth h 2)))
                          [:.text](content (nth h 1))))

(deftemplate index-page "templates/index.html"
  [headings errors]
  [:#toolbar_block] (content (toolbar))
  [:#message_block] (content (if errors (message errors) nil))
  [:#form_block] (content (form))
  [:#tables_block] (content (table headings)))

(defn index-route
  [headings errors]
  (fn [req]
    (-> (index-page headings errors) response)))

(defn wrap-exception
  [app]
  (fn [req]
     (try (app req)
          (catch Exception e
            (do (.printStackTrace e)
                (notify "571bda5bb0d6ee4595b0432339a151fc"
                        "production"
                        (System/getProperty "user.dir")
                        e
                        (request-to-message req))
                (-> "Error Man" response (status 500)))))))

(defn fetch-url
  []
  (fn [req]
    (let [url (-> req :params (get "url"))
          table-data ((process-request) {:url url})
          errors (:error-msg table-data)]
      (reset! prev-h table-data)
      (-> (index-page table-data errors) response))))

(def my-app (app
             wrap-exception
             (wrap-logger true)
             wrap-keyword-params
             wrap-params
             wrap-file-info
             (wrap-file "resources/public/")
             [""]  (index-route @prev-h nil)
             ["getContent"] (fetch-url)
             ["about"] "We are freaking cool man !!"
             [&] (-> "Nothing was found" response (status 404) constantly)))
