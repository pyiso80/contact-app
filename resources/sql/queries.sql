-- 1. Must run (go)
-- 2. (def query-fn (:db.sql/query-fn state/system))
-- 3. (query-fn :get-contact-by-id {:id 1})
-- Changes to this file usually running (reset) is enough. But function name changes, table name changes
-- requires restarting REPL

-- :name get-contact-by-id
-- :result one
-- :doc Get a single contact by ID
-- (query-fn :get-contact-by-id {:id 1})
SELECT id, first, last, phone, email
FROM contact
WHERE id = :id;

-- :name get-all-contacts :many
-- :doc Get all contacts with optional pagination
-- (query-fn :get-all-contacts {:limit 10 :offset 0})
SELECT id, first, last, phone, email
FROM contact
ORDER BY id
    LIMIT :limit OFFSET :offset;

-- :name find-contacts :many
-- :doc Find contacts by name, phone, or email
-- ;; (query-fn :find-contacts {:text "john"})
SELECT id, first, last, phone, email
FROM contact
WHERE first ILIKE '%' || :text || '%'
   OR last ILIKE '%' || :text || '%'
   OR phone ILIKE '%' || :text || '%'
   OR email ILIKE '%' || :text || '%';

-- :name save-contact!
-- :result one
-- :doc Insert a new contact and return the inserted row
-- (query-fn :save-contact! {:first "Tom" :last "Müller" :phone "123-456-8888" :email "tom.müller@example.com"})
INSERT INTO contact (first, last, phone, email)
VALUES (:first, :last, :phone, :email)
    RETURNING *;

-- :name update-contact!
-- :result one
-- :doc Update an existing contact by ID and return the updated row
-- (query-fn :update-contact! {:id 1 :first "Updated" :last "Name" :phone "987-654-3210" :email "updated@example.com"})
UPDATE contact
SET first = :first,
    last = :last,
    phone = :phone,
    email = :email
WHERE id = :id
RETURNING *;

-- :name delete-contact! :exec
-- :doc Delete a contact by ID
-- (query-fn :delete-contact! {:id 1})
DELETE FROM contact WHERE id = :id;