(ns server.core
  (:require [bell.core :refer [ANY GET POST router]]
            [cheshire.core :as json]
            [ring.adapter.undertow :refer [run-undertow]]
            [ring.adapter.undertow.websocket :as ws]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.json :refer [wrap-json-body]]
            [ring.middleware.not-modified :refer [wrap-not-modified]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.util.response :as resp])
  (:import [java.util UUID]))

(def ^:private ws-clients (atom #{}))

(defn post-messages-handler [request]
  (let [message (-> (select-keys (:body request) [:user :message])
                    (assoc :id (str (UUID/randomUUID)))
                    (json/generate-string))]
    (future
      (doseq [ws @ws-clients]
        (ws/send message ws))))
  (resp/created ""))

(defn ws-handler [_request]
  {:undertow/websocket
   {:on-open #(swap! ws-clients conj (:channel %))
    :on-close-message #(swap! ws-clients disj (:channel %))}})

(def handler
  (router
   (-> (GET "/" (fn [_] (-> (resp/resource-response "public/index.html")
                            (resp/content-type "text/html; charset=utf-8"))))
       (wrap-resource "public")
       (wrap-content-type)
       (wrap-not-modified))

   (-> (POST "/api/messages" post-messages-handler)
       (wrap-json-body {:keywords? true}))

   (ANY "/ws" ws-handler)))

(defn -main [& _args]
  (run-undertow handler {:port 8080}))