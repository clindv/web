(ns web.core
  (:use [ring.adapter.jetty :only (run-jetty)]
        [ring.util.response :refer :all]
        [ring.middleware.session :refer :all]
        [ring.middleware.resource :refer :all]
        [ring.middleware.params :refer :all]
        [ring.middleware.multipart-params :refer :all]
        [ring.middleware.not-modified :refer :all]
        [ring.middleware.content-type :refer :all]
        [ring.middleware.cookies :refer :all]))
;;curl -XPOST "http://127.0.0.1:3000" -F file=@install-mac.txt
(defn- num-lines
  [file]
  (with-open [rdr (clojure.java.io/reader file)]
    (count (line-seq rdr))))
(defn file-handler
  [{{{tempfile :tempfile filename :filename} "file"} :params :as request}]
  (let [n (num-lines tempfile)]
    (response (str "File " filename " has " n " lines "))))
(defn -main
  [& args]
  (run-jetty (-> file-handler
                       wrap-params
                       wrap-multipart-params)
             {:port 3001}))
;;session
(defn handler [{session :session}]
  (let [count   (:count session 0)
        session (assoc session :count (inc count))]
    (-> (response (str "You accessed this page " count " times."))
        (assoc :session session))))
;;response twice
(defn page [name]
  (str "<html><body>"
       (if name
         (str "Nice to meet you, " name "!")
         (str "<form>"
              "Name: <input name='name' type='text'>"
              "<input type='submit'>"
              "</form>"))
       "</body></html>"))
(defn handler [{{name "name"} :params}]
  (-> (response (page name))
      (content-type "text/html")))
(def app
  (-> handler wrap-params))
(defonce server
  (run-jetty #'app {:port 3000 :join? false}))
