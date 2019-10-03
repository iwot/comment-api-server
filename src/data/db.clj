(ns data.db
  (:require [clojure.java.jdbc :as jdbc]))

;; db-spec の書き方は下記URLを参考にしてください。
;; https://github.com/clojure/java.jdbc の Example Usage
;; http://clojure.github.io/java.jdbc/#clojure.java.jdbc/get-connection

(defn db-spec
  [config]
  (:db-spec config))
