CREATE TABLE contact (
    id SERIAL PRIMARY KEY,
    first TEXT,
    last TEXT,
    phone TEXT,
    email TEXT UNIQUE NOT NULL
);
--;;
INSERT INTO contact (first, last, phone, email) VALUES
('Alice', 'Johnson', '123-456-7890', 'alice.johnson@example.com'),
('Bob', 'Smith', '987-654-3210', 'bob.smith@example.com'),
('Charlie', 'Brown', '555-123-4567', 'charlie.brown@example.com'),
('Diana', 'Prince', '444-987-6543', 'diana.prince@example.com'),
('Edward', 'Norton', '333-222-1111', 'edward.norton@example.com');