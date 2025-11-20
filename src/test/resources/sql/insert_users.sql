-- Очищаем таблицы (будьте осторожны: удаляет ВСЕ данные из этих таблиц)
DELETE FROM user_roles;
DELETE FROM auth_schema.auth_users;
-- Вставляем тестовые данные
INSERT INTO auth_schema.auth_users (id, name, password, email, status)
VALUES (3, 'Test User', '$2a$10$DowE11.12345678901234567890123456789012345678901234567890', 'existing@example.com', 'UNBLOCKED');


