(ns server.core
  (:require [bell.core :refer [ANY GET POST router]]
            [cheshire.core :as json]
            [ring.adapter.jetty9 :as jetty]
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
        (jetty/send! ws message))))
  (resp/created ""))

(defn ws-handler [upgrade-request]
  (let [provided-subprotocols (:websocket-subprotocols upgrade-request)
        provided-extensions (:websocket-extensions upgrade-request)]
    {:on-connect (fn [ws] (swap! ws-clients conj ws))
     :on-close (fn [ws _status-code _reason] (swap! ws-clients disj ws))
     :on-ping (fn [ws payload] (jetty/send! ws payload))
     :on-error (fn [ws _e] (swap! ws-clients disj ws))
     :subprotocol (first provided-subprotocols)
     :extentions provided-extensions}))

(defn ws-upgrade-handler [request]
  (when (jetty/ws-upgrade-request? request)
    (jetty/ws-upgrade-response ws-handler)))

(def handler
  (router
   (-> (GET "/" (fn [_] (-> (resp/resource-response "public/index.html")
                            (resp/content-type "text/html; charset=utf-8"))))
       (wrap-resource "public")
       (wrap-content-type)
       (wrap-not-modified))

   (-> (POST "/api/messages" post-messages-handler)
       (wrap-json-body {:keywords? true}))

   (ANY "/ws" ws-upgrade-handler)))

(defn -main [& _args]
  (jetty/run-jetty handler {:port 8080}))