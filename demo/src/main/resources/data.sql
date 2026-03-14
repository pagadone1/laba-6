-- Test data for car service (runs after schema creation)
-- ON CONFLICT: не падать, если данные уже есть (повторный запуск, общая БД)
INSERT INTO customers (name, email, phone) VALUES
('Иван Петров', 'ivan@mail.ru', '+7-912-111-2233'),
('Мария Сидорова', 'maria@mail.ru', '+7-912-222-3344'),
('Алексей Козлов', 'alex@mail.ru', '+7-912-333-4455')
ON CONFLICT (email) DO NOTHING;

INSERT INTO mechanics (name, specialization) VALUES
('Сергей Мастеров', 'Двигатель'),
('Ольга Слесарёва', 'Кузов'),
('Дмитрий Электрик', 'Электрика')
;

INSERT INTO parts (name, price, quantity) VALUES
('Масло моторное 5W-40', 3500.00, 20),
('Фильтр воздушный', 1200.00, 15),
('Тормозные колодки', 4500.00, 8),
('Свечи зажигания (комплект)', 2200.00, 12)
;
