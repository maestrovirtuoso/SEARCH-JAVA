-- Keyspace creation
CREATE KEYSPACE IF NOT EXISTS search_data 
WITH replication = {
    'class': 'SimpleStrategy',
    'replication_factor': 3
};

USE search_data;

-- Documents table
CREATE TABLE IF NOT EXISTS documents (
    id TEXT PRIMARY KEY,
    title TEXT,
    content TEXT,
    category TEXT,
    author TEXT,
    metadata TEXT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Secondary indexes for filtering
CREATE INDEX IF NOT EXISTS ON documents (category);
CREATE INDEX IF NOT EXISTS ON documents (author);
CREATE INDEX IF NOT EXISTS ON documents (created_at);

-- Documents by category table for efficient category-based queries
CREATE TABLE IF NOT EXISTS documents_by_category (
    category TEXT,
    id TEXT,
    title TEXT,
    author TEXT,
    created_at TIMESTAMP,
    PRIMARY KEY (category, created_at, id)
) WITH CLUSTERING ORDER BY (created_at DESC, id ASC);