(ns data.comments
  (:require [hugsql.core :as hugsql]))

(hugsql/def-db-fns "comments.sql")
