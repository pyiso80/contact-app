ALTER TABLE contact ADD COLUMN created_at TIMESTAMP DEFAULT NOW();
--;;
WITH cte AS (
    SELECT id, NOW() - INTERVAL '2 day' - INTERVAL '1 second' * row_number() OVER (ORDER BY id) AS new_created_at
    FROM contact
)
UPDATE contact
SET created_at = cte.new_created_at
FROM cte
WHERE contact.id = cte.id;