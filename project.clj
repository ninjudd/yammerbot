(defproject yammerbot "0.0.1-SNAPSHOT"
  :description "Yammer irc bot."
  :main yammerbot
  :omit-source true
  :dependencies [[clojure "1.2.0"]
                 [clojure-useful "0.2.8"]
                 [pircbot/pircbot "1.4.2"]
                 [clj-http "0.1.1"]
                 [clj-json "0.3.0-SNAPSHOT"]
                 [clj-oauth "1.2.2"]])
