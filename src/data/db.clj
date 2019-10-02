(ns data.db
  (:require [clojure.java.jdbc :as jdbc]))

(def db-spec
  {:subprotocol "sqlite",
   :subname "target/test_db.sqlite"})


; (def db-spec
;   {:connection-uri "jdbc:sqlite:target/test_db.sqlite"})

; (def db-spec
;   {:classname   "org.sqlite.JDBC"
;    :subprotocol "sqlite"
;    :subname     ":memory:"})
