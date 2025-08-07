CREATE UNIQUE INDEX IF NOT EXISTS uniq_active_cart_per_session
ON cart (session_id)
WHERE status = 'ACTIVE';

-- для создания частичного уникального индекса 
-- CREATE UNIQUE INDEX ON cart(session_id) WHERE status = 'ACTIVE';
-- JPA (@Table(uniqueConstraints = …)) не поддерживает частичные (условные) индексы