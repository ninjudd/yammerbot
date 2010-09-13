(ns yammerbot
  (:use [useful :only [cond-let parse-opts invoke-private]])
  (:require [clj-http.client :as http]
            [clj-json.core :as json]
            [oauth.client :as oauth])
  (:import [org.jibble.pircbot PircBot]
           [java.io File])
  (:gen-class))

(def consumer
  (oauth/make-consumer "WkITiqyL4yzZgEfh1Ez6w"
                       "P6BuoIaxeqtyI7pKZfhi2ldbwvcc7RuMcAjXEbQbyYo"
                       "https://www.yammer.com/oauth/request_token"
                       "https://www.yammer.com/oauth/access_token"
                       "https://www.yammer.com/oauth/authorize"
                       :plaintext))

(defmacro mkdir [& args]
  `(doto (File. ~@args)
     (.mkdirs)))

(def conf-dir   (mkdir (System/getProperty "user.home") ".yammerbot"))
(def access-dir (mkdir conf-dir "access-tokens"))

(def request-tokens (atom {}))
(def access-tokens
  (atom
   (reduce
    (fn [m file]
      (assoc m (.getName file) (read-string (slurp file))))
    {} (rest (file-seq access-dir)))))

(defn signed-params [access-token method url params]
  (merge params
         (oauth/credentials consumer
           (:oauth_token access-token)
           (:oauth_token_secret access-token)
           method url params)))

(def http-method
  {:GET    http/get
   :POST   http/post
   :PUT    http/put
   :DELETE http/delete})

(defn http [method resource params access-token]
  (let [url      (str "https://www.yammer.com/api/v1/" resource ".json")
        params   (signed-params access-token method url params)
        response ((http-method method) url {:query-params params})]
    (json/parse-string (:body response))))

(defn authorize [bot channel sender login]
  (let [request-token (oauth/request-token consumer)
        approval-uri  (oauth/user-approval-uri consumer (:oauth_token request-token))]
    (.sendMessage bot channel (str sender ": please click here to authorize your yammer account " approval-uri))
    (swap! request-tokens assoc login request-token)))

(defn approve [bot channel sender login request-token verifier]
  (println verifier)
  (let [access-token (oauth/access-token consumer request-token verifier)]
    (swap! request-tokens dissoc login)
    (swap! access-tokens assoc login access-token)
    (spit (File. access-dir login) (prn-str access-token))))

(def group-ids (atom {}))

(defn group-id [name access-token]
  (or (@group-ids name)
      (let [results (http :GET "autocomplete" {:prefix name} access-token)
            group   (or (first (filter #(= name (% "name")) (results "groups"))) {})]
        (when-let [group-id (group "id")]
          (swap! group-ids assoc name group-id)
          group-id))))

(defn create-group [name access-token]
  (http :POST "groups" {:name name} access-token)
  (group-id name access-token))

(defn forward-message [channel message access-token]
  (let [group    (str (.substring channel 1) "-irc")
        group-id (or (group-id group access-token) (create-group group access-token))]
    (http :POST "messages" {:group_id group-id :body message} access-token)))

(defn on-message [bot channel sender login hostname message]
  (cond-let [token (@request-tokens login)] (approve bot channel sender login token message)
            [token (@access-tokens login)]  (forward-message channel message token)
            :else (authorize bot channel sender login)))

(defn join-channels [bot channels]
  (doseq [channel channels :let [[channel key] (.split channel ":")]]
    (if key
      (.joinChannel bot channel key)
      (.joinChannel bot channel))))

(defn run-bot [server port password channels]
  (doto (proxy [PircBot] []
          (onMessage [channel sender login hostname message]
            (on-message this channel sender login hostname message)))
    (invoke-private "setName" "yammerbot")
    (.connect server port password)
    (join-channels channels)))

(defn -main [& args]
  (let [opts (parse-opts :yammerbot args)
        [server & channels] (:yammerbot opts)
        [server port] (.split server ":")
        port (Integer/parseInt (or port "6667"))
        password (or (first (or (:password opts) (:p opts))) "")]
    (run-bot server port password channels)))
