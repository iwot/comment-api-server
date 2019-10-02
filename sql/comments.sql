-- :name create-comments-table
-- :command :execute
-- :result :raw
-- :doc Create comments table
create table comments (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  page_id TEXT,
  name TEXT,
  mail TEXT,
  comment TEXT,
  ip TEXT,
  approved INTEGER DEFAULT 0,
  created_at DATETIME DEFAULT (datetime('now', 'localtime'))
)

-- A :result value of :n below will return affected rows:
-- :name insert-comment :! :n
-- :doc Insert a single comment returning affected row count
insert into comments (page_id, name, mail, comment, ip)
values (:page_id, :name, :mail, :comment, :ip)

-- :name comments-by-page :? :*
-- :doc Comments
select *
  from comments
 where page_id = :page_id and approved = 1
 order by id desc
 limit 100 offset 0

-- :name not-approved-comments :? :*
-- :doc Comments
select *
  from comments
 where approved = 0

