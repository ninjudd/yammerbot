(ns yammerbot
  (:use [useful :only [parse-opts]])
  (:require [irclj.irclj :as irc]
            [clj-http.client :as http])
  (:gen-class))

(defn on-message [{:keys [nick channel message irc]}]
  (println message))

(defn -main [& args]
  (let [opts     (parse-opts :yammerbot args)
        server   (first (or (:server opts) (:s opts)))
        channels (or (:join opts) (:j opts))
        password (str (first (or (:password opts) (:p opts))) "\n")
        bot      (irc/create-irc {:name "yammerbot" :server server :password password :fnmap {:on-message on-message}})] 
    (irc/connect bot :channels channels)
    (read-line)
    (irc/close bot)))

